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
    public BufferedImage preprocess(@NotNull BufferedImage input) {
        HashMap<Pair<Integer, Integer>, Integer> img = imgMap(input);

        BufferedImage outputFile = input;

        if (shouldFlip(img, input.getWidth(), input.getHeight())) {
            outputFile = invertImage(input);
        }

        return constrast(outputFile);
    }

    private static BufferedImage invertImage(BufferedImage inputFile) {

        BufferedImage outputFile = new BufferedImage(inputFile.getWidth(), inputFile.getHeight(), inputFile.getType());

        for (int x = 0; x < inputFile.getWidth(); x++) {
            for (int y = 0; y < inputFile.getHeight(); y++) {
                int rgba = inputFile.getRGB(x, y);
                Color col = new Color(rgba);
                Color colF = new Color(255-col.getRed(), 255-col.getGreen(), 255-col.getBlue());
                outputFile.setRGB(x, y, colF.getRGB());
            }
        }

        return outputFile;

    }

    private static BufferedImage constrast(BufferedImage inputFile) {

        HashMap<Pair<Integer, Integer>, Integer> img = imgMap(inputFile);

        TreeMap<Integer, Integer> histo = histogram(img);

        HashMap<Integer, Integer> histoSorted = sortByValue(histo);

        int bT = blackThreshold(histoSorted);
        int wT = whiteThreshold(histo, img.size());

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

    private static BufferedImage buffImg(String imageName){
        try {
            return ImageIO.read(new File(imageName));
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

    private static int blackThreshold(HashMap<Integer, Integer> histoSorted){
        int b = 200;
        int i =0;
        while (b>=200){
            b = (Integer) histoSorted.keySet().toArray()[i];
            i++;
        }
        return b;
    }

    private static int whiteThreshold(TreeMap<Integer, Integer> histo, int size){

        int w =255;
        for (int i =250; i>=0; i--){
            int x = histo.get(histo.keySet().toArray()[i]);
            if (x>=0.009*size) {
                w = (Integer) histo.keySet().toArray()[i];
                break;
            }
        }
        return w;
    }

    public static HashMap<Integer, Integer> sortByValue(TreeMap<Integer, Integer> tm)
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

    private  static boolean shouldFlip(HashMap<Pair<Integer, Integer>, Integer> img, int width, int height){

        int flip = darkBones(0,0,img,  width) +
                darkBones(0,height-1,img,  width)+
                darkBones(width-1,0,img,  width)+
                darkBones(width-1,height-1,img,  width) +
                darkBones(width/2, 0, img, width);
        if (flip>2)
            return true;
        else
            return false;

    }

    //returns 1 if the bones are predicted to be dark and 0 otherwise (normal X-ray)
    private static int darkBones(int x, int y, HashMap<Pair<Integer, Integer>, Integer> img, int width){
        int height = img.size()/width;
        int m=0,n=0; //m is x movements and n is y movements

        int s =8;

        if (x==0 && y==0){
            m=s;
            n=s;
        }

        else if(x!=0 && y!=0){
            m=-s;
            n=-s;
        }

        else if(y==0){
            if (x<width-1) {
                m = 0;
                n=s;
            }
            else{
                m=-s;
                n=s;
            }
        }

        else {
            m=s;
            n=-s;

        }
        ArrayList<Integer> changes = new ArrayList<>();
        //lighter 0, darker 1
        while(x>=0 && x<width && y>=0 && y<height){
            int c1 = img.get(new Pair<>(x,y));
            x+=m;
            y+=n;
            if (!(x>=0 && x<width && y>=0 && y<height))
                break;
            //System.out.println(x+" "+y);
            int c2 = img.get(new Pair<>(x,y));
            if (c2-c1>=8) {
                changes.add(0);
                if (changes.size()>=2){
                    if (changes.get(changes.size()-1) == changes.get(changes.size()-2))
                        break;
                }
            }
            else if (c1-c2>=8) {
                changes.add(1);
                if (changes.size()>=2){
                    if (changes.get(changes.size()-1) == changes.get(changes.size()-2))
                        break;
                }
            }
        }
        if (changes.size()>=2 && changes.get(changes.size()-1) == changes.get(changes.size()-2))
            return changes.get(changes.size()-1);
        else
            return 0;
    }

    private static void preProcessFromName(String imageName){
        BufferedImage inputFile = buffImg(imageName);

        HashMap<Pair<Integer, Integer>, Integer> img = imgMap(inputFile);

        BufferedImage outputFile = inputFile;

        if (shouldFlip(img, inputFile.getWidth(), inputFile.getHeight())) {
            outputFile = invertImage(inputFile);
        }

        outputFile = constrast(outputFile);

        writeImage(imageName, outputFile);
    }
}