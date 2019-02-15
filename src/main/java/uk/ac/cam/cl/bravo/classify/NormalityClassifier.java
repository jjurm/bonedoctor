package uk.ac.cam.cl.bravo.classify;

import org.jetbrains.annotations.NotNull;
import uk.ac.cam.cl.bravo.dataset.Normality;

import java.awt.image.BufferedImage;

/**
 * Responsible: Leon Mlodzian (lam206)
 */
public interface NormalityClassifier {
    @NotNull
    Normality classify(@NotNull BufferedImage image);
}
