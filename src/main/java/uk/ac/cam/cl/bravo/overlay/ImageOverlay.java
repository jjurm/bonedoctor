package uk.ac.cam.cl.bravo.overlay;

import kotlin.Pair;
import org.jetbrains.annotations.NotNull;
import uk.ac.cam.cl.bravo.pipeline.Rated;

import java.awt.image.BufferedImage;

/**
 * Given a new image and a sample image, find a transformation of the sample image to maximise the match between the
 * images
 * <p>
 * Responsible: Juraj Micko (jm2186)
 */
public interface ImageOverlay {

    /**
     * Finds the best overlay. Then applies the transformation to the sample image.
     * <p>
     * Returns a Rated pair of (transformed sample image, the two images visually overlaid).
     */
    @NotNull
    Rated<Pair<BufferedImage, BufferedImage>> fitImage(
            @NotNull BufferedImage base,
            @NotNull BufferedImage sample,
            double downsample,
            double precision);

    /**
     * Normalises the given image to have the same size as the output of
     * {@link #fitImage(BufferedImage, BufferedImage, double, double)}
     */
    @NotNull
    BufferedImage normalise(@NotNull BufferedImage image);

}
