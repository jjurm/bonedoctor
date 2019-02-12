package uk.ac.cam.cl.bravo.classify;

import org.jetbrains.annotations.NotNull;
import org.tensorflow.Tensor;
import uk.ac.cam.cl.bravo.dataset.Bodypart;
import uk.ac.cam.cl.bravo.dataset.BodypartView;


import java.awt.image.BufferedImage;



public class BodypartViewClassifierImpl implements BodypartViewClassifier {
    @NotNull
    @Override
    public BodypartView classify(@NotNull BufferedImage image, @NotNull Bodypart bodypart) {
        return null;
    }

    /**
     * Build graph to preprocess and normalize image before feeding
     * into main model inference graph.
     *
     * @oaram   imageBytes   input image to be preprocessed
     */
    public Tensor<Float> preprocessingGraph(byte[] imageBytes){
        return null;
    }

}
