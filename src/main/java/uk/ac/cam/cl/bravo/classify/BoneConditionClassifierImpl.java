package uk.ac.cam.cl.bravo.classify;

import org.jetbrains.annotations.NotNull;
import org.tensorflow.Graph;
import org.tensorflow.Output;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import uk.ac.cam.cl.bravo.dataset.BoneCondition;
import uk.ac.cam.cl.bravo.pipeline.Confidence;
import uk.ac.cam.cl.bravo.pipeline.Uncertain;
import uk.ac.cam.cl.bravo.preprocessing.ImagePreprocessor;
import uk.ac.cam.cl.bravo.preprocessing.ImagePreprocessorI;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static uk.ac.cam.cl.bravo.classify.Utils.bufferedImageToByteArray;

public class BoneConditionClassifierImpl implements BoneConditionClassifier {
    private final String inputNodeName = "densenet169_input";
    private final String outputNodeName = "dense_1/Sigmoid";
    private String graphDefFilename = "python/abnormality_classifier/BoneConditionClassifier.pb";

    @Override
    @NotNull
    public Uncertain<BoneCondition> classify(@NotNull BufferedImage image) {
        Tensor<Float> input = preprocess(image);
        Tensor<Float> output = predict(input);
        float[][] resultArray = new float[1][1];
        output.copyTo(resultArray);
        float result = resultArray[0][0];
        if (result > 0.5) {
            Confidence conf;
            if (result > 0.9)
                conf = Confidence.HIGH;
            else if (result > 0.7)
                conf = Confidence.MEDIUM;
            else
                conf = Confidence.LOW;
            return new Uncertain<>(BoneCondition.ABNORMAL, conf);
        } else {
            Confidence conf;
            if (result < 0.1)
                conf = Confidence.HIGH;
            else if (result < 0.3)
                conf = Confidence.MEDIUM;
            else
                conf = Confidence.LOW;
            return new Uncertain<>(BoneCondition.NORMAL, conf);
        }
    }

    /**
     * The graph used to preprocess the input image and output a tensor float object
     * @param image Image to preprocess
     * @return Output float tensor object representing preprocessed image
     */
    private Tensor<Float> preprocess(BufferedImage image) {
        byte[] imageBytes = bufferedImageToByteArray(image);
        try (Graph g = new Graph()) {
            GraphBuilder b = new GraphBuilder(g);
            final int H = 320;
            final int W = 320;
            final float mean = 117f;
            final float scale = 1f;
            final Output<String> input = b.constant("input", imageBytes);
            final Output<Float> output =
                    b.div(
                            b.sub(
                                    b.resizeBilinear(
                                            b.expandDims(
                                                    b.cast(b.decodeJpeg(input, 3), Float.class),
                                                    b.constant("make_batch", 0)),
                                            b.constant("size", new int[] {H, W})),
                                    b.constant("mean", mean)),
                            b.constant("scale", scale));
            try (Session s = new Session(g)) {
                // Generally, there may be multiple output tensors, all of them must be closed to prevent resource leaks.
                return s.runner().fetch(output.op().name()).run().get(0).expect(Float.class);
            }
        }
    }

    /**
     * Performs inference on the preprocessed input image
     * @param input preprocessed input image in the form a float tensor
     * @return predicted output array
     */
    private Tensor<Float> predict(Tensor<Float> input) {
        byte[] graphDef = readAllBytesOrExit(Paths.get(graphDefFilename));
        Graph g = new Graph();
        g.importGraphDef(graphDef);
        Session s = new Session(g);
        Tensor<Float> output = s.runner().feed(inputNodeName, input).fetch(outputNodeName).run().get(0).expect(Float.class);
        return output;
    }


    /**
     * Read bytes from files for getting graph def
     * @param path path to graph def
     * @return byte array of file read
     */
    private static byte[] readAllBytesOrExit(Path path) {
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            System.err.println("Failed to read [" + path + "]: " + e.getMessage());
            System.exit(1);
        }
        return null;
    }

}
