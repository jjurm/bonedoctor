package uk.ac.cam.cl.bravo.overlay;

import org.jetbrains.annotations.NotNull;

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
     * Returns a pair of (base image, transformed sample image). The base image contains only necessary modifications
     * (e.g. make it the same resolution as the sample image)
     */
    @NotNull
    BufferedImage fitImage(@NotNull BufferedImage base, @NotNull BufferedImage sample);

    /**
     * Normalises the given image to have the same size as the output of {@link #fitImage(BufferedImage, BufferedImage)}
     */
    @NotNull
    BufferedImage normalise(@NotNull BufferedImage image);

}
