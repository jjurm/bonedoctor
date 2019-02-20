package uk.ac.cam.cl.bravo.classify;

import org.jetbrains.annotations.NotNull;
import org.tensorflow.Graph;
import org.tensorflow.Output;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import uk.ac.cam.cl.bravo.dataset.BoneCondition;
import uk.ac.cam.cl.bravo.preprocessing.ImagePreprocessor;
import uk.ac.cam.cl.bravo.preprocessing.ImagePreprocessorI;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class BoneConditionClassifierImpl implements BoneConditionClassifier {
    @Override
    @NotNull
    public BoneCondition classify(@NotNull BufferedImage image) {
        return BoneCondition.NORMAL;
    }

    private static BoneCondition classify(Path imagePath) {
        Tensor<Float> input = preprocess(imagePath);
        Tensor<Float> output = predict(input);
        float[][] result = new float[1][1];
        output.copyTo(result);
        System.out.println(result[0][0]);
        return result[0][0] > 0.5 ? BoneCondition.ABNORMAL : BoneCondition.NORMAL;
    }

    private static Tensor<Float> predict(Tensor<Float> input) {
        byte[] graphDef = readAllBytesOrExit(Paths.get("/Users/leonmlodzian/Desktop/uni/Group Project", "BoneConditionClassifier.pb"));
        Graph g = new Graph();
        g.importGraphDef(graphDef);
        Session s = new Session(g);
        Tensor<Float> output = s.runner().feed("densenet169_input", input).fetch("dense_1/Sigmoid").run().get(0).expect(Float.class);
        return output;
    }

    private static Tensor<Float> preprocess(Path imagePath) {
        byte[] imageBytes = readAllBytesOrExit(imagePath);
        try (Graph g = new Graph()) {
            GraphBuilder b = new GraphBuilder(g);
            final int H = 320;
            final int W = 320;
            final float mean = 117f;
            final float scale = 1f;
            // TODO: placeholder for multiple images
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


    private static byte[] readAllBytesOrExit(Path path) {
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            System.err.println("Failed to read [" + path + "]: " + e.getMessage());
            System.exit(1);
        }
        return null;
    }

    public static void main(String[] args) {
        File[] files = new File ("/Users/leonmlodzian/Desktop/uni/Group Project/MURA-v1.1/train/XR_ELBOW").listFiles();
        for (int i = 0; i < 10; i++) {
            File[] f2 = new File(files[i].getAbsolutePath()).listFiles();
            for (File file : f2) {
                File[] f3 = new File(file.getAbsolutePath()).listFiles();
                if (file.getAbsolutePath().endsWith("positive")) {
                    System.out.println("Positive study");
                } else if (file.getAbsolutePath().endsWith("negative")) {
                    System.out.println("Negative study");
                } else {
                    continue;
                }
                if (f3 == null)
                    continue;
                for (File file1 : f3) {
                    classify(Paths.get(file1.getAbsolutePath()));
                }
            }
        }
    }
}
