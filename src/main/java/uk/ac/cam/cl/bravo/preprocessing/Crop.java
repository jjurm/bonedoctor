package uk.ac.cam.cl.bravo.preprocessing;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

class Crop
{

    public static class CropError extends Exception{
    }

    public static BufferedImage crop(BufferedImage srcFile, BufferedImage inputFile) throws CropError {

        inputFile.getGraphics().drawImage(srcFile, 0, 0, null);

        ArrayList<Boolean> rows = new ArrayList<>();
        ArrayList<Boolean> columns = new ArrayList<>();

        for (int y=0; y<srcFile.getHeight(); y++){
            boolean row = true;
            for (int x=0; x<srcFile.getWidth(); x++){
                if (!(new Color(inputFile.getRGB(x,y)).equals(new Color(0,0,0))))
                    row = false;
            }
            rows.add(row);
        }

        for (int x=0; x<srcFile.getWidth(); x++){
            boolean col = true;
            for (int y=0; y<srcFile.getHeight(); y++){
                if (!(new Color(inputFile.getRGB(x,y)).equals(new Color(0,0,0))))
                    col = false;
            }
            columns.add(col);
        }

        int lrow = Math.max(0, lowerBound(rows));
        int urow = Math.min(srcFile.getHeight()-1, upperBound(rows));

        int lcol = Math.max(0, lowerBound(columns));
        int ucol = Math.min(srcFile.getWidth()-1, upperBound(columns));

        if (lcol==ucol || lrow==urow)
            throw new CropError();

        return inputFile.getSubimage(lcol, lrow, ucol-lcol, urow-lrow);
    }

    private static int lowerBound(ArrayList<Boolean> vals){

        for (int i=0; i<vals.size(); i++){
            if (!vals.get(i))
                return i-1;
        }
        return -2; //all black...oops
    }

    private static int upperBound(ArrayList<Boolean> vals){

        for (int i=vals.size()-1; i>=0; i--){
            if (!vals.get(i))
                return i+1;
        }
        return -1; //all black...oops
    }

}
