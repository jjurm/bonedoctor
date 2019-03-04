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

    /**
     * Given an image and list of images to find, find the visually closest image to the given image and output them
     * in a list rated by similarity
     * @param image input image, not preprocessed
     * @param domain list of images to choose from. They must all have the bodypart view equal to bodypartView.
     * @param n number of images to return
     * @return list of rated objects representing image samples and their corresponding score. higher better. This is
     *         different from cosine similarity since rated score must be universally understandable by other components
     *         and so the interpretation of cosine similarity is changed to fit this rated class.
     */
    @NotNull
    @Override
    public List<Rated<ImageSample>> findMatchingImages(@NotNull BufferedImage image, @NotNull List<ImageSample> domain, int n) {
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
        Map<ImageSample, float[]> outputImagesMap = classifier.executeInferenceGraphConcurrently(imageInferenceMap);

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
}
