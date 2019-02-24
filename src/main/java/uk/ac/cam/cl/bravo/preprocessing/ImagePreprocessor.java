package uk.ac.cam.cl.bravo.preprocessing;

import org.jetbrains.annotations.NotNull;
import uk.ac.cam.cl.bravo.pipeline.Uncertain;

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
public interface ImagePreprocessor {
    @NotNull
    Uncertain<BufferedImage> preprocess(@NotNull String filePath);
}
