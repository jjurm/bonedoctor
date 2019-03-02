package uk.ac.cam.cl.bravo.hash;

import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;

/**
 * An interface similar to {@link FractureHighlighter}, but more suitable to be used in
 * {@link uk.ac.cam.cl.bravo.pipeline.MainPipeline}
 */
public interface FractureHighlighter2 {

    /**
     * @param hammingThreshold 0 to 1
     * @param gradient 0 to 1
     * @return
     */
    BufferedImage highlight(
            @NotNull BufferedImage base,
            @NotNull BufferedImage sample,
            double hammingThreshold,
            double gradient
    );

    Observable<BufferedImage> getPartialResults();

}
