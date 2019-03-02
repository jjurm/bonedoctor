package uk.ac.cam.cl.bravo.gui;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
    private static int xLocation = 5;
    private static int yLocation = 50;
    private static int screenWidth = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();

    public DisplayImage(@NotNull BufferedImage img) {
        this(img, null);
    }

    public DisplayImage(BufferedImage img, @Nullable String title) {
        ImageIcon icon = new ImageIcon(img);
        JFrame frame = new JFrame();
        frame.setLayout(new FlowLayout());
        JLabel lbl = new JLabel();
        lbl.setIcon(icon);
        frame.add(lbl);
        frame.pack();
        frame.setLocation(xLocation, yLocation);
        xLocation += img.getWidth() + 11;
        if (xLocation >= screenWidth - img.getWidth()) {
            xLocation = 5;
            yLocation += 400;
        }
        frame.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent ke) {  // handler
                if (ke.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    System.exit(0);
                }
            }
        });
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle(title != null ? title : "Bone doctor");
        frame.setVisible(true);
    }

    public DisplayImage(String filename, String title) throws IOException {
        this(ImageIO.read(new File(filename)), title);
    }

    public DisplayImage(String filename) throws IOException {
        this(filename, null);
    }

    public DisplayImage(ImageSample sample) throws IOException {
        this(sample.getPath());
    }
}
