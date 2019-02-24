package uk.ac.cam.cl.bravo.hash;

import javafx.util.Pair;
import org.jetbrains.annotations.NotNull;
import uk.ac.cam.cl.bravo.dataset.BodypartView;
import uk.ac.cam.cl.bravo.dataset.BoneCondition;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

public interface ImageMatcher {

    /**
     * Search the dataset for an image that has the same bone condition, body part and body part view.
     * <p>
     * This method can make use of the Dataset class.
     */
    @NotNull
    List<Pair<File, Integer>> findMatchingImage(
            @NotNull BufferedImage image,
            @NotNull BoneCondition boneCondition,
            @NotNull BodypartView bodypartView,
            int n
    );

}
