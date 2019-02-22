package uk.ac.cam.cl.bravo.classify;

import org.jetbrains.annotations.NotNull;
import uk.ac.cam.cl.bravo.dataset.Bodypart;
import uk.ac.cam.cl.bravo.dataset.BodypartView;
import uk.ac.cam.cl.bravo.pipeline.Uncertain;

import java.awt.image.BufferedImage;

/**
 * Responsible: Kwot Sin Lee (ksl36)
 */
public interface BodypartViewClassifier {
    @NotNull
    Uncertain<BodypartView> classify(@NotNull BufferedImage image, @NotNull Bodypart bodypart);
}
