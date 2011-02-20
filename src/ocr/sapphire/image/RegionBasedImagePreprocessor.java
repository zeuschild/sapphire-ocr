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

import com.tomgibara.CannyEdgeDetector;
import java.awt.image.WritableRaster;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Arrays;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import static ocr.sapphire.util.Utils.getBounds;

/**
 *
 * @author cumeo89
 */
public class RegionBasedImagePreprocessor extends AbstractImagePreprocessor {

    private static final byte WHITE = (byte) 255;
    private static final byte BLACK = (byte) 0;

    public RegionBasedImagePreprocessor() {
        this(4, 10);
    }

    public RegionBasedImagePreprocessor(PreprocessorConfig config) {
        this(config.getComponentCount(), config.getSeriesLength());
    }

    public RegionBasedImagePreprocessor(int componentCount, int seriesLength) {
        super(componentCount, seriesLength);
    }

    @Override
    public BufferedImage getContourImage() {
        int[] pixels = new int[width * height];
        Arrays.fill(pixels, CannyEdgeDetector.WHITE);
        for (Point[] component : componentArr) {
            for (Point p : component) {
                try {
                    pixels[(int) p.x + (int) p.y * width] = CannyEdgeDetector.BLACK;
                } catch (ArrayIndexOutOfBoundsException ex) {
                    System.out.format("(%.2f, %.2f)\n", p.x, p.y);
                }
            }
        }
        BufferedImage image = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_ARGB);
        WritableRaster raster = image.getRaster();
        raster.setDataElements(0, 0, width, height, pixels);
        return image;
    }

    @Override
	protected List<List<Point>> extractComponents(BufferedImage image) {
		List<List<Point>> components = combinedContourLabeling(image);

        // remove noise
        for (Iterator<List<Point>> it = components.iterator(); it.hasNext(); ) {
            List<Point> points = it.next();
            if (points.size() <= 4) {
                it.remove();
            }
        }

        // try to connect some contours
        boolean connected;
        do {
            connected = false;
            for (int i = 0; i < components.size()-1; i++) {
                for (int j = i+1; j < components.size(); j++) {
                    // find distance
                    double distance = Double.MAX_VALUE;
                    int k0 = 0, l0 = 0;
                    for (int k = 0; k < components.get(i).size(); k++) {
                        Point p1 = components.get(i).get(k);
                        for (int l = 0; l < components.get(j).size(); l++) {
                            Point p2 = components.get(j).get(l);
                            double d12 = d(p1, p2);
                            if (d12 < distance) {
                                distance = d12;
                                k0 = k;
                                l0 = l;
                            }
                        }
                    }
                    // connect if they are close to each other
                    if (distance < 20) {
                        for (int l = l0; l < components.get(j).size(); l++) {
                            components.get(i).add(k0, components.get(j).get(l));
                        }
                        for (int l = 0; l < l0; l++) {
                            components.get(i).add(k0, components.get(j).get(l));
                        }
                        components.remove(j);
                        connected = true;
                    }
                }
            }
        } while (connected);

        // remove inner components
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
		return components;
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

    private List<List<Point>> combinedContourLabeling(BufferedImage image) {
        List<List<Point>> contours = new ArrayList<List<Point>>();
        byte[] data = threshold(image);
        byte[] labelMap = new byte[data.length];
        Arrays.fill(labelMap, (byte) 0);
        byte regionCounter = 0;

        for (int y = 0; y < height; y++) {
            byte label = 0;
            for (int x = 0; x < width; x++) {
                if (data[x + y * width] == BLACK) { // foreground pixel
                    if (label != 0) { // continue existing region
                        labelMap[x + y * width] = label;
                    } else {
                        label = labelMap[x + y * width];
                        if (label == 0) { // new outer contour
                            regionCounter++;
                            label = regionCounter;
                            Point start = new Point(x, y);
                            contours.add(traceContour(start, 0, label, data, labelMap));
                            labelMap[x + y * width] = label;
                        }
                    }
                } else { // background pixel
                    if (label != 0) { // new inner contour
                        Point start = new Point(x - 1, y);
                        traceContour(start, 1, label, data, labelMap); // don't add inner contours
                    }
                    label = 0;
                }
            }
        }
        return contours;
    }

    /**
     * 
     * @param startPoint
     * @param startDirection
     * @param label
     * @param data
     * @param labelMap
     * @return
     */
    private List<Point> traceContour(Point startPoint, int startDirection,
            byte label, byte[] data, byte[] labelMap) {
        xc = startPoint;
        d = startDirection;
        findNextPoint(data, labelMap);
        Point secondPoint = new Point(xc);

        List<Point> contour = new ArrayList<Point>();
        contour.add(new Point(xc));

        Point xp = startPoint; // previous point
        boolean done = startPoint.equals(xc);
        while (!done) {
            labelMap[(int) xc.x + (int) xc.y * width] = label;
            xp = new Point(xc);
            d = (d + 6) % 8;
            findNextPoint(data, labelMap);
            done = (startPoint.equals(xp) && secondPoint.equals(xc));
            if (!done) {
                contour.add(new Point(xc));
            }
        }
        return contour;
    }
    private Point xc;
    private int d;
    private static final int[] DX = {1, 1, 0, -1, -1, -1, 0, 1};
    private static final int[] DY = {0, 1, 1, 1, 0, -1, -1, -1};

    private void findNextPoint(byte[] data, byte[] labelMap) {
        for (int i = 0; i < 7; i++) {
            double x = xc.x + DX[d], y = xc.y + DY[d];
            if (x < 0 || x >= width || y < 0 || y >= height) {
                d = (d + 1) % 8;
            } else if (data[(int) x + (int) y * width] == WHITE) {
                labelMap[(int) x + (int) y * width] = -1;
                d = (d + 1) % 8;
            } else {
                xc = new Point(x, y);
                return;
            }
        }
    }

    private byte[] threshold(BufferedImage image) {
        Object obj = image.getRaster().getDataElements(0, 0, width, height, null);
        byte[] data;
        if (obj instanceof byte[]) {
            data = (byte[]) obj;
            for (int i = 0; i < data.length; i++) {
                int unsigned = data[i] & 0x00ff;
                data[i] = unsigned > 20 ? WHITE : BLACK;
            }
        } else {
            int[] idata = (int[])obj;
            data = new byte[idata.length];
            for (int i = 0; i < idata.length; i++) {
                int r = (idata[i] & 0x00ff0000) >> 16;
                int g = (idata[i] & 0x0000ff00) >> 8;
                int b = (idata[i] & 0x000000ff);
                int aluminance = (r + g + b) / 3;
                data[i] = aluminance > 20 ? WHITE : BLACK;
            }
        }
        return data;
    }
}
