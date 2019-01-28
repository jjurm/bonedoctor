package uk.ac.cam.cl.bravo.overlay;

import org.jetbrains.annotations.NotNull;
import uk.ac.cam.cl.bravo.dataset.BodypartView;
import uk.ac.cam.cl.bravo.dataset.ImageSample;
import uk.ac.cam.cl.bravo.dataset.Normality;

import java.awt.image.BufferedImage;

/**
 * Author: Shehab Alshehabi (sa863)
 */
public interface MatchingImageFinder {
    @NotNull
    ImageSample match(@NotNull BufferedImage image, @NotNull BodypartView bodypartView, @NotNull Normality normality);
}
