package uk.ac.cam.cl.bravo;

import uk.ac.cam.cl.bravo.dataset.ImageSample;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class DisplayImage {

    /**
     * X location of the next window to be opened (so that if more windows are opened at once,
     * they are next to each other)
     */
    private static AtomicInteger xLocation = new AtomicInteger(5);

    public DisplayImage(BufferedImage img) {
        ImageIcon icon = new ImageIcon(img);
        JFrame frame = new JFrame();
        frame.setLayout(new FlowLayout());
        JLabel lbl = new JLabel();
        lbl.setIcon(icon);
        frame.add(lbl);
        frame.pack();
        frame.setLocation(xLocation.getAndAdd(img.getWidth() + 11), 50);
        frame.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent ke) {  // handler
                if (ke.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    System.exit(0);
                }
            }
        });
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public DisplayImage(String filename) throws IOException {
        this(ImageIO.read(new File(filename)));
    }

    public DisplayImage(ImageSample sample) throws IOException {
        this(sample.getPath());
    }
}
