package uk.ac.cam.cl.bravo.hash;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;

public class FractureHighlighter2Impl implements FractureHighlighter2 {

    private Subject<BufferedImage> partialResults = BehaviorSubject.create();

    @Override
    public BufferedImage highlight(@NotNull BufferedImage base, @NotNull BufferedImage sample, double hammingThreshold, double gradient) {
        BufferedImage source = sample;
        BufferedImage target = base;
        FractureHighlighterImpl fractureHighlighter = new FractureHighlighterImpl(source, target, true, partialResult -> {
            partialResults.onNext(partialResult);
        });

        fractureHighlighter.setHammingThreshold((int) (hammingThreshold * 10));
        fractureHighlighter.setGradient(((int) (gradient *40)) - 40);
        BufferedImage result = fractureHighlighter.getFullHighlight();
        return result;
    }

    @Override
    public Observable<BufferedImage> getPartialResults() {
        return partialResults;
    }
}
