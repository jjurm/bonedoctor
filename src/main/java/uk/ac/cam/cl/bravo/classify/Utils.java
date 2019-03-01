package uk.ac.cam.cl.bravo.classify;

import uk.ac.cam.cl.bravo.pipeline.Confidence;

import java.util.Map;

public class Utils {
    public static double computeMean(float[] feat){
        double mean = 0.0;

        for (int i=0; i<feat.length; i++){
            mean += feat[i];
        }

        return mean/feat.length;
    }

    public static double computeStd(float[] feat, double mean){
        double std = 0.0;

        for (int i=0; i<feat.length; i++){
            std += Math.pow(feat[i] - mean, 2.0);
        }

        std = Math.sqrt(std / feat.length);

        return std;
    }

    public static void standardize(float[] feat){
        double mean = computeMean(feat);
        double std = computeStd(feat, mean);

        for (int i=0; i<feat.length; i++){
            feat[i] = (float) ((feat[i] - mean) / std);
        }
    }

    /**
     * Function to compute the euclidean distance between two float arrays, for finding to which centroid the current
     * image belongs to
     * @param inputFeatures features from the extracted image that was given by user
     * @param meanFeatures the mean features of the images in the cluster
     * @return ret L2 distance between the two features
     */
    public static double computeL2Distance(float[] inputFeatures, float[] meanFeatures){
        double ret = 0.0;

        for (int i=0; i<inputFeatures.length; i++){
            ret += Math.pow((inputFeatures[i] - meanFeatures[i]), 2.0);
        }

        return Math.sqrt(ret);
    }

    public static double computeL1Distance(float[] feat0, float[] feat1){
        double ret = 0.0;

        for (int i=0; i<feat0.length; i++){
            ret += Math.abs(feat0[i] - feat1[i]);
        }

        return ret;
    }


    /**
     * Assesses the confidence of the label produced given the distance to each cluster centroid mean features
     * Let x and y be the L2 distances to each centroids from the current image features.
     * The ratio we compute is min(x,y)/max(x,y) and if this ratio is close to 0 we have a very high confidence.
     * otherwise, if x ~= y, then we have the ratio close to 1.0, which indicates the model is ambivalent about
     * the cluster assigned.
     *
     * Assumes that all arrays are standardized already.
     *
     * @param labelToMeanFeaturesMap Map containing the features for the cluster done
     * @param minL2Distance Minimum distance from one feature to the centroids
     * @return
     */
    protected static Confidence getConfidenceLevel(double minL2Distance, float[] outputArray, Map<Integer, float[]> labelToMeanFeaturesMap){
        double dist0 = computeL2Distance(outputArray, labelToMeanFeaturesMap.get(0));
        double dist1 = computeL2Distance(outputArray, labelToMeanFeaturesMap.get(1));

        double ratio = Math.min(dist0, dist1) / Math.max(dist0, dist1);

        // Perform the bucketing here
        if (ratio < 0.333){
            return Confidence.HIGH;
        }

        else if(ratio < 0.666){
            return Confidence.MEDIUM;
        }

        else{
            return Confidence.LOW;
        }
    }



}