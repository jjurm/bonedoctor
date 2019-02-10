package uk.ac.cam.cl.bravo.preprocessing;

import javafx.util.Pair;
import org.jetbrains.annotations.NotNull;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;

class ImagePreprocessorI implements ImagePreprocessor
{

    @NotNull
    @Override
    public BufferedImage preprocess(@NotNull  String imageName){

        BufferedImage srcFile = srcImg(imageName);
        BufferedImage buffFile = buffImg(imageName);

        BufferedImage outputFile;

        if (shouldFlip(srcFile, buffFile)){
            outputFile = invertImage(srcImg("invert-"+imageName), buffImg("invert-"+imageName));
            writeImage("invert-"+imageName, outputFile);
        }

        if (fleshy)
            outputFile = contrast(srcImg("invert-"+imageName), buffImg("invert-"+imageName));
        else
            outputFile = contrast(srcImg("invert-"+imageName));
        writeImage("invert-"+imageName, outputFile);

        outputFile = resize(srcImg("invert-"+imageName), buffImg("invert-"+imageName));

        return outputFile;

    }

    static boolean fleshy = false;

    private static BufferedImage invertImage(BufferedImage srcFile, BufferedImage inputFile) {

        inputFile.getGraphics().drawImage(srcFile, 0, 0, null);

        for (int x = 0; x < inputFile.getWidth(); x++) {
            for (int y = 0; y < inputFile.getHeight(); y++) {
                int rgba = inputFile.getRGB(x, y);
                Color col = new Color(rgba);
                Color colF = new Color(255-col.getRed(), 255-col.getGreen(), 255-col.getBlue());
                inputFile.setRGB(x, y, colF.getRGB());
            }
        }

        return inputFile;

    }

    private static boolean shouldFlip(BufferedImage srcFile, BufferedImage inputFile){

        inputFile.getGraphics().drawImage(srcFile, 0, 0, null);

        HashMap<Pair<Integer, Integer>, Integer> img = imgMap(inputFile);

        TreeMap<Integer, Integer> hist = histogram(img);
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
        HashMap<Integer, Integer> sortDiff = sortByValue(diff);
        int b = (Integer) sortDiff.keySet().toArray()[0];
        if (b>=127)
            return true;
        return false;
    }

    private static BufferedImage contrast(BufferedImage srcFile, BufferedImage inputFile) {

        inputFile.getGraphics().drawImage(srcFile, 0, 0, null);

        HashMap<Pair<Integer, Integer>, Integer> img = imgMap(inputFile);

        TreeMap<Integer, Integer> histo = histogram(img);

        HashMap<Integer, Integer> histoSorted = sortByValue(histo);

        int bT = blackThreshold(histo);
        int wT = whiteThreshold(histo, img.size(), bT);

        BufferedImage outputFile = new BufferedImage(inputFile.getWidth(), inputFile.getHeight(), inputFile.getType());

        for (int x = 0; x < inputFile.getWidth(); x++) {
            for (int y = 0; y < inputFile.getHeight(); y++) {
                int rgba = inputFile.getRGB(x, y);
                Color col = new Color(rgba, true);
                int gs = (int)(0.3*col.getRed() + 0.59*col.getGreen() + 0.11*col.getBlue());
                if (gs<=bT)
                    col = new Color(0,0,0);
                else if (gs>=wT)
                    col = new Color(255,255,255);
                else{
                    int c = (int) Math.floor(255.0/(wT-bT)*(gs-bT));
                    col = new Color(c,c,c);
                }
                outputFile.setRGB(x, y, col.getRGB());
            }
        }

        return outputFile;
    }

    private static BufferedImage contrast(BufferedImage srcFile) {

        HashMap<Pair<Integer, Integer>, Integer> img = imgMap(srcFile);

        TreeMap<Integer, Integer> histo = histogram(img);

        int bT = blackThreshold(histo);
        int wT = whiteThreshold(histo, img.size(), bT);

        BufferedImage outputFile = new BufferedImage(srcFile.getWidth(), srcFile.getHeight(), srcFile.getType());

        for (int x = 0; x < srcFile.getWidth(); x++) {
            for (int y = 0; y < srcFile.getHeight(); y++) {
                int rgba = srcFile.getRGB(x, y);
                Color col = new Color(rgba, true);
                int gs = (int)(0.3*col.getRed() + 0.59*col.getGreen() + 0.11*col.getBlue());
                if (gs<=bT)
                    col = new Color(0,0,0);
                else if (gs>=wT)
                    col = new Color(255,255,255);
                else{
                    int c = (int) Math.floor(255.0/(wT-bT)*(gs-bT));
                    col = new Color(c,c,c);
                }
                outputFile.setRGB(x, y, col.getRGB());
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
        HashMap<Integer, Integer> sortDiff = sortByValue(diff);
        return (Integer) sortDiff.keySet().toArray()[0];
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
        return w+10;
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

    private static  HashMap<Pair<Integer, Integer>, Integer> imgMap(BufferedImage inputFile){
        HashMap<Pair<Integer, Integer>, Integer> img = new HashMap<>();

        for (int x = 0; x <inputFile.getWidth(); x++) {
            for (int y = 0; y < inputFile.getHeight(); y++) {
                int rgba = inputFile.getRGB(x, y);
                Color col = new Color(rgba, true);
                img.put(new Pair<>(x,y), (int)(0.3*col.getRed() + 0.59*col.getGreen() + 0.11*col.getBlue()));
            }
        }
        return img;
    }

    private static void writeImage(String outputName, BufferedImage inputFile){
        try {
            File outputFile = new File(outputName);
            ImageIO.write(inputFile, "png", outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static BufferedImage resize(BufferedImage srcFile, BufferedImage inputFile) {

        inputFile.getGraphics().drawImage(srcFile, 0, 0, null);

        Image img = inputFile.getScaledInstance(320, 320, Image.SCALE_SMOOTH);
        BufferedImage buffImg = new BufferedImage(320, 320, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = buffImg.createGraphics();
        g2d.drawImage(img, 0, 0, null);
        g2d.dispose();

        return buffImg;
    }

    private static TreeMap<Integer, Integer> histogram(HashMap<Pair<Integer, Integer>, Integer> img){

        TreeMap<Integer, Integer> histo = new TreeMap<>();
        for (Pair p: img.keySet()) {
            int c = img.get(p);
            if (histo.containsKey(c))
                histo.put(c, histo.get(c) + 1);
            else
                histo.put(c, 1);
        }
        for (int i=0; i<256; i++){
            if (!histo.containsKey(i))
                histo.put(i, 0);
        }
        return histo;
    }

    public static HashMap<Integer, Integer> sortByValue(Map<Integer, Integer> tm)
    {
        // Create a list from elements of HashMap
        List<Map.Entry<Integer, Integer> > list =
                new LinkedList<Map.Entry<Integer, Integer> >(tm.entrySet());

        // Sort the list
        Collections.sort(list, new Comparator<Map.Entry<Integer, Integer> >() {
            public int compare(Map.Entry<Integer, Integer> o1,
                               Map.Entry<Integer, Integer> o2)
            {
                return -(o1.getValue()).compareTo(o2.getValue());
            }
        });

        // put data from sorted list to hashmap
        HashMap<Integer, Integer> temp = new LinkedHashMap<>();
        for (Map.Entry<Integer, Integer> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }


}