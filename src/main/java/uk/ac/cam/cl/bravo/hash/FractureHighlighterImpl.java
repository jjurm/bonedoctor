package uk.ac.cam.cl.bravo.hash;

import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.function.Consumer;

public class FractureHighlighterImpl implements FractureHighlighter {
    BufferedImage source;
    BufferedImage target;
    private int gradientThreshold = 0;
    private int hammingThreshold = 0;
    private HashSet<Long> sourcePixelHash;
    private boolean recomputeNeeded;
    private boolean deferComputation;
    private int sourceStartX, sourceStartY, sourceEndX, sourceEndY;
    private Consumer<BufferedImage> imageConsumer;

    /**
     * Construct a fracture highlighter that uses the image source in order to target irregularities in the image
     * passed as the target parameter.
     *
     * @param source
     * @param target
     * @param deferComputation Defers all computation of the highlights until the getHighlight function is called.
     *                         This is useful if a user might want to change their mind about a target.
     */
    public FractureHighlighterImpl(@NotNull BufferedImage source, @NotNull BufferedImage target,
                                   boolean deferComputation, Consumer<BufferedImage> imageConsumer){
        this.source = source;
        this.target = target;
        this.deferComputation = deferComputation;
        this.imageConsumer = imageConsumer;
    }

    /**
     * Set the mode to defer computation or not
     *
     * @param deferComputation
     */
    public void setMode(boolean deferComputation) {
        this.deferComputation = deferComputation;
    }

    /**
     * In the 'source' image, target the fracture based on the differences from the sample image.
     *
     * @return the 'source' image with highlights
     */
    @NotNull
    @Override
    public BufferedImage getHighlight(int startx, int starty, int endx, int endy) {

        if (recomputeNeeded){
            computeHashes();
            recomputeNeeded = false;
        }

        int diff, mindiff;

        boolean different;

        long[] rotations = new long[8];

        Color RGB;

        BufferedImage result = new BufferedImage(target.getWidth(), target.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) result.getGraphics();
        g.drawImage(target, 0, 0, null);

        for (int x = startx; x < endx; x++){
            for (int y = starty; y < endy; y++){

                long hash = pixelHash(result, x, y);

                for (int i = 0; i < 8; i++){
                    rotations[i] = hash << (8 * i);
                    rotations[i] = rotations[i] | (hash >> ((8-i)*8));
                }

                different = true;
                mindiff = 64;

                if (hammingThreshold == 0){
                    for (int i = 0; i < 8; i++) {
                        if (sourcePixelHash.contains(rotations[i])) {
                            different = false;
                            break;
                        }
                    }
                } else {
                    for (Long aPixel : sourcePixelHash) {
                        for (int i = 0; i < 8; i++) {
                            diff = Long.bitCount(rotations[i] ^ aPixel);
                            mindiff = Math.min(diff, mindiff);
                        }

                        if (mindiff <= hammingThreshold) {
                            different = false;
                            break;
                        }
                    }
                }


                if (different){
                    RGB = new Color(result.getRGB(x,y));
                    result.setRGB(x,y, new Color(RGB.getRed(), 0, 255).getRGB());
                }

            }
        }

        imageConsumer.accept(result);
        return result;
    }

    /**
     * Set the bounding rectangle in the 'source' image that you want to learn from
     *
     * @param startx
     * @param starty
     * @param endx
     * @param endy
     */
    @Override
    public void setSourcePixels(int startx, int starty, int endx, int endy) {
        sourceStartX = startx;
        sourceStartY = starty;
        sourceEndX = endx;
        sourceEndY = endy;

        computeOrDefer();
    }

    /**
     * Alter the tolerance for two pixels to be judged to be similar. A threshold of 0 is the lowest and 64 is the
     * maximum hamming distance any two pixel hashes can have. Setting the threshold to 0 (its default value) is
     * recommended for large images as it allows for many optimisations. It may be necessary to increase this value
     * if the size of the regions to be compared is small.
     *
     * @param hammingThreshold
     */
    @Override
    public void setHammingThreshold(int hammingThreshold) {
        this.hammingThreshold = hammingThreshold;
    }

    /**
     * Set the gradient that will be used to generate the hashes. Changing this can significantly affect the results.
     * The gradient should be in the range -254 to 254 to generate any highlighting but the best results will be
     * found in between -50 and 50.
     * <p>
     * A negative value will highlight the abnormalities whereas a positive one will highlight around abnormalities.
     *
     * @param gradient
     */
    @Override
    public void setGradient(int gradient) {
        if (gradient != gradientThreshold){
            gradientThreshold = gradient;
            computeOrDefer();
        }
    }

    private void computeHashes(){
        sourcePixelHash = new HashSet<>();

        for (int x = sourceStartX; x < sourceEndX; x++){
            for (int y = sourceStartY; y < sourceEndY; y++){
                sourcePixelHash.add(pixelHash(source,x,y));
            }
        }
    }

    private void computeOrDefer(){
        if (deferComputation == true){
            recomputeNeeded = true;
        } else {
            computeHashes();
        }
    }

    private long pixelHash(BufferedImage img, int x, int y){
        // return 0 if too close to edge
        if (x < 7 | y < 7 | x + 8 > img.getWidth() | y + 8 > img.getHeight()){
            return 0L;
        }


        Color pixel = new Color(img.getRGB(x,y));
        int pixelCol = pixel.getRed();
        int matchPixelCol;

        long ans = 0L;

        for (int i = 0; i < 8; i++){
            pixel = new Color(img.getRGB(x+i, y));
            matchPixelCol = pixel.getRed();
            ans = ans << 1;
            if (pixelCol + gradientThreshold >= matchPixelCol){
                ans += 1;
            }
        }
        for (int i = 0; i < 8; i++){
            pixel = new Color(img.getRGB(x+i, y+i));
            matchPixelCol = pixel.getRed();
            ans = ans << 1;
            if (pixelCol + gradientThreshold >= matchPixelCol){
                ans += 1;
            }
        }
        for (int i = 0; i < 8; i++){
            pixel = new Color(img.getRGB(x, y+i));
            matchPixelCol = pixel.getRed();
            ans = ans << 1;
            if (pixelCol + gradientThreshold >= matchPixelCol){
                ans += 1;
            }
        }
        for (int i = 0; i < 8; i++){
            pixel = new Color(img.getRGB(x-i, y+i));
            matchPixelCol = pixel.getRed();
            ans = ans << 1;
            if (pixelCol + gradientThreshold >= matchPixelCol){
                ans += 1;
            }
        }
        for (int i = 0; i < 8; i++){
            pixel = new Color(img.getRGB(x-i, y));
            matchPixelCol = pixel.getRed();
            ans = ans << 1;
            if (pixelCol + gradientThreshold >= matchPixelCol){
                ans += 1;
            }
        }
        for (int i = 0; i < 8; i++){
            pixel = new Color(img.getRGB(x-i, y-i));
            matchPixelCol = pixel.getRed();
            ans = ans << 1;
            if (pixelCol + gradientThreshold >= matchPixelCol){
                ans += 1;
            }
        }
        for (int i = 0; i < 8; i++){
            pixel = new Color(img.getRGB(x, y-i));
            matchPixelCol = pixel.getRed();
            ans = ans << 1;
            if (pixelCol + gradientThreshold >= matchPixelCol){
                ans += 1;
            }
        }
        for (int i = 0; i < 8; i++){
            pixel = new Color(img.getRGB(x+i, y-i));
            matchPixelCol = pixel.getRed();
            ans = ans << 1;
            if (pixelCol + gradientThreshold >= matchPixelCol){
                ans += 1;
            }
        }

        return ans;
    }

    public void setAllSource(){
        setSourcePixels(7,7,  source.getWidth()-7, source.getHeight()-7);
    }

}
