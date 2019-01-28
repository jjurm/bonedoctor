package uk.ac.cam.cl.bravo.overlay;

import org.jetbrains.annotations.NotNull;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * Given a new image and a sample image, find a transformation of the sample image to maximise the match between the
 * images
 * <p>
 * Responsible: Juraj Micko (jm2186)
 */
public interface ImageOverlay {
    @NotNull
    AffineTransform findBestOverlay(@NotNull BufferedImage base, @NotNull BufferedImage sample);
}
