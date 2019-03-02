package uk.ac.cam.cl.bravo.classify;

import javafx.util.Pair;
import org.jetbrains.annotations.NotNull;
import uk.ac.cam.cl.bravo.dataset.BodypartView;
import uk.ac.cam.cl.bravo.dataset.ImageSample;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

/**
 * From a list of similar images, chooses a few that are the closest matches.
 */
public interface PreciseImageMatcher {

    /**
     * Search the dataset for an image that has the body part and body part view.
     *
     * @param image input image, not preprocessed
     * @param bodypartView bodypart view
     * @param domain list of images to choose from. They must all have the bodypart view equal to bodypartView.
     * @param n number of images to return
     * @return n closest matches from the domain
     */
    @NotNull
    List<Pair<File, Integer>> findMatchingImages(
            @NotNull BufferedImage image,
            @NotNull BodypartView bodypartView,
            @NotNull List<ImageSample> domain,
            int n
    );

}
