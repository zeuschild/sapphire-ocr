/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ocr.sapphire.ui;

/**
 *
 * @author Do Bich Ngoc
 */
import java.awt.*;
import javax.swing.*;
import java.io.*;
import java.awt.image.*;
import javax.imageio.*;

public class ImageCanvas extends JPanel {
    private static final int WIDTH = 120;
    private static final int HEIGHT = 160;
    private BufferedImage img;
    public ImageCanvas() {
        
    }

    public void drawImage(File file) {
        img = null;
        try {
            img = ImageIO.read(file);
            int width = img.getWidth();
            int height = img.getHeight();
            if (width > WIDTH || height > HEIGHT) {
                resize();
            }
        } catch (IOException ex) {
            // Do something here
            System.out.println(ex.getMessage());
        }
        repaint();
    }

    private void resize() {
        BufferedImage resizedImage = new BufferedImage(WIDTH, HEIGHT,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resizedImage.createGraphics();
        int width = img.getWidth();
        int height = img.getHeight();
        if ((float) width / height < (float) WIDTH / HEIGHT) {
            int newWidth = (int) Math.floor((float) width / height * HEIGHT);
            g.drawImage(img, 0, 0, newWidth, HEIGHT, null);
        }
        else {
            int newHeight = (int) Math.floor((float) height / width * WIDTH);
            g.drawImage(img, 0, 0, WIDTH, newHeight, null);
        }
        img = resizedImage;
    }

    public BufferedImage getImage() {
        return img;
    }

    @Override
    public void paint(Graphics g) {
        g.clearRect(0, 0, WIDTH, HEIGHT);
        g.drawImage(img, 0, 0, null);
    }
}
