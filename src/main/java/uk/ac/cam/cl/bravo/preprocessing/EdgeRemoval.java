package uk.ac.cam.cl.bravo.preprocessing;

import javafx.util.Pair;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;

public class EdgeRemoval {

    public static Pair<BufferedImage,HashSet<Point2D>> edgeRemoval(BufferedImage srcFile, BufferedImage inputFile){

        inputFile.getGraphics().drawImage(srcFile, 0, 0, null);

        BufferedImage outputFile = new BufferedImage(inputFile.getWidth(), inputFile.getHeight(), inputFile.getType());

        ArrayList<Point2D> iniEdges = EdgeDetection.edges(srcFile, inputFile);
        ArrayList<Point2D> edges = new ArrayList<>();

        for (int i=0; i<iniEdges.size(); i++){
            Point2D p1 = iniEdges.get(i);
            Point2D p2;
            if (i+1<iniEdges.size())
                p2 = iniEdges.get(i + 1);
            else
                p2 = iniEdges.get(0);
            edges.addAll(getLine(p1, p2));
        }

        boolean circle = circleTest(edges, srcFile.getWidth()/2, srcFile.getHeight()/2);

        HashSet<Point2D> band = new HashSet<>();
        for (Point2D p: edges)
            band.addAll(getBand(p, srcFile.getWidth(), srcFile.getHeight()));

        for (int x = 0; x < srcFile.getWidth(); x++) {
            for (int y = 0; y < srcFile.getHeight(); y++) {
                int rgba = inputFile.getRGB(x, y);
                Color col = new Color(rgba, true);
                if (edges.contains(new Point2D(x,y)))
                    col = new Color(0,255,0);

                outputFile.setRGB(x, y, col.getRGB());
            }
        }

        if (circle) {
            outputFile = Inversion.invertImage(outputFile, new BufferedImage(srcFile.getWidth(),
                    srcFile.getHeight(), BufferedImage.TYPE_3BYTE_BGR));
        }

        return new Pair<>(outputFile,band);
    }

    public static Pair<HashSet<Point2D>, HashSet<Point2D>> insideOutside(BufferedImage srcFile,
                                                                         BufferedImage inputFile) {

        inputFile.getGraphics().drawImage(srcFile, 0, 0, null);
        //edges in outside
        HashSet outside = new HashSet<Point2D>();
        HashSet inside = new HashSet<Point2D>();


        for (int y=0; y<srcFile.getHeight(); y++){
            boolean out = true;
            boolean prevGreen = false;
            for (int x =0; x<srcFile.getWidth(); x++){
                int rgba = inputFile.getRGB(x, y);
                Color col = new Color(rgba, true);
                boolean green = (col.equals(new Color(0,255,0))) ||
                        (col.equals(new Color(255,0, 255)));
                if (green) {
                    outside.add(new Point2D(x,y));
                    if (!prevGreen)
                        out=!out;
                    prevGreen = true;
                }
                else {
                    if (out){
                        outside.add(new Point2D(x,y));
                    }
                    else{
                        inside.add(new Point2D(x,y));
                    }
                    prevGreen = false;
                }
            }
            if (!out){ //can never end inside since a convex hull is always closed
                for (int x =0; x<srcFile.getWidth(); x++) {
                    outside.add(new Point2D(x, y));
                    inside.remove(new Point2D(x, y));
                }
            }
        }

        return new Pair<>(inside,outside);

    }

    public static HashSet inside(Pair<HashSet<Point2D>, HashSet<Point2D>> insideOutside){

        return insideOutside.getKey();
    }

    public static HashSet outside(Pair<HashSet<Point2D>, HashSet<Point2D>> insideOutside){

        return insideOutside.getValue();
    }

    public static BufferedImage colourBand(BufferedImage srcFile, BufferedImage inputFile, HashSet<Point2D> band){

        inputFile.getGraphics().drawImage(srcFile, 0, 0, null);

        BufferedImage outputFile = new BufferedImage(inputFile.getWidth(), inputFile.getHeight(), inputFile.getType());

        for (int x = 0; x < srcFile.getWidth(); x++) {
            for (int y = 0; y < srcFile.getHeight(); y++) {
                int rgba = inputFile.getRGB(x, y);
                Color col = new Color(rgba, true);
                if (band.contains(new Point2D(x,y)))
                    col = new Color(0,0,0);
                outputFile.setRGB(x, y, col.getRGB());
            }
        }

        return outputFile;

    }

    private static HashSet<Point2D> getLine(Point2D p1, Point2D p2){
        HashSet<Point2D> line  = new HashSet<>();
        if (p1.x() == p2.x()){
            int y1 = (int)Math.min(p1.y(), p2.y());
            int y2 = (int)Math.max(p1.y(), p2.y());
            for (int i =y1; i<=y2; i++)
                line.add(new Point2D((int)p1.x(), i));
            return line;
        }
        if (p1.y() == p2.y()){
            int x1 = (int)Math.min(p1.x(), p2.x());
            int x2 = (int)Math.max(p1.x(), p2.x());
            for (int i =x1; i<=x2; i++)
                line.add(new Point2D(i, (int)p1.y()));
            return line;
        }
        //y  = mx +c
        double m = (p2.y()-p1.y())/(p2.x()-p1.x());
        double c = p1.y() - m*p1.x();
        if (p1.x()<p2.x()){
            for (int i =(int)p1.x(); i<=(int)p2.x(); i++)
                line.add(new Point2D(i, (int)(m*i+c)));
        }
        else{
            for (int i =(int)p1.x(); i>=(int)p2.x(); i--)
                line.add(new Point2D(i, (int)(m*i+c)));
        }

        double my = (p2.x()-p1.x())/(p2.y()-p1.y());
        double cy = p1.x() - my*p1.y();
        if (p1.y()<p2.y()){
            for (int i =(int)p1.y(); i<=(int)p2.y(); i++)
                line.add(new Point2D((int)(my*i+cy), i));
        }
        else{
            for (int i =(int)p1.y(); i>=(int)p2.y(); i--)
                line.add(new Point2D((int)(my*i+cy), i));
        }

        return line;
    }

    private static HashSet<Point2D> getBand(Point2D p, int w, int h){

        int t = 10;

        int lx = (int)Math.max(p.x()-t, 0);
        int rx = (int)Math.min(p.x()+t, w);

        int by = (int)Math.max(p.y()-t, 0);
        int ty = (int)Math.min(p.y()+t, h);

        HashSet<Point2D> band = new HashSet<>();

        for (int i = lx; i<=rx; i++){
            for (int j = by; j<=ty; j++)
                band.add(new Point2D(i,j));
        }

        return band;

    }

    private static boolean circleTest(ArrayList<Point2D> edges, int x, int y) {

        ArrayList<Double> dist = new ArrayList<>();

        for (Point2D e: edges)
            dist.add(Math.sqrt(Math.pow(e.x()-x,2)+Math.pow(e.y()-y,2)));


        return (Statistics.var(dist)<100);

    }
}
