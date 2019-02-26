package uk.ac.cam.cl.bravo.preprocessing;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public class Statistics {

    public static TreeMap<Integer, Integer> histogram(HashMap<Point2D, Integer> img){

        TreeMap<Integer, Integer> histo = new TreeMap<>();
        for (Point2D p: img.keySet()) {
            int c = (img.get(p)/5)*5;
            if (histo.containsKey(c))
                histo.put(c, histo.get(c) + 1);
            else
                histo.put(c, 1);
        }
        return histo;
    }

    public static HashMap<Integer, Integer> sortByValue(Map<Integer, Integer> tm)
    {
        List<Map.Entry<Integer, Integer> > list =
                new LinkedList<Map.Entry<Integer, Integer> >(tm.entrySet());

        Collections.sort(list, (o1, o2) -> -(o1.getValue()).compareTo(o2.getValue()));

        HashMap<Integer, Integer> temp = new LinkedHashMap<>();
        for (Map.Entry<Integer, Integer> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }

    public static  HashMap<Point2D, Integer> imgMap(BufferedImage inputFile, HashSet<Point2D> inside){

        HashMap<Point2D, Integer> img = new HashMap<>();

        for (int x = 0; x <inputFile.getWidth(); x++) {
            for (int y = 0; y < inputFile.getHeight(); y++) {
                if (inside.contains(new Point2D(x,y))) {
                    int rgba = inputFile.getRGB(x, y);
                    Color col = new Color(rgba, true);
                    img.put(new Point2D(x, y), (int) (0.3 * col.getRed() + 0.59 * col.getGreen()
                            + 0.11 * col.getBlue()));
                }
            }
        }
        return img;
    }

    public static  HashMap<Point2D, Integer> imgMap(BufferedImage inputFile){

        HashMap<Point2D, Integer> img = new HashMap<>();

        for (int x = 0; x <inputFile.getWidth(); x++) {
            for (int y = 0; y < inputFile.getHeight(); y++) {
                int rgba = inputFile.getRGB(x, y);
                Color col = new Color(rgba, true);
                img.put(new Point2D(x, y), (int) (0.3 * col.getRed() + 0.59 * col.getGreen() + 0.11 * col.getBlue()));
            }
        }
        return img;
    }

    public static double var(ArrayList<Double> vals){
        int mean=0;
        for (double d: vals)
            mean+=d;
        mean = mean/vals.size();
        int var = 0;
        for (double d:vals)
            var+=(d-mean)*(d-mean);
        var = var/vals.size();
        return var;
    }

    public static TreeMap<Integer, Integer> padded(TreeMap<Integer, Integer> hist) {
        TreeMap<Integer, Integer> histo = new TreeMap<>();
        for (int i=0; i<256; i++){
            if (!hist.containsKey(i))
                histo.put(i, 0);
            else histo.put(i, hist.get(i));
        }
        return histo;
    }
}

