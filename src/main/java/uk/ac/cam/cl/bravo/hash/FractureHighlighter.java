package uk.ac.cam.cl.bravo.hash;

import org.jetbrains.annotations.NotNull;
import uk.ac.cam.cl.bravo.dataset.BodypartView;
import uk.ac.cam.cl.bravo.pipeline.Uncertain;

import java.awt.image.BufferedImage;

public interface FractureHighlighter {

    /**
     * In the 'base' image, highlight the fracture based on the differences from the sample image.
     *
     * @param base Image inputted by the user (the new x-ray), preprocessed
     * @param sample Similar image, preprocessed and transformed to maximise overlay with the base image
     * @param bodypartView classified BodypartView
     * @return the 'base' image with highlights
     */
    @NotNull
    Uncertain<BufferedImage> highlight(
            @NotNull BufferedImage base,
            @NotNull BufferedImage sample,
            @NotNull Uncertain<BodypartView> bodypartView
    );

}
