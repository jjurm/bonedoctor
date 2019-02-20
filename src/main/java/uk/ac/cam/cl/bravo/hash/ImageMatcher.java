package uk.ac.cam.cl.bravo.hash;

import org.jetbrains.annotations.NotNull;
import uk.ac.cam.cl.bravo.dataset.BodypartView;
import uk.ac.cam.cl.bravo.dataset.BoneCondition;
import uk.ac.cam.cl.bravo.dataset.ImageSample;

import java.awt.image.BufferedImage;

public interface ImageMatcher {

    /**
     * Search the dataset for an image that has the same bone condition, body part and body part view.
     *
     * This method can make use of the Dataset class.
     */
    @NotNull
    ImageSample findMatchingImage(
            @NotNull BufferedImage image,
            @NotNull BoneCondition boneCondition,
            @NotNull BodypartView bodypartView);

}
