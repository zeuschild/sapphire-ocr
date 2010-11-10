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
import java.awt.event.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;

public class DrawingCanvas extends JPanel {
    private static final int WIDTH = 120;
    private static final int HEIGHT = 160;
    private BufferedImage img;

    private static Color[] COLOR = {Color.black, Color.white};
    private static int[] SIZE = {7, 15};

    private int x, y;
    private int index = 0;

    public DrawingCanvas() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                x = e.getX();
                y = e.getY();
                if ((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
                    index = 1;
                }
                else {
                    index = 0;
                }
                draw(x, y);
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                x = e.getX();
                y = e.getY();
                if ((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
                    index = 1;
                }
                else {
                    index = 0;
                }
                draw(x, y);
            }
        });
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

    private void draw(int x, int y) {
        if (x <= WIDTH && y <= HEIGHT) {
            if (img == null) {
                initImage();
            }
            Graphics g = img.createGraphics();
            g.setColor(COLOR[index]);
            g.fillOval(x, y, SIZE[index], SIZE[index]);
            repaint();
        }
    }

    private void initImage() {
        img = new BufferedImage(WIDTH, HEIGHT,BufferedImage.TYPE_INT_ARGB);
        Graphics g = img.createGraphics();
        g.setColor(Color.white);
        g.fillRect(0, 0, WIDTH, HEIGHT);
    }

    public BufferedImage getImage() {
        return img;
    }

    public void clear() {
        img = null;
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (img == null) {
            initImage();
        }
        g.drawImage(img, 0, 0, null);
    }
}
