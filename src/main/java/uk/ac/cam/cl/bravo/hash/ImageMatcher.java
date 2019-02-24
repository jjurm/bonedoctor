package uk.ac.cam.cl.bravo.hash;

import org.jetbrains.annotations.NotNull;
import uk.ac.cam.cl.bravo.dataset.BodypartView;
import uk.ac.cam.cl.bravo.dataset.BoneCondition;
import uk.ac.cam.cl.bravo.dataset.ImageSample;
import uk.ac.cam.cl.bravo.pipeline.Rated;

import java.awt.image.BufferedImage;
import java.util.List;

public interface ImageMatcher {

    /**
     * Search the dataset for an image that has the same bone condition, body part and body part view.
     * <p>
     * This method can make use of the Dataset class.
     */
    @NotNull
    // TODO Juraj: consider changing bodypartView to Uncertain<BodypartView>, then allow searching other clusters too
    List<Rated<ImageSample>> findMatchingImage(
            @NotNull BufferedImage image,
            @NotNull BoneCondition boneCondition,
            @NotNull BodypartView bodypartView,
            int n
    );

}
