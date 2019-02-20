package uk.ac.cam.cl.bravo.preprocessing;

import javafx.util.Pair;
import org.jetbrains.annotations.NotNull;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;

public class ImagePreprocessorI implements ImagePreprocessor
{

    @NotNull
    @Override
    public BufferedImage preprocess(@NotNull  String imageName){

        BufferedImage srcFile = srcImg(imageName);
        BufferedImage buffFile = buffImg(imageName);

        BufferedImage outputFile = srcFile;

        if (Inversion.shouldInvert(srcFile, buffFile)){
            outputFile = Inversion.invertImage(srcFile, new BufferedImage(srcFile.getWidth(),
                    srcFile.getHeight(), BufferedImage.TYPE_3BYTE_BGR));
        }

        Pair edgeRemoval= EdgeRemoval.edgeRemoval(outputFile, new BufferedImage(outputFile.getWidth(),
                outputFile.getHeight(), BufferedImage.TYPE_3BYTE_BGR));

        outputFile = (BufferedImage) edgeRemoval.getKey();
        HashSet<Point2D> band = (HashSet) edgeRemoval.getValue();

        if (fleshy)
            outputFile = Contrast.contrast(outputFile, new BufferedImage(outputFile.getWidth(),
                    outputFile.getHeight(), BufferedImage.TYPE_3BYTE_BGR), EdgeRemoval.insideOutside(outputFile,
                    new BufferedImage(outputFile.getWidth(),
                    outputFile.getHeight(), BufferedImage.TYPE_3BYTE_BGR)));
        else
            outputFile = Contrast.contrast(outputFile, EdgeRemoval.insideOutside(outputFile,
                    new BufferedImage(outputFile.getWidth(),
                    outputFile.getHeight(), BufferedImage.TYPE_3BYTE_BGR)));

        outputFile = EdgeRemoval.colourBand(outputFile, new BufferedImage(outputFile.getWidth(),
                outputFile.getHeight(), BufferedImage.TYPE_3BYTE_BGR), band);

        outputFile = Crop.crop(outputFile, new BufferedImage(outputFile.getWidth(),
                outputFile.getHeight(), BufferedImage.TYPE_3BYTE_BGR));

        return outputFile;

    }

    private static BufferedImage buffImg(String imageName){
        try {
            BufferedImage i = ImageIO.read(new File(imageName));
            BufferedImage img = new BufferedImage(i.getWidth(), i.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
            return img;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static BufferedImage srcImg(String imageName){
        try {
            BufferedImage i = ImageIO.read(new File(imageName));
            return i;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    static boolean fleshy = false;




}
