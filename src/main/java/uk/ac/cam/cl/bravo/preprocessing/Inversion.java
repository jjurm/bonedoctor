package uk.ac.cam.cl.bravo.preprocessing;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.TreeMap;

public class Inversion {

    public static BufferedImage invertImage(BufferedImage srcFile, BufferedImage inputFile) {

        inputFile.getGraphics().drawImage(srcFile, 0, 0, null);

        for (int x = 0; x < inputFile.getWidth(); x++) {
            for (int y = 0; y < inputFile.getHeight(); y++) {
                int rgba = inputFile.getRGB(x, y);
                Color col = new Color(rgba);
                Color colF = new Color(255 - col.getRed(), 255 - col.getGreen(), 255 - col.getBlue());
                inputFile.setRGB(x, y, colF.getRGB());
            }
        }

        return inputFile;

    }

    public static boolean shouldInvert(BufferedImage srcFile, BufferedImage inputFile){

        inputFile.getGraphics().drawImage(srcFile, 0, 0, null);

        HashMap<Point2D, Integer> img = Statistics.imgMap(inputFile);

        TreeMap<Integer, Integer> hist = Statistics.histogram(img);
        TreeMap<Integer, Integer> histo = hist;
        for (int k: hist.keySet()){
            if (hist.get(k) != 0)
                histo.put(k, hist.get(k));
        }

        HashMap<Integer, Integer> diff = new HashMap<>();
        for (int i=0; i<histo.size()-2; i++){
            int a = (int) histo.keySet().toArray()[i];
            int b = (int) histo.keySet().toArray()[i+1];
            int d = Math.abs(histo.get(a) -
                    histo.get(b));
            diff.put(Math.max(a,b), d);
        }
        HashMap<Integer, Integer> sortDiff = Statistics.sortByValue(diff);
        int b = (Integer) sortDiff.keySet().toArray()[0];
        if (b>=127)
            return true;
        return false;
    }
}
