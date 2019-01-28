package uk.ac.cam.cl.bravo;

import uk.ac.cam.cl.bravo.dataset.Bodypart;
import uk.ac.cam.cl.bravo.dataset.BodypartView;

import java.awt.image.BufferedImage;

/**
 * Responsible: Kwot Sin Lee (ksl36)
 */
public interface BodypartViewClassifier {
    BodypartView classify(BufferedImage image, Bodypart bodypart);
}
