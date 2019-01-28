package uk.ac.cam.cl.bravo;

import uk.ac.cam.cl.bravo.dataset.Bodypart;

import java.awt.image.BufferedImage;

/**
 * Responsible: Leon Mlodzian (lam206)
 */
public interface NormalityClassifier {
    Bodypart classify(BufferedImage image);
}
