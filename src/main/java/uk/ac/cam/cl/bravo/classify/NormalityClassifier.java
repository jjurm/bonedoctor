package uk.ac.cam.cl.bravo.classify;

import org.jetbrains.annotations.NotNull;
import uk.ac.cam.cl.bravo.dataset.Bodypart;

import java.awt.image.BufferedImage;

/**
 * Responsible: Leon Mlodzian (lam206)
 */
public interface NormalityClassifier {
    @NotNull
    Bodypart classify(@NotNull BufferedImage image);
}
