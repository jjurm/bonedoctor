package uk.ac.cam.cl.bravo.preprocessing;

import javafx.util.Pair;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

public class Contrast {

    public static BufferedImage contrast(BufferedImage srcFile,
                                         Pair<HashSet<Point2D>, HashSet<Point2D>> insideOutside) {

        HashMap<Point2D, Integer> img = Statistics.imgMap(srcFile, EdgeRemoval.inside(insideOutside));

        TreeMap<Integer, Integer> histo = Statistics.histogram(img);

        HashMap<Integer, Integer> histoSorted = Statistics.sortByValue(histo);

        int bT = blackThreshold(histo);
        int wT = whiteThreshold(histo, img.size(), bT);

        BufferedImage outputFile = new BufferedImage(srcFile.getWidth(), srcFile.getHeight(), srcFile.getType());

        for (int x = 0; x < srcFile.getWidth(); x++) {
            for (int y = 0; y < srcFile.getHeight(); y++) {
                if (EdgeRemoval.outside(insideOutside).contains(new Point2D(x,y))){
                    outputFile.setRGB(x, y, new Color(0,0,0).getRGB());
                }
                else {
                    int rgba = srcFile.getRGB(x, y);
                    Color col = new Color(rgba, true);
                    int gs = (int) (0.3 * col.getRed() + 0.59 * col.getGreen() + 0.11 * col.getBlue());
                    if (gs <= bT)
                        col = new Color(0, 0, 0);
                    else if (gs >= wT)
                        col = new Color(255, 255, 255);
                    else {
                        int c = (int) Math.floor(255.0 / (wT - bT) * (gs - bT));
                        col = new Color(c, c, c);
                    }
                    outputFile.setRGB(x, y, col.getRGB());
                }
            }
        }

        return outputFile;
    }

    private static int blackThreshold(TreeMap<Integer, Integer> hist){

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
        return ((Integer)sortDiff.keySet().toArray()[0])+5;
    }

    private static int whiteThreshold(TreeMap<Integer, Integer> histo, int size, int bT){

        int sum=0;
        for (int i=bT; i<=255; i++){
            sum+=histo.get(histo.keySet().toArray()[i]);
        }
        int avg =  (int) (sum*1.0/(255-bT));
        int w =255;
        for (int i =250; i>=0; i--){
            int x = histo.get(histo.keySet().toArray()[i]);
            if (x>=avg) {
                w = (Integer) histo.keySet().toArray()[i];
                break;
            }
        }
        return w;
    }

}
