package uk.ac.cam.cl.bravo.classify;

import org.jetbrains.annotations.NotNull;
import uk.ac.cam.cl.bravo.dataset.BoneCondition;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class BoneConditionClassifierImpl implements BoneConditionClassifier {
  @Override
  @NotNull
  public BoneCondition classify(@NotNull BufferedImage image) {
    // assume "image" has been preprocessed appropriately

    return BoneCondition.NORMAL;
  }

  public static void main(String[] args) throws IOException {
    BufferedImage img = ImageIO.read(new File("/Users/leonmlodzian/Desktop/uni/Group Project/MURA-v1.1/train/XR_ELBOW/patient01005/study1_negative/image3.png"));
    System.out.println(img);
  }
}
