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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static com.tomgibara.CannyEdgeDetector.BLACK;
import static com.tomgibara.CannyEdgeDetector.WHITE;

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
        this.detector = new CannyEdgeDetector();
        detector.setLowThreshold(0.1f);
        detector.setHighThreshold(0.2f);
    }

    public int getSeriesLength() {
        return seriesLength;
    }

    public int getComponentCount() {
        return componentCount;
    }

    private int[] findEdges(BufferedImage image) {
        detector.setSourceImage(image);
        detector.process();
        return detector.getData();
    }

    private Point[][] findEdgePoints(int[] edgeData) {
        List<Deque<Point>> components = new ArrayList<Deque<Point>>();
        // find open paths
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int color = edgeData[x + y * width];
                if (color == BLACK) {
                    if (getAdjacentCount(edgeData, x, y) >= 2) {
                        continue;
                    }
                    Deque<Point> points = findConnectedComponent(edgeData, x, y);
                    components.add(points);
                }
            }
        }
        // find close paths
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int color = edgeData[x + y * width];
                if (color == BLACK) {
                    if (getAdjacentCount(edgeData, x, y) != 2) {
                        continue;
                    }

                    edgeData[x + y * width] = WHITE;
                    Deque<Point> firstPart = null, secondPart = null;
                    for (int k = 0; k < DX.length; k++) {
                        int x2 = x + DX[k];
                        int y2 = y + DY[k];
                        if (x2 < 0 || x2 >= width || y2 < 0 || y2 >= height) {
                            continue;
                        }
                        if (edgeData[x2 + y2 * width] == BLACK) {
                            Deque<Point> points = findConnectedComponent(edgeData, x2, y2);
                            if (firstPart == null) {
                                firstPart = points;
                            } else {
                                secondPart = points;
                            }
                        }
                    }
                    firstPart.addFirst(new Point(x, y));
                    if (secondPart != null) { // the path is not closed
                        for (Point p : secondPart) {
                            firstPart.addFirst(p);
                        }
                    }
                    components.add(firstPart);
                }
            }
        }

        // try to connect some paths
        for (int i = 0; i < components.size() - 1; i++) {
            for (int j = i + 1; j < components.size();) {
                Deque<Point> a = components.get(i);
                Deque<Point> b = components.get(j);
                int d0 = d(a.getFirst(), a.getLast()) + d(b.getFirst(), b.getLast());
                int d1 = d(a.getFirst(), b.getFirst()) + d(a.getLast(), b.getLast());
                int d2 = d(a.getFirst(), b.getLast()) + d(a.getLast(), b.getFirst());
                if (d1 <= d0 && d1 <= d2) {
                    for (Point p : b) {
                        a.addFirst(p);
                    }
                    components.remove(j);
                } else if (d2 <= d0 && d2 <= d1) {
                    for (Point p : b) {
                        a.addLast(p);
                    }
                    components.remove(j);
                } else {
                    j++;
                }
            }
        }

        // choose (componentCount) biggest components
        SortedMap<Integer, Deque<Point>> componentMap = new TreeMap<Integer, Deque<Point>>();
        for (Deque<Point> c : components) {
            componentMap.put(-c.size(), c);
        }
        int foundComponentCount = Math.min(componentCount, componentMap.size());
        Point[][] componentArr = new Point[foundComponentCount][];
        for (int c = 0; c < foundComponentCount; c++) {
            int key = componentMap.firstKey();
            componentArr[c] = new Point[componentMap.get(key).size()];
            componentMap.get(key).toArray(componentArr[c]);
            componentMap.remove(key);
        }
        return componentArr;
    }

    private int getAdjacentCount(int[] edgeData, int x, int y) {
        int adjacentCounter = 0;
        for (int k = 0; k < DX.length; k++) {
            int x2 = x + DX[k];
            int y2 = y + DY[k];
            if (x2 < 0 || x2 >= width || y2 < 0 || y2 >= height) {
                continue;
            }
            if (edgeData[x2 + y2 * width] == BLACK) {
                adjacentCounter++;
            }
        }
        return adjacentCounter;
    }

    /**
     * Compute distance from a to b
     * @param a
     * @param b
     * @return
     */
    private static int d(Point a, Point b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    private Deque<Point> findConnectedComponent(int[] edgeData, int x, int y) {
        Deque<Point> points = new LinkedList<Point>();
        Deque<Point> stack = new LinkedList<Point>();

        edgeData[x + y * width] = WHITE;
        Point initialPoint = new Point(x, y);
        points.add(initialPoint);
        stack.push(initialPoint);

        while (!stack.isEmpty()) {
            Point point = stack.pop();
            for (int k = 0; k < 8; k++) {
                int x2 = point.x + DX[k];
                int y2 = point.y + DY[k];
                if (x2 < 0 || y2 < 0 || x2 >= width || y2 >= height) {
                    continue;
                }
                if (edgeData[x2 + y2 * width] == BLACK) {
                    edgeData[x2 + y2 * width] = WHITE;
                    Point point2 = new Point(x2, y2);
                    points.add(point2);
                    stack.push(point2);
                }
            }
        }
        return points;
    }

    private double[][] fourierTransform(Point[] points, int width, int height) {
        final int N = points.length;
        double[] a_r = new double[seriesLength]; // phan thuc cua a
        double[] a_i = new double[seriesLength]; // phan ao cua a
        double[] b_r = new double[seriesLength]; // phan thuc cua b
        double[] b_i = new double[seriesLength]; // phan ao cua b
        double TWO_PI_OVER_N = 2 * Math.PI / N;

        for (int n = 0; n < seriesLength; n++) {
            for (int k = 0; k < N; k++) {
                double phi = TWO_PI_OVER_N * k * n;
                double cosFactor = Math.cos(phi);
                double sinFactor = Math.sin(phi);
                // tinh cac he so
                a_r[n] += cosFactor * points[k].x;
                a_i[n] += sinFactor * points[k].x;
                b_r[n] += cosFactor * points[k].y;
                b_i[n] += sinFactor * points[k].y;
            }
            a_r[n] = a_r[n] / N / width;
            a_i[n] = -a_i[n] / N / width;
            b_r[n] = b_r[n] / N / height;
            b_i[n] = -b_i[n] / N / height;
        }

        return new double[][]{a_r, a_i, b_r, b_i};
    }

    public double[][][] process(BufferedImage image) {
        height = image.getHeight();
        width = image.getWidth();
        int[] edgeData = findEdges(image);
        foundComponents = findEdgePoints(edgeData);
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
            int[] pixels = new int[width * height];
            Arrays.fill(pixels, WHITE);

            final int N = foundComponents[c].length;
            double a_r[] = coefficients[c][0];
            double a_i[] = coefficients[c][1];
            double b_r[] = coefficients[c][2];
            double b_i[] = coefficients[c][3];

            double TWO_PI_OVER_N = 2 * Math.PI / N;

            for (int n = 0; n < N; n++) {
                double x = 0, y = 0;
                for (int k = 0; k < seriesLength; k++) {
                    double phi = TWO_PI_OVER_N * k * n;
                    double cosFactor = Math.cos(phi);
                    double sinFactor = Math.sin(phi);
                    x += a_r[k] * cosFactor + a_i[k] * sinFactor;
                    y += b_r[k] * cosFactor + b_i[k] * sinFactor;
                }
                x *= width;
                y *= height;
                if (x >= 0 && x < width && y >= 0 && y < height) {
                    pixels[(int) x + width * (int) y] = BLACK;
                } else {
                    System.out.println("(" + x + ", " + y + "); size=(" + width + ", " + height + ")");
                }
            }

            images[c] = new BufferedImage(width, height,
                    BufferedImage.TYPE_INT_ARGB);
            WritableRaster raster = images[c].getRaster();
            raster.setDataElements(0, 0, width, height, pixels);
        }

        return images;
    }

    public BufferedImage reverseToSingleImage() {
        int[] pixels = new int[width * height];
        Arrays.fill(pixels, WHITE);

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
                    double phi = TWO_PI_OVER_N * k * n;
                    double cosFactor = Math.cos(phi);
                    double sinFactor = Math.sin(phi);
                    x += a_r[k] * cosFactor + a_i[k] * sinFactor;
                    y += b_r[k] * cosFactor + b_i[k] * sinFactor;
                }
                x *= width;
                y *= height;
                if (x >= 0 && x < width && y >= 0 && y < height) {
                    pixels[(int) x + width * (int) y] = BLACK;
                } else {
                    System.out.println("(" + x + ", " + y + "); size=(" + width + ", " + height + ")");
                }
            }
        }

        BufferedImage image = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_ARGB);
        WritableRaster raster = image.getRaster();
        raster.setDataElements(0, 0, width, height, pixels);
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
    private double[][][] coefficients; // các hệ số tìm được trong lần cuối cùng gọi process
    private CannyEdgeDetector detector;
    private Point[][] foundComponents;

    public BufferedImage getEdgeImage() {
        return detector.getEdgesImage();
    }
}
