package uk.ac.cam.cl.bravo;

import uk.ac.cam.cl.bravo.dataset.BodypartView;
import uk.ac.cam.cl.bravo.dataset.ImageSample;
import uk.ac.cam.cl.bravo.dataset.Normality;

import java.awt.image.BufferedImage;

/**
 * Author: Shehab Alshehabi (sa863)
 */
public interface MatchingImageFinder {
    ImageSample match(BufferedImage image, BodypartView bodypartView, Normality normality);
}
