package uk.ac.cam.cl.bravo.hash;

import javafx.util.Pair;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

public class ImageHasher implements java.io.Serializable {
    private static final int MATCH_THRESHOLD = 10;
    private ArrayList<Pair<File, Long>> simpleHash;

    public ImageHasher(){
        simpleHash = new ArrayList<>();
    }

    public void addImageFile(File f) {
        long hash = averageHash(f);
        simpleHash.add(new Pair<>(f, hash));
    }

    public ArrayList<File> getSimpleMatches(File f){
        try {
            BufferedImage inputImage = ImageIO.read(f);

            return getSimpleMatches(inputImage);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    public ArrayList<File> getSimpleMatches(BufferedImage img){
        long[] rotations = rotatedAverageHashes(img);

        ArrayList<File> matches = new ArrayList<>();
        int minHamming = Integer.MAX_VALUE;
        long imgHash;
        int hamming;
        for (Pair<File, Long> p : simpleHash){
            imgHash = p.getValue();
            for(int i = 0; i < rotations.length; i++) {
                hamming = Long.bitCount(rotations[i] ^ imgHash);

                if (hamming < minHamming){
                    minHamming = hamming;
                    matches.clear();
                    matches.add(p.getKey());
                    break; //prevent matching twice to same image
                } else if (hamming == minHamming){
                    matches.add(p.getKey());
                    break; //prevent matching twice to same image
                }
            }
        }

        return matches;
    }

    public ArrayList<File> getNMatches(BufferedImage img, int n){
        long[] rotations = rotatedAverageHashes(img);

        // Keep a set of matches, ordered by hamming
        TreeSet<Pair<Integer, File>> matches = new TreeSet<>(
                Comparator.comparing(Pair<Integer, File>::getKey).thenComparing(Pair::getValue)
        );

        int minHamming;
        long imgHash;
        for (Pair<File, Long> p : simpleHash){
            imgHash = p.getValue();
            minHamming = Integer.MAX_VALUE;
            for(int i = 0; i < rotations.length; i++) {
                minHamming = Math.min(Long.bitCount(rotations[i] ^ imgHash), minHamming);
            }
            if (minHamming < MATCH_THRESHOLD) {
                matches.add(new Pair<>(minHamming, p.getKey()));
                while (matches.size() > n) matches.pollLast();
            }
        }

        ArrayList<File> ret = new ArrayList<>();
        for (Pair<Integer, File> match : matches) {
            ret.add(match.getValue());
        }
        return ret;
    }

    public List<Pair<File, Integer>> getNPairs(BufferedImage img, int n){
        List<File> files = getNMatches(img, 2*n);
        List<Pair<File, Integer>> pairList = new ArrayList<>();

        for (File f: files) {
            pairList.add(new Pair<>(f, getDiff(img, f, 32)));
        }

        pairList.sort(new Comparator<Pair<File, Integer>>() {
            @Override
            public int compare(Pair<File, Integer> o1, Pair<File, Integer> o2) {
                return o1.getValue() - o2.getValue();
            }
        });

        return pairList.subList(0, n);
    }

    public File getBestMatch(File f){
        ArrayList<File> matches = getSimpleMatches(f);
        return getMatchFromList(f, matches, 32);
    }

    private static long averageHash (File f) {
        try {
            BufferedImage inputImage = ImageIO.read(f);

            return averageHash(inputImage);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return 0L;
    }

    protected static long averageHash (BufferedImage inputImage) {
        try {
            int scaledWidth = 8;
            int scaledHeight = 8;

            BufferedImage scaledImage = new BufferedImage(scaledWidth, scaledHeight, inputImage.getType());

            Graphics2D g2d = scaledImage.createGraphics();
            g2d.drawImage(inputImage, 0, 0, scaledWidth, scaledHeight, null);

            int avg = 0;

            Color pixel;

            for (int j = 0; j < 8; j++){
                for (int i = 0; i < 8; i++){
                    pixel = new Color(scaledImage.getRGB(i,j));
                    avg += pixel.getRed();
                }
            }

            avg = avg/64;

            long ans = 0;

            for (int j = 0; j < 8; j++){
                for (int i = 0; i < 8; i++){
                    pixel = new Color(scaledImage.getRGB(i,j));
                    ans = ans << 1;
                    ans += pixel.getRed() > avg ? 1 : 0;
                }
            }

            return ans;
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return 0L;
    }

    private static long[] rotatedAverageHashes (File f){
        try {
            BufferedImage inputImage = ImageIO.read(f);

            return rotatedAverageHashes(inputImage);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    private static long[] rotatedAverageHashes (BufferedImage inputImage){
        long[] ans = new long[4];

        try {
            int scaledWidth = 8;
            int scaledHeight = 8;

            BufferedImage scaledImage = new BufferedImage(scaledWidth, scaledHeight, inputImage.getType());

            Graphics2D g2d = scaledImage.createGraphics();
            g2d.drawImage(inputImage, 0, 0, scaledWidth, scaledHeight, null);

            int avg = 0;

            Color pixel;

            for (int i = 0; i < 8; i++){
                for (int j = 0; j < 8; j++){
                    pixel = new Color(scaledImage.getRGB(i,j));
                    avg += pixel.getRed();
                }
            }

            avg = avg/64;

            for (int j = 0; j < 8; j++) {
                for (int i = 0; i < 8; i++) {
                    pixel = new Color(scaledImage.getRGB(i, j));
                    ans[0] = ans[0] << 1;
                    ans[0] += pixel.getRed() > avg ? 1 : 0;
                }
            }
            for (int i = 7; i >= 0; i--){
                for (int j = 0; j < 8; j++){
                    pixel = new Color(scaledImage.getRGB(i,j));
                    ans[1] = ans[1] << 1;
                    ans[1] += pixel.getRed() > avg ? 1 : 0;
                }
            }
            for (int j = 7; j >= 0; j--){
                for (int i = 7; i >= 0; i--){
                    pixel = new Color(scaledImage.getRGB(i,j));
                    ans[2] = ans[2] << 1;
                    ans[2] += pixel.getRed() > avg ? 1 : 0;
                }
            }
            for (int i = 0; i < 8; i++){
                for (int j = 7; j >= 0; j--){
                    pixel = new Color(scaledImage.getRGB(i,j));
                    ans[3] = ans[3] << 1;
                    ans[3] += pixel.getRed() > avg ? 1 : 0;
                }
            }

            return ans;
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return ans;
    }

    private File getMatchFromList(File f, ArrayList<File> matches, int fidelity){
        try {
            BufferedImage inputImage = ImageIO.read(f);
            BufferedImage scaledImage = new BufferedImage(fidelity, fidelity, inputImage.getType());

            Graphics2D g2d = scaledImage.createGraphics();
            g2d.drawImage(inputImage, 0, 0, fidelity, fidelity, null);

            BufferedImage matchImage;
            BufferedImage scaledMatchImage = new BufferedImage(fidelity, fidelity, inputImage.getType());

            Graphics2D matchGraphics = scaledMatchImage.createGraphics();
            matchGraphics.drawImage(inputImage, 0, 0, fidelity, fidelity, null);

            File returnFile = null;

            int thissquaredistance, minsquaredistance = Integer.MAX_VALUE;

            for (File match : matches){
                thissquaredistance = getDiff(inputImage, match, fidelity);

                if (thissquaredistance < minsquaredistance){
                    minsquaredistance = thissquaredistance;
                    returnFile = match;
                }
            }

            System.out.println(minsquaredistance);
            return returnFile;
        } catch (IOException e){
            System.out.println(e.getMessage());
        }
        return null;
    }

    private int getDiff(BufferedImage inputImage, File f, int fidelity){
        try {
            BufferedImage scaledImage = new BufferedImage(fidelity, fidelity, inputImage.getType());

            Graphics2D g2d = scaledImage.createGraphics();
            g2d.drawImage(inputImage, 0, 0, fidelity, fidelity, null);

            BufferedImage matchImage = ImageIO.read(f);
            BufferedImage scaledMatchImage = new BufferedImage(fidelity, fidelity, inputImage.getType());

            Graphics2D matchGraphics = scaledMatchImage.createGraphics();
            matchGraphics.drawImage(inputImage, 0, 0, fidelity, fidelity, null);

            Color imagePixel;
            Color matchPixel;

            int squaredistance, thissquaredistance, minsquaredistance = Integer.MAX_VALUE;

            matchGraphics.drawImage(matchImage, 0, 0, fidelity, fidelity, null);

            thissquaredistance = 0;
            for (int j = 0; j < fidelity; j++) {
                for (int i = 0; i < fidelity; i++) {
                    imagePixel = new Color(scaledImage.getRGB(i, j));
                    matchPixel = new Color(scaledMatchImage.getRGB(i, j));

                    thissquaredistance += (imagePixel.getRed() - matchPixel.getRed())
                            * (imagePixel.getRed() - matchPixel.getRed());
                }
            }

            squaredistance = 0;
            for (int i = fidelity - 1; i >= 0; i--) {
                for (int j = 0; j < fidelity; j++) {
                    imagePixel = new Color(scaledImage.getRGB(i, j));
                    matchPixel = new Color(scaledMatchImage.getRGB(i, j));

                    squaredistance += (imagePixel.getRed() - matchPixel.getRed())
                            * (imagePixel.getRed() - matchPixel.getRed());
                }
            }
            thissquaredistance = Math.min(squaredistance, thissquaredistance);

            squaredistance = 0;
            for (int j = fidelity - 1; j >= 0; j--) {
                for (int i = fidelity - 1; i >= 0; i--) {
                    imagePixel = new Color(scaledImage.getRGB(i, j));
                    matchPixel = new Color(scaledMatchImage.getRGB(i, j));

                    squaredistance += (imagePixel.getRed() - matchPixel.getRed())
                            * (imagePixel.getRed() - matchPixel.getRed());
                }
            }
            thissquaredistance = Math.min(squaredistance, thissquaredistance);

            squaredistance = 0;
            for (int i = 0; i < fidelity; i++) {
                for (int j = fidelity - 1; j >= 0; j--) {
                    imagePixel = new Color(scaledImage.getRGB(i, j));
                    matchPixel = new Color(scaledMatchImage.getRGB(i, j));

                    squaredistance += (imagePixel.getRed() - matchPixel.getRed())
                            * (imagePixel.getRed() - matchPixel.getRed());
                }
            }
            thissquaredistance = Math.min(squaredistance, thissquaredistance);

            return thissquaredistance;

        } catch (IOException e){
            System.out.println(e.getMessage());
        }
        return Integer.MAX_VALUE;
    }
}
