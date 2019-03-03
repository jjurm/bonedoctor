package uk.ac.cam.cl.bravo.classify;

import com.google.common.collect.MinMaxPriorityQueue;
import org.jetbrains.annotations.NotNull;
import org.tensorflow.Tensor;
import uk.ac.cam.cl.bravo.dataset.Bodypart;
import uk.ac.cam.cl.bravo.dataset.BodypartView;
import uk.ac.cam.cl.bravo.dataset.ImageSample;
import uk.ac.cam.cl.bravo.pipeline.Rated;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static uk.ac.cam.cl.bravo.classify.Utils.bufferedImageToByteArray;
import static uk.ac.cam.cl.bravo.classify.Utils.computeCosineSimilarity;

public class PreciseImageMatcherImpl implements PreciseImageMatcher {
    private BodypartViewClassifierImpl classifier = new BodypartViewClassifierImpl();

    @NotNull
    @Override
    public List<Rated<ImageSample>> findMatchingImages(@NotNull BufferedImage image, @NotNull List<ImageSample> domain, int n) {
//        String imageDirectory = Dataset.DIR;
//        String imageDirectory = "/home/kwotsin/Desktop/group_project/data/MURA/";

        // Run inference once on the image to get the array's features
        Tensor<Float> preprocessedImage = classifier.executePreprocessingGraph(bufferedImageToByteArray(image));
        float[] outputArray = classifier.executeInferenceGraph(preprocessedImage);

        // Map filenames to results obtained
        Map<ImageSample, Tensor<Float>> imageInferenceMap = new HashMap<>();

        // Loop through the cluster images to preprocess into tensors
        for (ImageSample imageSample : domain){
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

        // Build a PQ to find the best results. Order: best matches first. Size limit will discard worst matches.
        Queue<Rated<ImageSample>> PQ = MinMaxPriorityQueue.maximumSize(n).create();

        for (ImageSample imageSample : outputImagesMap.keySet()){
            double currDistance = computeCosineSimilarity(outputArray, outputImagesMap.get(imageSample));

            // Add results to PQ. Rated needs better score to be lower
            PQ.add(new Rated<>(imageSample, -currDistance));
        }

        ArrayList<Rated<ImageSample>> list = new ArrayList<>(n);
        while (!PQ.isEmpty()) list.add(PQ.remove());
        return list;
    }

    public static void main(String[] args) throws IOException {
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
        List<Rated<ImageSample>> PQ = matcher.findMatchingImages(image, testDomain, 30);

        for (Rated<ImageSample> item : PQ){
            System.out.println(item.getValue().getPath());
            System.out.println(item.getScore());
        }
    }
}
