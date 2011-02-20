
package ocr.sapphire.image;

import java.awt.image.WritableRaster;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import static com.tomgibara.CannyEdgeDetector.BLACK;
import static com.tomgibara.CannyEdgeDetector.WHITE;
import static ocr.sapphire.util.Utils.getBounds;

/**
 * 
 * @author cumeo89
 */
public abstract class AbstractImagePreprocessor implements ImagePreprocessor {

	protected int componentCount;
	protected int seriesLength;
	protected int width; // chiều rộng của ảnh trong lần cuối cùng gọi process
	protected int height; // chiều rộng của ảnh trong lần cuối cùng gọi process
	protected double[][][] coefficients; // các hệ số tìm được trong lần cuối
											// cùng gọi process
	protected Point[][] componentArr;

	public AbstractImagePreprocessor(int componentCount, int seriesLength) {
		this.componentCount = componentCount;
		this.seriesLength = seriesLength;
	}

	public abstract BufferedImage getContourImage();

	public int getInputCount() {
		return coefficients.length * 4 * seriesLength;
	}

	public double[] getInputs() {
		double[] inputs = new double[getInputCount()];
		int index = 0;
		for (int i = 0; i < coefficients.length; i++) {
			double[][] component = coefficients[i];
			for (int s = 0; s < 4; s++) {
				for (int k = 0; k < seriesLength; k++) {
					inputs[index++] = component[s][k];
				}
			}
		}
		return inputs;
	}

	public int getMaxInputCount() {
		return componentCount * 4 * seriesLength;
	}

	public int getSeriesLength() {
		return seriesLength;
	}

	public int getComponentCount() {
		return componentCount;
	}

	/* (non-Javadoc)
	 * @see ocr.sapphire.image.ImagePreprocessor#process(java.lang.String)
	 */
	@Override
	public double[][][] process(String path) throws IOException {
		return process(ImageIO.read(new File(path)));
	}
	
	/* (non-Javadoc)
	 * @see ocr.sapphire.image.ImagePreprocessor#process(java.awt.image.BufferedImage)
	 */
	@Override
	public CharacterEigen process(BufferedImage image) {
        height = image.getHeight();
        width = image.getWidth();
        List<List<Point>> components = extractComponents(image);

        SortedMap<Integer, List<Point>> componentMap = new TreeMap<Integer, List<Point>>();
        for (List<Point> c : components) {
            componentMap.put(-c.size(), c);
        }

        int usedComponentCount = Math.min(componentCount, componentMap.size());
        componentArr = new Point[usedComponentCount][];
        coefficients = new double[usedComponentCount][][];
        Rectangle r = getBounds(componentMap.get(componentMap.firstKey()));
        for (int c = 0; c < usedComponentCount; c++) {
            int key = componentMap.firstKey();
            componentArr[c] = new Point[componentMap.get(key).size()];
            componentMap.get(key).toArray(componentArr[c]);

            Point[] points = new Point[componentMap.get(key).size()];
            for (int i = 0; i < points.length; i++) {
                Point p = componentMap.get(key).get(i);
                points[i] = new Point((p.x - r.x) / r.width, (p.y - r.y) / r.height);
            }
            coefficients[c] = fourierTransform(points);

            componentMap.remove(key);
        }
	}

	protected abstract List<List<Point>> extractComponents(BufferedImage image);
	
	/* (non-Javadoc)
	 * @see ocr.sapphire.image.ImagePreprocessor#reverse()
	 */
	@Override
	public BufferedImage[] reverse() {
		BufferedImage[] images = new BufferedImage[componentArr.length];

		for (int c = 0; c < componentArr.length; c++) {
			int[] pixels = new int[width * height];
			Arrays.fill(pixels, WHITE);

			final int N = componentArr[c].length;
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
				x = width / 3 + x * width / 3;
				y = height / 3 + y * height / 3;
				if (x >= 0 && x < width && y >= 0 && y < height) {
					pixels[(int) x + width * (int) y] = BLACK;
				} else {
					System.out.println("(" + x + ", " + y + "); size=(" + width
							+ ", " + height + ")");
				}
			}

			images[c] = new BufferedImage(width, height,
					BufferedImage.TYPE_INT_ARGB);
			WritableRaster raster = images[c].getRaster();
			raster.setDataElements(0, 0, width, height, pixels);
		}

		return images;
	}

	/* (non-Javadoc)
	 * @see ocr.sapphire.image.ImagePreprocessor#reverseToSingleImage()
	 */
	@Override
	public BufferedImage reverseToSingleImage() {
		int[] pixels = new int[width * height];
		Arrays.fill(pixels, WHITE);

		for (int c = 0; c < componentArr.length; c++) {
			final int N = componentArr[c].length;
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
				x = width / 3 + x * width / 3;
				y = height / 3 + y * height / 3;
				if (x >= 0 && x < width && y >= 0 && y < height) {
					pixels[(int) x + width * (int) y] = BLACK;
				} else {
					// System.out.println("(" + x + ", " + y + "); size=(" +
					// width + ", " + height + ")");
				}
			}
		}

		BufferedImage image = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);
		WritableRaster raster = image.getRaster();
		raster.setDataElements(0, 0, width, height, pixels);
		return image;
	}

	protected double[][] fourierTransform(Point[] points) {
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
			a_r[n] = a_r[n] / N;
			a_i[n] = -a_i[n] / N;
			b_r[n] = b_r[n] / N;
			b_i[n] = -b_i[n] / N;
		}
		return new double[][] { a_r, a_i, b_r, b_i };
	}

}
