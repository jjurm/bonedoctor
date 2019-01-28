package uk.ac.cam.cl.bravo;

import java.awt.image.BufferedImage;

/**
 * Preprocess images
 * - Remap pixel intensities to make background completely dark and make bones use the whole scale between black and white
 * - Remove borders
 * - Flip colours if needed
 * - Crop the image to only contain the bone
 * <p>
 * Responsible: Nicole Joseph (nmj33)
 */
interface ImagePreprocessor {
    BufferedImage preprocess(BufferedImage input);
}
