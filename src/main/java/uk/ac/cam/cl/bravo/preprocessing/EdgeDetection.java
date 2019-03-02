package uk.ac.cam.cl.bravo.preprocessing;


import java.awt.image.BufferedImage;
import java.util.*;

class EdgeDetection
{

    public static class EdgeDetectionError extends Exception{
    }

    public static ArrayList<Point2D> edges(BufferedImage srcFile, BufferedImage inputFile) throws EdgeDetectionError{

        inputFile.getGraphics().drawImage(srcFile, 0, 0, null);

        int x = srcFile.getWidth();
        int y = srcFile.getHeight();

        int[][] edgeColors = new int[x][y];
        int maxGradient = -1;

        for (int i = 1; i < x - 1; i++) {
            for (int j = 1; j < y - 1; j++) {

                //Using Sobel Edge Detection

                int val00 = Statistics.getGrayScale(inputFile.getRGB(i - 1, j - 1));
                int val01 = Statistics.getGrayScale(inputFile.getRGB(i - 1, j));
                int val02 = Statistics.getGrayScale(inputFile.getRGB(i - 1, j + 1));

                int val10 = Statistics.getGrayScale(inputFile.getRGB(i, j - 1));
                int val11 = Statistics.getGrayScale(inputFile.getRGB(i, j));
                int val12 = Statistics.getGrayScale(inputFile.getRGB(i, j + 1));

                int val20 = Statistics.getGrayScale(inputFile.getRGB(i + 1, j - 1));
                int val21 = Statistics.getGrayScale(inputFile.getRGB(i + 1, j));
                int val22 = Statistics.getGrayScale(inputFile.getRGB(i + 1, j + 1));

                int gx =  ((-1 * val00) + (0 * val01) + (1 * val02))
                        + ((-2 * val10) + (0 * val11) + (2 * val12))
                        + ((-1 * val20) + (0 * val21) + (1 * val22));

                int gy =  ((-1 * val00) + (-2 * val01) + (-1 * val02))
                        + ((0 * val10) + (0 * val11) + (0 * val12))
                        + ((1 * val20) + (2 * val21) + (1 * val22));

                double gval = Math.sqrt((gx * gx) + (gy * gy));
                int g = (int) gval;

                if(maxGradient < g) {
                    maxGradient = g;
                }

                edgeColors[i][j] = g;
            }
        }

        double scale = 255.0 / maxGradient;

        for (int i = 1; i < x - 1; i++) {
            for (int j = 1; j < y - 1; j++) {
                int edgeColor = (int) (edgeColors[i][j]/scale);
                edgeColor = 0xff000000 | (edgeColor << 16) | (edgeColor << 8) | edgeColor;
                srcFile.setRGB(i, j, edgeColor);
            }
        }

        ArrayList<Point2D> points = points(srcFile, new BufferedImage(srcFile.getWidth(), srcFile.getHeight(),
                BufferedImage.TYPE_3BYTE_BGR));

        Point2D[] pts = new Point2D[points.size()];

        for (int i =0; i<points.size(); i++)
            pts[i] = points.get(i);

        if (pts.length==0)
            throw new EdgeDetectionError();

        GrahamScan graham = new GrahamScan(pts);
        ArrayList<Point2D> hull = new ArrayList<>();
        for (Point2D p: graham.hull())
            hull.add(p);

        return hull;
    }

    public static ArrayList<Point2D> points(BufferedImage srcFile, BufferedImage inputFile){
        inputFile.getGraphics().drawImage(srcFile, 0, 0, null);

        int w = srcFile.getWidth();
        int h = srcFile.getHeight();

        ArrayList<Point2D> pts = new ArrayList<>();

        int b=0;

        for (int x =0; x<w; x++){
            for (int y = 0; y<h; y++){
                b+=Statistics.getGrayScale(inputFile.getRGB(x, y));
            }
        }

        b = (b/(w*h))+5;

        for (int x =0; x<w; x++){
            for (int y = 0; y<h; y++){
                if ((Statistics.getGrayScale(inputFile.getRGB(x, y))>b))
                    pts.add(new Point2D(x, y));
            }
        }

        return pts;
    }
}