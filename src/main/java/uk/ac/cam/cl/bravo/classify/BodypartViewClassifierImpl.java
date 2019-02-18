package uk.ac.cam.cl.bravo.classify;

import org.jetbrains.annotations.NotNull;
import org.tensorflow.*;
import uk.ac.cam.cl.bravo.dataset.Bodypart;
import uk.ac.cam.cl.bravo.dataset.BodypartView;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;


public class BodypartViewClassifierImpl implements BodypartViewClassifier {
    @NotNull
    @Override
    public BodypartView classify(@NotNull BufferedImage image, @NotNull Bodypart bodypart) {
        return null;
    }

    /**
     *
     * @param input input image bytes stored in graph as a constant
     * @param H     output height of preprocessed image
     * @param W     output width of preprocessed image
     * @param mean  mean value to normalize image
     * @param scale scale value to normalize image
     * @param b     Graph builder to add on more nodes to the graph
     * @return preprocessedInput    output from image preprocessing graph ready for inference.
     */

    private Output<Float> preprocessImage(Output<String> input, int H, int W, float mean, float scale, GraphBuilder b){
        // Decode input as jpeg and cast as float
        Output<Float> preprocessedInput = b.cast(b.decodeJpeg(input, 3), Float.class);

        // Expand batch dimension to get (1, H, W, C)
        preprocessedInput = b.expandDims(preprocessedInput, b.constant("make_batch", 0));

        // Resize the input image with bilinear interpol
        preprocessedInput = b.resizeBilinear(preprocessedInput, b.constant("size", new int[] {H, W}));

        // Perform mean normalization
        preprocessedInput = b.sub(preprocessedInput, b.constant("mean", mean));
        preprocessedInput = b.div(preprocessedInput, b.constant("scale", scale));

        return preprocessedInput;

    }

    /**
     * Build graph to preprocess and normalize image before feeding
     * into main model inference graph. The input size image must be of
     * shape (299, 299, 3).
     *
     * We perform standard mean normalization used in inception networks.
     *
     * @param imageBytes input image to be preprocessed
     * @return PreprocessedTensor tensor representing preprocessed image
     */
    public Tensor<Float> preprocessingGraph(byte[] imageBytes){
        try (Graph g = new Graph()) {
            GraphBuilder b = new GraphBuilder(g);

            // Define fixed parameters
            final int H = 299;
            final int W = 299;
            final float mean = 117f;
            final float scale = 1f;

            // TODO: Check if need to use placeholder for input or could build graph consecutively.
//            final Output<Float> input = b.placeholder("input", DataType.STRING);

            // Setup input and output nodes
            final Output<String> input = b.constant("input", imageBytes);
            final Output<Float> output = preprocessImage(input, H, W, mean, scale, b);

            // Build a session and obtain the preprocessed image
            try (Session s = new Session(g)){
                return s.runner().fetch(output.op().name()).run().get(0).expect(Float.class);
            }

        }
    }

    /**
     * Reads an image into a byte array for processing in the graph.
     * @param ImageName
     * @return byteArray
     */
    private static byte[] imageToByteArray(String ImageName){
        // Open the file
        File imgPath = new File(ImageName);
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(imgPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Get the data in bytes form from the rastered image
        DataBufferByte data = (DataBufferByte) bufferedImage.getRaster().getDataBuffer();

        return data.getData();

    }

    public static void main(String[] args){
        System.out.println("lol123");
    }

}
