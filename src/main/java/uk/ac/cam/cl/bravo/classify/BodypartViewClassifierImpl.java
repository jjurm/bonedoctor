package uk.ac.cam.cl.bravo.classify;

import org.jetbrains.annotations.NotNull;
import org.tensorflow.*;
import uk.ac.cam.cl.bravo.dataset.Bodypart;
import uk.ac.cam.cl.bravo.dataset.BodypartView;
import uk.ac.cam.cl.bravo.dataset.ImageSample;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.stream.Stream;


public class BodypartViewClassifierImpl implements BodypartViewClassifier {
    @NotNull
    @Override
    public BodypartView classify(@NotNull ImageSample imageSample) {
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
    public Tensor<Float> executePreprocessingGraph(byte[] imageBytes){
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


    private static byte[] readBytesFromFile(String filename){
        try{
            return Files.readAllBytes(new File(filename).toPath());
        }catch (IOException e){
            System.err.println("Cannot find [" + filename + "]: " + e.getMessage());
            System.exit(1);
        }
        return null;
    }

    private float[] executeInferenceGraph(byte[] graphDef,
                                          Tensor<Float> image,
                                          String inputNodeName,
                                          String outputNodeName){
        try(Graph g = new Graph()){
            // First restore the graph definition from frozen graph
            g.importGraphDef(graphDef);

            // Initiate a session to perform inference and try to get the res
            try(Session s = new Session(g);
                Tensor<Float> result = s.runner().feed(
                        inputNodeName, image).fetch(outputNodeName).run().get(0).expect(Float.class)){
                // Obtain shape of the result
                final long[] shape = result.shape();

                // Output result is shape [1, 8, 8, 2048] tensor, so we flatten it.
                float[][][][] retArray = result.copyTo(new float [(int) shape[0]][(int) shape[1]][(int) shape[2]][(int) shape[3]]);
                float[] flattenedArray = flattenArray(retArray);

                return flattenedArray;

            }
        }
    }

    private float[] flattenArray(float[][][][] retArray){
        // Build output array
        float[] ret = new float[(int) (retArray.length
                                        * retArray[0].length
                                        * retArray[0][0].length
                                        * retArray[0][0][0].length)];

        // Start flattening it
        // TODO: check mathematics.
        for (int i=0; i<retArray.length; i++){
            for (int j=0; j<retArray[0].length; j++){
                for (int k=0; k<retArray[0][0].length; k++){
                    for (int l=0; l<retArray[0][0][0].length; l++){
                        // Compute index for each float element
                        int index = i * retArray[0].length * retArray[0][0].length * retArray[0][0][0].length
                                + j * retArray[0][0].length * retArray[0][0][0].length
                                + k * retArray[0][0][0].length
                                + l;

                        ret[index] = retArray[i][j][k][l];
                    }
                }
            }
        }

        return ret;
    }

    // TODO: return view label and list of image samples, but add the information to the Image Sample class
    // Hashing takes in a list of image samples

    // Lesson: storing image sample as a new class and successively build up the image sample info

    public static void main(String[] args){
//        // Test with one image
//        String imageFilename = "/home/kwotsin/Desktop/group_project/python/data/MURA-v1.1/image_by_class/XR_HUMERUS/train_XR_HUMERUS_patient03225_study1_negative_image2.png";
//        byte[] imageBytes = readBytesFromFile(imageFilename);
//        BodypartViewClassifierImpl classifier = new BodypartViewClassifierImpl();
//
//        Tensor<Float> preprocessedImage = classifier.executePreprocessingGraph(imageBytes);
//        System.out.println(preprocessedImage); // gives float tensor of shape [1, 299, 299, 3]
//
//        // Inference with PB file
//        String graphDefFilename = "/home/kwotsin/Desktop/group_project/java/bonedoctor/python/view_clustering/InceptionV3.pb";
//        byte[] graphDef = readBytesFromFile(graphDefFilename);
//        String inputNodeName = "input_1"; // based on python nodes inspection
//        String outputNodeName = "mixed10/concat"; // based on python nodes inspection.
//
//        classifier.executeInferenceGraph(graphDef, preprocessedImage, inputNodeName, outputNodeName);
    }

}
