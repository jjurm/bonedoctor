package uk.ac.cam.cl.bravo.hash;

import org.jetbrains.annotations.NotNull;
import uk.ac.cam.cl.bravo.dataset.BodypartView;
import uk.ac.cam.cl.bravo.pipeline.Uncertain;

import java.awt.image.BufferedImage;

public interface FractureHighlighter {

    /**
     * In the 'source' image, target the fracture based on the differences from the sample image.
     *
     * @param startx
     * @param starty
     * @param endx
     * @param endy
     * @return the 'source' image with highlights
     */
    @NotNull
    BufferedImage getHighlight(int startx, int starty, int endx, int endy);

    /**
     * Automatically selects the full image for the getHighlight method
     *
     * @return a highlighted image
     */
    BufferedImage getFullHighlight();

    /**
     * Set the bounding rectangle in the 'source' image that you want to learn from
     *
     * @param startx
     * @param starty
     * @param endx
     * @param endy
     */
    void setSourcePixels(int startx, int starty, int endx, int endy);

    /**
     * Alter the tolerance for two pixels to be judged to be similar. A threshold of 0 is the lowest and 64 is the
     * maximum hamming distance any two pixel hashes can have. Setting the threshold to 0 (its default value) is
     * recommended for large images as it allows for many optimisations. It may be necessary to increase this value
     * if the size of the regions to be compared is small.
     *
     * @param hammingThreshold
     */
    void setHammingThreshold(int hammingThreshold);

    /**
     * Set the gradient that will be used to generate the hashes. Changing this can significantly affect the results.
     * The gradient should be in the range -254 to 254 to generate any highlighting but the best results will be
     * found in between -50 and 50.
     *
     * A negative value will highlight the abnormalities whereas a positive one will highlight around abnormalities.
     *
     * @param gradient
     */
    void setGradient(int gradient);
}
