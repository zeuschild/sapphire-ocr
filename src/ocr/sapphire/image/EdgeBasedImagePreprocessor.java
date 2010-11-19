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

import java.awt.image.BufferedImage;
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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import static com.tomgibara.CannyEdgeDetector.BLACK;
import static com.tomgibara.CannyEdgeDetector.WHITE;
import static ocr.sapphire.util.Utils.getBounds;

public class EdgeBasedImagePreprocessor extends ImagePreprocessor {

    public EdgeBasedImagePreprocessor() {
        this(4, 10);
    }

    public EdgeBasedImagePreprocessor(PreprocessorConfig config) {
        this(config.getComponentCount(), config.getSeriesLength());
    }

    public EdgeBasedImagePreprocessor(int componentCount, int seriesLength) {
        super(componentCount, seriesLength);
        this.detector = new CannyEdgeDetector();
        detector.setLowThreshold(0.1f);
        detector.setHighThreshold(0.2f);
    }

    private int[] findEdges(BufferedImage image) {
        detector.setSourceImage(image);
        detector.process();
        return detector.getData();
    }

    private static final int CLOSE_THRESHOLD = 10;

    /**
     * <p>Rotate the component so that the first point is the leftmost.</p>
     * <p>Normalization doesnot make difference when reverse the Fourier series
     * but neuron networks may "feel" easier to recorgnize normalized series.</p>
     * @param points
     * @return
     */
    private static Deque<Point> normalize(Deque<Point> c) {
        double leftmost = Double.MAX_VALUE;
        for (Point p : c) {
            if (p.x < leftmost) {
                leftmost = p.x;
            }
        }
        while (c.getFirst().x > leftmost) {
            c.addLast(c.removeFirst());
        }
        return c;
    }

    private Point[][] findEdgePoints(int[] edgeData) {
        List<Deque<Point>> components = new ArrayList<Deque<Point>>();
        // find close paths
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (edgeData[x + y * width] == BLACK && isBridge(edgeData, x, y)) {
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
                        join(firstPart, true, secondPart, true);
                    }
                    components.add(firstPart);
                }
            }
        }

        // remove contained components
        for (int i = 0; i < components.size() - 1; i++) {
            Rectangle r1 = getBounds(components.get(i));
            for (int j = i + 1; j < components.size();) {
                Rectangle r2 = getBounds(components.get(j));
                if (r1.contains(r2)) {
                    components.remove(j);
                } else if (r2.contains(r1)) {
                    components.set(i, components.get(j));
                    components.remove(j);
                } else {
                    j++;
                }
            }
        }

        // try to connect some paths
        int connectedCount;
        do {
            connectedCount = 0;
            for (int i = 0; i < components.size() - 1; i++) {
                for (int j = i + 1; j < components.size(); j++) {
                    Deque<Point> a = components.get(i);
                    Deque<Point> b = components.get(j);

                    double d0 = d(a.getFirst(), a.getLast()) + d(b.getFirst(), b.getLast());
                    double d1 = d(a.getFirst(), b.getFirst()) + d(a.getLast(), b.getLast());
                    double d2 = d(a.getFirst(), b.getLast()) + d(a.getLast(), b.getFirst());
                    double d3 = d(a.getFirst(), b.getFirst());
                    double d4 = d(a.getFirst(), b.getLast());
                    double d5 = d(a.getLast(), b.getFirst());
                    double d6 = d(a.getLast(), b.getLast());

                    if (d3 <= CLOSE_THRESHOLD && d3 <= d4) {
                        join(a, true, b, true);
                        components.remove(j);
                        connectedCount++;
                    } else if (d4 <= CLOSE_THRESHOLD && d4 <= d3) {
                        join(a, true, b, false);
                        components.remove(j);
                        connectedCount++;
                    } else if (d5 <= CLOSE_THRESHOLD && d5 <= d6) {
                        join(a, false, b, true);
                        components.remove(j);
                        connectedCount++;
                    } else if (d6 <= CLOSE_THRESHOLD && d6 <= d5) {
                        join(a, false, b, false);
                        components.remove(j);
                        connectedCount++;
                    } else if (d1 <= d0 && d1 <= d2) {
                        if (d3 < d6) {
                            join(a, true, b, true);
                        } else {
                            join(a, false, b, false);
                        }
                        components.remove(j);
                        connectedCount++;
                    } else if (d2 <= d0 && d2 <= d1) {
                        if (d4 < d5) {
                            join(a, true, b, false);
                        } else {
                            join(a, false, b, true);
                        }
                        components.remove(j);
                        connectedCount++;
                    }
                } // end of for j
            } // end of for i
        } while (connectedCount > 0);

        // choose (componentCount) biggest components
        SortedMap<Integer, Deque<Point>> componentMap = new TreeMap<Integer, Deque<Point>>();
        for (Deque<Point> c : components) {
            componentMap.put(-c.size(), c);
        }

        // remove noise
        boolean firstPoint = true;
        for (Iterator<Entry<Integer, Deque<Point>>> iterator =
                componentMap.entrySet().iterator();
                iterator.hasNext(); ) {
            Entry<Integer, Deque<Point>> entry = iterator.next();
            Rectangle r = getBounds(entry.getValue());
            if (r.width <= 10 && r.height <= 10) {
                if (firstPoint) {
                    firstPoint = false;
                } else {
                    iterator.remove();
                }
            }
        }

        // convert components: normalize points, to array
        int foundComponentCount = Math.min(componentCount, componentMap.size());
        componentArr = new Point[foundComponentCount][];
        Rectangle r = getBounds(componentMap.get(componentMap.firstKey()));
        for (int c = 0; c < foundComponentCount; c++) {
            int key = componentMap.firstKey();
            componentArr[c] = new Point[componentMap.get(key).size()];
            normalize(componentMap.get(key)).toArray(componentArr[c]);
            componentMap.remove(key);

            for (int i = 0; i < componentArr[c].length; i++) {
                componentArr[c][i].x = (componentArr[c][i].x - r.x) / r.width;
                componentArr[c][i].y = (componentArr[c][i].y - r.y) / r.height;
            }
        }
        return componentArr;
    }

    /**
     * Join b to a, return a.
     * @param a
     * @param afirst join at the first point of a?
     * @param b
     * @param bfirst join at the first point of b?
     * @return
     */
    private static Deque<Point> join(Deque<Point> a, boolean afirst, 
            Deque<Point> b, boolean bfirst) {
        if (!bfirst) {
            Collections.reverse((List<Point>)b);
        }
        // don't reverse a, may confuse the network
        if (afirst) {
            for (Point p : b) {
                a.addFirst(p);
            }
        } else {
            for (Point p : b) {
                a.addLast(p);
            }
        }
        return a;
    }
    
    private boolean isBridge(int[] edgeData, int x, int y) {
        Point[] points = new Point[DX.length];
        int adjacentCounter = 0;
        for (int k = 0; k < DX.length; k++) {
            int x2 = x + DX[k];
            int y2 = y + DY[k];
            if (x2 < 0 || x2 >= width || y2 < 0 || y2 >= height) {
                continue;
            }
            if (edgeData[x2 + y2 * width] == BLACK) {
                points[adjacentCounter] = new Point(x2, y2);
                adjacentCounter++;
            }
        }
        return (adjacentCounter == 2
                && (Math.abs(points[0].x - points[1].x) >= 2
                || Math.abs(points[0].y - points[1].y) >= 2));
    }

    /**
     * Compute distance from a to b
     * @param a
     * @param b
     * @return
     */
    private static double d(Point a, Point b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    private Deque<Point> findConnectedComponent(int[] edgeData, int x, int y) {
        Deque<Point> points = new LinkedList<Point>();
        Deque<Point> queue = new LinkedList<Point>();

        edgeData[x + y * width] = WHITE;
        Point initialPoint = new Point(x, y);
        points.add(initialPoint);
        queue.push(initialPoint);

        while (!queue.isEmpty()) {
            Point point = queue.removeFirst();
            for (int k = 0; k < 8; k++) {
                int x2 = (int) (point.x + DX[k]);
                int y2 = (int) (point.y + DY[k]);
                if (x2 < 0 || y2 < 0 || x2 >= width || y2 >= height) {
                    continue;
                }
                if (edgeData[x2 + y2 * width] == BLACK) {
                    edgeData[x2 + y2 * width] = WHITE;
                    Point point2 = new Point(x2, y2);
                    points.add(point2);
                    queue.addLast(point2);
                }
            }
        }
        return points;
    }

    public double[][][] process(BufferedImage image) {
        height = image.getHeight();
        width = image.getWidth();
        int[] edgeData = findEdges(image);
        componentArr = findEdgePoints(edgeData);
        coefficients = new double[componentArr.length][][];
        for (int c = 0; c < componentArr.length; c++) {
            coefficients[c] = fourierTransform(componentArr[c]);
        }
        return coefficients;
    }

    public static void main(String[] args) throws IOException {
        BufferedImage image = ImageIO.read(new File("a.png"));
        EdgeBasedImagePreprocessor preprocessor = new EdgeBasedImagePreprocessor();
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
        ImageIO.write(preprocessor.getContourImage(), "PNG", new File("edge.png"));
    }
    private static final int[] DX = {0, 1, 1, 1, 0, -1, -1, -1};
    private static final int[] DY = {-1, -1, 0, 1, 1, 1, 0, -1};
    private CannyEdgeDetector detector;

    public BufferedImage getContourImage() {
        return detector.getEdgesImage();
    }
}
