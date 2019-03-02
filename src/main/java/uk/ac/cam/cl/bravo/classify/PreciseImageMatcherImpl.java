package uk.ac.cam.cl.bravo.classify;

import javafx.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.tensorflow.Tensor;
import uk.ac.cam.cl.bravo.dataset.Bodypart;
import uk.ac.cam.cl.bravo.dataset.BodypartView;
import uk.ac.cam.cl.bravo.dataset.ImageSample;
import uk.ac.cam.cl.bravo.pipeline.Rated;
import uk.ac.cam.cl.bravo.pipeline.Uncertain;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

import static uk.ac.cam.cl.bravo.classify.Utils.bufferedImageToByteArray;
import static uk.ac.cam.cl.bravo.classify.Utils.computeCosineSimilarity;

public class PreciseImageMatcherImpl implements PreciseImageMatcher {
    private BodypartViewClassifierImpl classifier = new BodypartViewClassifierImpl();

    @NotNull
    @Override
    public PriorityQueue<Pair<ImageSample, Double>> findMatchingImages(@NotNull BufferedImage image, @NotNull List<ImageSample> domain, int n) {
//        String imageDirectory = Dataset.DIR;
//        String imageDirectory = "/home/kwotsin/Desktop/group_project/data/MURA/";

        // Run inference once on the image to get the array's features
        Tensor<Float> preprocessedImage = classifier.executePreprocessingGraph(bufferedImageToByteArray(image));
        float[] outputArray = classifier.executeInferenceGraph(preprocessedImage);

        // Limit the filenames for scalability
        List<ImageSample> clusterImageSamples = domain.subList(0, Math.min(domain.size(), n));

        // Map filenames to results obtained
        Map<ImageSample, Tensor<Float>> imageInferenceMap = new HashMap<>();

        // Loop through the cluster images to preprocess into tensors
        for (ImageSample imageSample : clusterImageSamples){
            try {
                BufferedImage currImage = ImageIO.read(new File(imageSample.getPath()));


                // Get preprocessed input
                Tensor<Float> preprocessedInput = classifier.executePreprocessingGraph(bufferedImageToByteArray(currImage));

                // Add to results
                imageInferenceMap.put(imageSample, preprocessedInput);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        // Find the float outputs of preprocessed inputs, but use this function
        // so that inference graph is built only once (costly).
        Map<ImageSample, float[]> outputImagesMap = classifier.executeInferenceGraphConsecutively(imageInferenceMap);

        // Build a PQ to find the best results
        PriorityQueue<Pair<ImageSample, Double>> PQ = new PriorityQueue<>((o1, o2) -> {
            if (o1.getValue() < o2.getValue()) {
                return -1;
            }
            else if (o1.getValue() > o2.getValue()){
                return 1;
            }
            else {
                return 0;
            }
        });

        for (ImageSample imageSample : outputImagesMap.keySet()){
            double currDistance = computeCosineSimilarity(outputArray, outputImagesMap.get(imageSample));

            // Add results to PQ
            PQ.add(new Pair(imageSample, currDistance));
        }

        return PQ;
    }

    public static void main(String[] args) throws IOException {
        // Test
        PreciseImageMatcherImpl matcher = new PreciseImageMatcherImpl();
        String testImage = "/home/kwotsin/Desktop/group_project/data/MURA/train/XR_HAND/patient10943/study1_negative/image2.png";

        List<ImageSample> testDomain = new ArrayList<>();
        testDomain.add(new ImageSample(
                "/home/kwotsin/Desktop/group_project/data/MURA/train/XR_HAND/patient11162/study1_negative/image2.png",
                new BodypartView(Bodypart.HAND, 1)));

        testDomain.add(new ImageSample(
                "/home/kwotsin/Desktop/group_project/data/MURA/train/XR_HAND/patient11168/study1_negative/image2.png",
                new BodypartView(Bodypart.HAND, 1)));

        testDomain.add(new ImageSample(
                "/home/kwotsin/Desktop/group_project/data/MURA/train/XR_HAND/patient11171/study1_negative/image3.png",
                new BodypartView(Bodypart.HAND, 1)));

        BufferedImage image = ImageIO.read(new File(testImage));
        PriorityQueue<Pair<ImageSample, Double>> PQ = matcher.findMatchingImages(image, testDomain, 30);

        while (PQ.size() != 0){
            Pair<ImageSample, Double> item = PQ.poll();
            System.out.println(item.getKey().getPath());
            System.out.println(item.getValue());
        }
    }
}
