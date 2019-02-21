package uk.ac.cam.cl.bravo.classify;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.jetbrains.annotations.NotNull;
import org.tensorflow.*;
import uk.ac.cam.cl.bravo.dataset.Bodypart;
import uk.ac.cam.cl.bravo.dataset.BodypartView;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;


public class BodypartViewClassifierImpl implements BodypartViewClassifier {
    private Map<Bodypart, Map<Integer, List<String>>> bodyPartToLabelToFilenamesMap = new HashMap<>();
    private Map<Bodypart, Map<Integer, float[]>> bodyPartToLabelToMeanFeaturesMap = new HashMap<>();
    private String outputDir; // Location where the python mean features are stored
    private String graphDefFilename; // Graph def file for inference

    // Input and output node names based on python nodes inspection.
    private final String inputNodeName = "input_1";
    private final String outputNodeName = "mixed10/concat";

    public BodypartViewClassifierImpl(String pathToOutputFeaturesFile, String pathToGraphDefFile){
        outputDir = pathToOutputFeaturesFile;
        graphDefFilename = pathToGraphDefFile;
    }

    @NotNull
    @Override
    public BodypartView classify(@NotNull BufferedImage image, @NotNull Bodypart bodypart) {
        // First load the results into the hashmaps if haven't already
        if (bodyPartToLabelToMeanFeaturesMap.get(bodypart) == null && bodyPartToLabelToFilenamesMap.get(bodypart) ==null){
            decodeBodyPartFolder(bodypart, outputDir);
        }

        // Convert image to byte array for inference
        byte[] imageBytes = bufferedImageToByteArray(image);
        // Preprocess image first to get float tensor of shape [1, 299, 299, 3]
        Tensor<Float> preprocessedImage = executePreprocessingGraph(imageBytes);

        // Inference with PB file

        byte[] graphDef = readBytesFromFile(graphDefFilename);


        // Obtained flattened array as output
        float[] outputArray = executeInferenceGraph(graphDef, preprocessedImage, inputNodeName, outputNodeName);

        // Obtain the right hashmap for this bodypart for comparison
        Map<Integer, List<String>> labelToFilenamesMap = bodyPartToLabelToFilenamesMap.get(bodypart);
        Map<Integer, float[]> labelToMeanFeaturesMap = bodyPartToLabelToMeanFeaturesMap.get(bodypart);

        // Compute L2 distance with benchmark
        int topLabel = 0;
        double minL2Distance = Float.MAX_VALUE;
        for (Integer label : labelToMeanFeaturesMap.keySet()){
            float[] meanFeatures = labelToMeanFeaturesMap.get(label);
            double currDistance = computeL2Distance(outputArray, meanFeatures);

            // Update the top label
            if (currDistance < minL2Distance){
                topLabel = label;
                minL2Distance = currDistance;
            }
        }

        return new BodypartView(bodypart, topLabel);
    }

    private static byte[] bufferedImageToByteArray(BufferedImage image){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "jpg", baos);

            byte[] bytes = baos.toByteArray();

            return bytes;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private double computeL2Distance(float[] inputFeatures, float[] meanFeatures){
        double ret = 0.0;

        for (int i=0; i<inputFeatures.length; i++){
            ret += Math.pow((inputFeatures[i] - meanFeatures[i]), 2.0);
        }

        return ret;
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

    private Map<Integer, List<String>> decodeFeaturesNamesFile(String featuresNamesFilename){
        try {
            JsonReader reader = new JsonReader(new FileReader(featuresNamesFilename));
            Map<String, List<String>> data = new Gson().fromJson(reader, Map.class);

            // Convert key to integers instead
            Map<Integer, List<String>> ret = new HashMap<>();

            for (String key : data.keySet()){
                ret.put(Integer.valueOf(key), data.get(key));
            }

            return ret;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    private float[] decodeMeanFeaturesFile(String meanFeaturesFilename){
        try {
            // Obtain the txt file contents and read all lines.
            String content = new String(Files.readAllBytes(Paths.get(meanFeaturesFilename)));
            String[] contentValues = content.split("\n");

            // Convert content values to float to return
            float[] contentFloatArray = new float[contentValues.length];

            for (int i=0; i<contentValues.length; i++){
                contentFloatArray[i] = Float.valueOf(contentValues[i]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void decodeBodyPartFolder(Bodypart bodypart, String outputDir){
        // Append XR_ to bodypart to account for dataset and impl differences
        String bodyPartString = "XR_" + bodypart;

        // Obtain array of files in the bodypart folder
        File[] filesArray = new File((outputDir + "/" + bodyPartString)).listFiles();

        System.out.println(outputDir + "/" + bodyPartString);

        for (File file : filesArray){
            // Decode mean features, process only label files containing mean features
            String filename = file.toString();
            if (filename.startsWith("mean_features_label")){
                // Get the label id
                int label = Integer.valueOf(filename.split("_")[filename.length()-4]);

                // Decode mean features
                float[] meanFeatures = decodeMeanFeaturesFile(filename);

                // Add it to required hashmap
                Map<Integer, float[]> toPut = new HashMap<>();
                toPut.put(label, meanFeatures);
                bodyPartToLabelToMeanFeaturesMap.put(bodypart, toPut);
            }

            // Decode features filenames
            else if (filename.startsWith("labels_to_image_filenames")){
                Map<Integer, List<String>> labelToImageFilenamesMap = decodeFeaturesNamesFile(filename);

                // Add it to required hashmap
                bodyPartToLabelToFilenamesMap.put(bodypart, labelToImageFilenamesMap);

            }
        }
    }

    public static void main(String[] args) throws IOException {
        // Read classifier
        String graphDefFilename = "/home/kwotsin/Desktop/group_project/java_github/bonedoctor/python/view_clustering/InceptionV3.pb";
        String outputDirName = "/home/kwotsin/Desktop/group_project/java_github/bonedoctor/python/view_clustering/output/";
        BodypartViewClassifierImpl classifier = new BodypartViewClassifierImpl(outputDirName, graphDefFilename);

        String testImage = "/home/kwotsin/Desktop/group_project/python/data/MURA-v1.1/image_by_class/XR_HUMERUS/train_XR_HUMERUS_patient03226_study1_negative_image2.png";

        BufferedImage image = ImageIO.read(new File(testImage));

        System.out.println(classifier.classify(image, Bodypart.HUMERUS));


//        // Read features file from txt
//        String meanFeaturesFilename = "/home/kwotsin/Desktop/group_project/python/output/XR_HUMERUS/label_to_image_filenames/mean_features_label_0.txt";
//        String content = new String(Files.readAllBytes(Paths.get(meanFeaturesFilename)));
//        String[] contentFloat = content.split("\n");
//
//        float[] contentFloatArray = new float[contentFloat.length];
//
//        for (int i=0; i<contentFloat.length; i++){
//            contentFloatArray[i] = Float.valueOf(contentFloat[i]);
//        }
//        System.out.println(Arrays.toString(contentFloatArray));

//        // Read features name file from json
//        String featuresNameFilename = "/home/kwotsin/Desktop/group_project/python/output/XR_HUMERUS/label_to_image_filenames/labels_to_image_filenames.json";
//
//        try {
//            JsonReader reader = new JsonReader(new FileReader(featuresNameFilename));
//            Map<String, List<String>> data = new Gson().fromJson(reader, Map.class);
//
//            // Convert key to integers instead
//            Map<Integer, List<String>> ret = new HashMap<>();
//
//            for (String key : data.keySet()){
//                ret.put(Integer.valueOf(key), data.get(key));
//            }
//
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }

    }

}
