/*
 * Copyright Sapphire-group 2010
 *
 * This file is part of sapphire-ocr.
 *
 * sapphire-ocr is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sapphire-ocr is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with sapphire-ocr.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package ocr.sapphire.image;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Deque;
import java.util.LinkedList;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import org.apache.commons.lang.ArrayUtils;

import com.tomgibara.CannyEdgeDetector;

public class ImagePreprocessor {

    private int componentCount;
    private int seriesLength;

    public ImagePreprocessor() {
        this(3, 15);
    }

    public ImagePreprocessor(int componentCount, int seriesLength) {
        super();
        this.componentCount = componentCount;
        this.seriesLength = seriesLength;
    }

    public int getSeriesLength() {
        return seriesLength;
    }

    public int getComponentCount() {
        return componentCount;
    }

    private BufferedImage findEdges(BufferedImage image) {
        CannyEdgeDetector detector = new CannyEdgeDetector();
        detector.setLowThreshold(0.2f);
        detector.setHighThreshold(0.9f);
        detector.setSourceImage(image);
        detector.process();
        return detector.getEdgesImage();
    }

    private Point[][] findEdgePoints(BufferedImage edgeImage) {
        SortedMap<Integer, Deque<Point>> components = new TreeMap<Integer, Deque<Point>>();
        WritableRaster raster = edgeImage.getRaster();
        height = edgeImage.getHeight();
        width = edgeImage.getWidth();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int color = raster.getSample(x, y, 0);
                if (color >= 255) {
                    Deque<Point> points = findConnectedComponent(raster, x, y);
                    components.put(-points.size(), points);
                }
            }
        }

        int foundComponentCount = Math.min(componentCount, components.size());
        Point[][] componentArr = new Point[foundComponentCount][];
        for (int c = 0; c < foundComponentCount; c++) {
            int key = components.firstKey();
            componentArr[c] = new Point[components.get(key).size()];
            components.get(key).toArray(componentArr[c]);
            components.remove(key);
        }
        return componentArr;
    }

    private Deque<Point> findConnectedComponent(WritableRaster raster, int x, int y) {
        Deque<Point> points = new LinkedList<Point>();

        Deque<Point> stack = new LinkedList<Point>();
        stack.push(new Point(x, y));

        while (!stack.isEmpty()) {
            // add to closer end
            Point point = stack.pop();
            if (points.size() >= 2) {
                if (distance2(point, points.getFirst()) < distance2(point,
                        points.getLast())) {
                    points.addFirst(point);
                } else {
                    points.addLast(point);
                }
            } else {
                points.add(point);
            }
            raster.setSample(point.x, point.y, 0, 0);

            for (int k = 0; k < 8; k++) {
                int x2 = point.x + DX[k];
                int y2 = point.y + DY[k];
                if (x2 < 0 || y2 < 0 || x2 >= raster.getWidth()
                        || y2 >= raster.getHeight()) {
                    continue;
                }
                if (raster.getSample(x2, y2, 0) >= 255) {
                    raster.setSample(x2, y2, 0, 0);
                    stack.push(new Point(x2, y2));
                }
            }
        }
        return points;
    }

    private long distance2(Point a, Point b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    private double[][] fourierTransform(Point[] points, int width, int height) {
        final int N = points.length;
        double[] a_r = new double[seriesLength]; // phan thuc cua a
        double[] a_i = new double[seriesLength]; // phan ao cua a
        double[] b_r = new double[seriesLength]; // phan thuc cua b
        double[] b_i = new double[seriesLength]; // phan ao cua b
        double TWO_PI_OVER_N = 2 * Math.PI / N;

        for (int k = 0; k < seriesLength; k++) {
            for (int n = 0; n < N; n++) {
                long kn = k * n;
                double cosFactor = Math.cos(TWO_PI_OVER_N * kn);
                double sinFactor = Math.sin(TWO_PI_OVER_N * kn);
                // chuan hoa toa do
                double x = points[n].x / (double) width;
                double y = points[n].y / (double) height;
                // tinh cac he so
                a_r[k] += cosFactor * x;
                a_i[k] += sinFactor * x;
                b_r[k] += cosFactor * y;
                b_i[k] += sinFactor * y;
            }
            a_r[k] = a_r[k] / N;
            a_i[k] = -a_i[k] / N;
            b_r[k] = b_r[k] / N;
            b_i[k] = -b_i[k] / N;
        }

        return new double[][]{a_r, a_i, b_r, b_i};
    }

    public double[][][] process(BufferedImage image) {
        edgeImage = findEdges(image);
        foundComponents = findEdgePoints(edgeImage);
        coefficients = new double[foundComponents.length][][];
        for (int c = 0; c < foundComponents.length; c++) {
            coefficients[c] = fourierTransform(foundComponents[c],
                    image.getWidth(), image.getHeight());
        }
        return coefficients;
    }

    public BufferedImage[] reverse() {
        BufferedImage[] images = new BufferedImage[foundComponents.length];

        for (int c = 0; c < foundComponents.length; c++) {
            images[c] = new BufferedImage(width, height,
                    BufferedImage.TYPE_BYTE_BINARY);
            WritableRaster raster = images[c].getRaster();
            final int N = foundComponents[c].length;
            double a_r[] = coefficients[c][0];
            double a_i[] = coefficients[c][1];
            double b_r[] = coefficients[c][2];
            double b_i[] = coefficients[c][3];

            double TWO_PI_OVER_N = 2 * Math.PI / N;

            for (int n = 0; n < N; n++) {
                double x = 0, y = 0;
                for (int k = 0; k < seriesLength; k++) {
                    long kn = k * n;
                    double cosFactor = Math.cos(TWO_PI_OVER_N * kn);
                    double sinFactor = Math.sin(TWO_PI_OVER_N * kn);
                    x += (a_r[k] * cosFactor - a_i[k] * sinFactor) * width;
                    y += (b_r[k] * cosFactor - b_i[k] * sinFactor) * height;
                }
                if (x >= 0 && x < width && y >= 0 && y < height) {
                    raster.setSample((int) x, (int) y, 0, 255);
                } else {
                    System.out.println("(" + x + ", " + y + "); size=(" + width + ", " + height + ")");
                }
            }
        }

        return images;
    }

    public BufferedImage reverseToSingleImage() {
        BufferedImage image = new BufferedImage(width, height,
                BufferedImage.TYPE_BYTE_BINARY);
        WritableRaster raster = image.getRaster();

        for (int c = 0; c < foundComponents.length; c++) {
            final int N = foundComponents[c].length;
            double a_r[] = coefficients[c][0];
            double a_i[] = coefficients[c][1];
            double b_r[] = coefficients[c][2];
            double b_i[] = coefficients[c][3];

            double TWO_PI_OVER_N = 2 * Math.PI / N;

            for (int n = 0; n < N; n++) {
                double x = 0, y = 0;
                for (int k = 0; k < seriesLength; k++) {
                    long kn = k * n;
                    double cosFactor = Math.cos(TWO_PI_OVER_N * kn);
                    double sinFactor = Math.sin(TWO_PI_OVER_N * kn);
                    x += (a_r[k] * cosFactor - a_i[k] * sinFactor) * width;
                    y += (b_r[k] * cosFactor - b_i[k] * sinFactor) * height;
                }
                if (x >= 0 && x < width && y >= 0 && y < height) {
                    raster.setSample((int) x, (int) y, 0, 255);
                } else {
                    System.out.println("(" + x + ", " + y + "); size=(" + width + ", " + height + ")");
                }
            }
        }

        return image;
    }

    public static void main(String[] args) throws IOException {
        BufferedImage image = ImageIO.read(new File("a.png"));
        ImagePreprocessor preprocessor = new ImagePreprocessor();
        double[][][] coefficients = preprocessor.process(image);
        BufferedImage[] reversedImages = preprocessor.reverse();
        for (int c = 0; c < preprocessor.getComponentCount(); c++) {
            System.out.println("component " + (c + 1));
            System.out.println("\treal(a): "
                    + ArrayUtils.toString(coefficients[c][0]));
            System.out.println("\timagine(a): "
                    + ArrayUtils.toString(coefficients[c][1]));
            System.out.println("\treal(b): "
                    + ArrayUtils.toString(coefficients[c][2]));
            System.out.println("\timagine(b): "
                    + ArrayUtils.toString(coefficients[c][3]));
            ImageIO.write(reversedImages[c], "PNG", new File("reverse" + c
                    + ".png"));
        }
        ImageIO.write(preprocessor.getEdgeImage(), "PNG", new File("edge.png"));
    }
    private static final int[] DX = {0, 1, 0, -1, 1, 1, -1, -1};
    private static final int[] DY = {-1, 0, 1, 0, -1, 1, 1, -1};
    private int width; // chiều rộng của ảnh trong lần cuối cùng gọi process
    private int height; // chiều rộng của ảnh trong lần cuối cùng gọi process
    private double[][][] coefficients; // các hệ số tìm được trong lần cuối cùng
    // gọi process
    private BufferedImage edgeImage;
    private Point[][] foundComponents;

    public BufferedImage getEdgeImage() {
        return edgeImage;
    }
}
