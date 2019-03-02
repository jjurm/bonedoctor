package uk.ac.cam.cl.bravo.hash;

import javafx.util.Pair;
import uk.ac.cam.cl.bravo.gui.DisplayImage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

public class PixelHasher {
    private File a, b;
    private BufferedImage imgA, imgB;

    private ArrayList<Pair<Pair<Integer,Integer>, Long>> aPixels;
    private HashSet<Long> aPixelHash;

    static int THRESHOLD = 1;
    static int K = -20;


    public PixelHasher(File f, File g){
        try {
            a = f;
            b = g;
            imgA = ImageIO.read(f);
            imgB = ImageIO.read(g);

            aPixels = new ArrayList<>();
            aPixelHash = new HashSet<>();

            //DisplayImage dispA = new DisplayImage(imgA);
            DisplayImage dispB = new DisplayImage(imgB);
        } catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    private long pixelHash(BufferedImage img, int x, int y){
        Color pixel = new Color(img.getRGB(x,y));
        int pixelCol = pixel.getRed();
        int matchPixelCol;

        long ans = 0L;

        for (int i = 0; i < 8; i++){
            pixel = new Color(img.getRGB(x+i, y));
            matchPixelCol = pixel.getRed();
            ans = ans << 1;
            if (pixelCol + K >= matchPixelCol){
                ans += 1;
            }
        }
        for (int i = 0; i < 8; i++){
            pixel = new Color(img.getRGB(x+i, y+i));
            matchPixelCol = pixel.getRed();
            ans = ans << 1;
            if (pixelCol + K >= matchPixelCol){
                ans += 1;
            }
        }
        for (int i = 0; i < 8; i++){
            pixel = new Color(img.getRGB(x, y+i));
            matchPixelCol = pixel.getRed();
            ans = ans << 1;
            if (pixelCol + K >= matchPixelCol){
                ans += 1;
            }
        }
        for (int i = 0; i < 8; i++){
            pixel = new Color(img.getRGB(x-i, y+i));
            matchPixelCol = pixel.getRed();
            ans = ans << 1;
            if (pixelCol + K >= matchPixelCol){
                ans += 1;
            }
        }
        for (int i = 0; i < 8; i++){
            pixel = new Color(img.getRGB(x-i, y));
            matchPixelCol = pixel.getRed();
            ans = ans << 1;
            if (pixelCol + K >= matchPixelCol){
                ans += 1;
            }
        }
        for (int i = 0; i < 8; i++){
            pixel = new Color(img.getRGB(x-i, y-i));
            matchPixelCol = pixel.getRed();
            ans = ans << 1;
            if (pixelCol + K >= matchPixelCol){
                ans += 1;
            }
        }
        for (int i = 0; i < 8; i++){
            pixel = new Color(img.getRGB(x, y-i));
            matchPixelCol = pixel.getRed();
            ans = ans << 1;
            if (pixelCol + K >= matchPixelCol){
                ans += 1;
            }
        }
        for (int i = 0; i < 8; i++){
            pixel = new Color(img.getRGB(x+i, y-i));
            matchPixelCol = pixel.getRed();
            ans = ans << 1;
            if (pixelCol + K >= matchPixelCol){
                ans += 1;
            }
        }

        return ans;
    }

    public void setAPixels(int startx, int starty, int endx, int endy){
        aPixels = new ArrayList<>();
        aPixelHash = new HashSet<>();

        BufferedImage aBox = imgA;

        try {
            BufferedImage read = ImageIO.read(a);
            aBox = new BufferedImage(read.getWidth(), read.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
            Graphics2D g = aBox.createGraphics();
            g.drawImage(read, 0,0, null);
        } catch (IOException e){
            System.out.println(e.getMessage());
        }

        for (int x = startx; x < endx; x++){
            for (int y = starty; y < endy; y++){
                //aPixels.add(new Pair(new Pair<>(x, y), pixelHash(imgA, x, y)));
                aPixelHash.add(pixelHash(aBox,x,y));
            }
        }
        for (int i = 0; i < endx-startx+1; i++){
            aBox.setRGB(startx+i, starty-1, new Color(255,0,0).getRGB());
            aBox.setRGB(startx+i, endy, new Color(255,0,0).getRGB());
        }

        for (int j = 0; j < endy-starty+1; j++){
            aBox.setRGB(startx-1, starty+j, new Color(255,0,0).getRGB());
            aBox.setRGB(endx, starty+j, new Color(255,0,0).getRGB());
        }

        DisplayImage displayImage = new DisplayImage(aBox);
    }

    public void showDiff(int startx, int starty, int endx, int endy){

        ArrayList<Pair<Integer, Integer>> arrList = new ArrayList<>();

        int diff, mindiff;

        boolean different;

        long[] rotations = new long[8];

        BufferedImage differenceImage = imgB;
        BufferedImage read;

        try {
            read = ImageIO.read(b);
            differenceImage = new BufferedImage(read.getWidth(), read.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
            Graphics2D g = differenceImage.createGraphics();
            g.drawImage(read, 0,0, null);
        } catch (IOException e){
            System.out.println(e.getMessage());
        }

        Color RGB;

        for (int x = startx; x < endx; x++){
            for (int y = starty; y < endy; y++){

                if (x == 135 && y == 135){
                    int u = 5;
                }

                long hash = pixelHash(imgB, x, y);

                for (int i = 0; i < 8; i++){
                    rotations[i] = hash << (8 * i);
                    rotations[i] = rotations[i] | (hash >> ((8-i)*8));
                }

                different = true;
                mindiff = 64;

                /*for (Pair<Pair<Integer, Integer>, Long> p : aPixels) {
                    long aPixel = p.getValue();

                    for (int i = 0; i < 8; i++){
                        diff = Long.bitCount(rotations[i] ^ aPixel);
                        mindiff = Math.min(diff, mindiff);
                    }

                    if (mindiff < THRESHOLD){
                        different = false;
                        break;
                    }
                }*/

                if (THRESHOLD == 1){
                    for (int i = 0; i < 8; i++) {
                        if (aPixelHash.contains(rotations[i])) {
                            different = false;
                            break;
                        }
                    }
                } else {
                    for (Long aPixel : aPixelHash) {
                        for (int i = 0; i < 8; i++) {
                            diff = Long.bitCount(rotations[i] ^ aPixel);
                            mindiff = Math.min(diff, mindiff);
                        }

                        if (mindiff < THRESHOLD) {
                            different = false;
                            break;
                        }
                    }
                }


                if (different){
                    //System.out.println(x + " " + y);
                    //arrList.add(new Pair<>(x,y));
                    RGB = new Color(imgB.getRGB(x,y));
                    differenceImage.setRGB(x,y, new Color(RGB.getRed(), 0, 255).getRGB());
                }

                if (x == startx | x+1 == endx | y == starty | y+1 == endy){
                    differenceImage.setRGB(x,y, new Color(255,0,0).getRGB());
                }
            }
        }

        DisplayImage higlights = new DisplayImage(differenceImage);
    }

    public void setAllA(){
        setAPixels(7,7, imgA.getWidth()-7, imgA.getHeight()-7);
    }

    public void scanAllB(){
        showDiff(7,7,imgB.getWidth()-7, imgB.getHeight()-7);
    }

    public void fullScan(){
        setAllA();
        scanAllB();
    }
}
