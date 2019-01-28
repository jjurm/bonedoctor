package uk.ac.cam.cl.bravo;

import java.awt.image.BufferedImage;

/**
 * Given a new image and a sample image, find a transformation of the sample image to maximise the match between the
 * images
 *
 * Responsible: Juraj Micko (jm2186)
 */
public interface ImageOverlay {
    AffineTransformation findBestOverlay(BufferedImage base, BufferedImage sample);
}
