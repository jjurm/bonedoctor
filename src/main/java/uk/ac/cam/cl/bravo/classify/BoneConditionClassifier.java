package uk.ac.cam.cl.bravo.classify;

import org.jetbrains.annotations.NotNull;
import uk.ac.cam.cl.bravo.dataset.BoneCondition;
import uk.ac.cam.cl.bravo.pipeline.Uncertain;

import java.awt.image.BufferedImage;

/**
 * Responsible: Leon Mlodzian (lam206)
 */
public interface BoneConditionClassifier {
    @NotNull
    Uncertain<BoneCondition> classify(@NotNull BufferedImage image);
}
