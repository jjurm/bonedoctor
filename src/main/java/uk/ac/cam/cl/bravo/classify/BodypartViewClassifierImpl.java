package uk.ac.cam.cl.bravo.classify;

import org.jetbrains.annotations.NotNull;
import uk.ac.cam.cl.bravo.dataset.Bodypart;
import uk.ac.cam.cl.bravo.dataset.BodypartView;

import java.awt.image.BufferedImage;

/**
 * Responsible: Kwot Sin Lee (ksl36)
 */
public interface BodypartViewClassifierImpl {
    @NotNull
    BodypartView classify(@NotNull BufferedImage image, @NotNull Bodypart bodypart);
}
