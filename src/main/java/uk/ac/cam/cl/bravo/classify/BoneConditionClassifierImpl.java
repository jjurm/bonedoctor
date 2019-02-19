package uk.ac.cam.cl.bravo.classify;

import org.jetbrains.annotations.NotNull;
import uk.ac.cam.cl.bravo.dataset.BoneCondition;

import java.awt.image.BufferedImage;

public class BoneConditionClassifierImpl implements BoneConditionClassifier {
  @Override
  @NotNull
  public BoneCondition classify(@NotNull BufferedImage image) {
     // TODO: preprocess image
     // TODO: feed image through graph
    return BoneCondition.NORMAL;
  }
}
