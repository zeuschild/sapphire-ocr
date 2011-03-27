
package ocr.sapphire.image;

import static com.tomgibara.CannyEdgeDetector.BLACK;
import static com.tomgibara.CannyEdgeDetector.WHITE;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.imageio.ImageIO;

/**
 * 
 * @author cumeo89
 */
public abstract class AbstractImagePreprocessor implements ImagePreprocessor {

	protected int componentCount;
	protected int seriesLength;
	protected int width; // chiều rộng của ảnh trong lần cuối cùng gọi process
	protected int height; // chiều rộng của ảnh trong lần cuối cùng gọi process

	public AbstractImagePreprocessor(int componentCount, int seriesLength) {
		this.componentCount = componentCount;
		this.seriesLength = seriesLength;
	}

	public abstract BufferedImage getContourImage();

	@Override
	public int getMainInputCount() {
		return 4 * seriesLength;
	}
	
	@Override
	public int getAuxiliaryInputCount() {
		return 4 * seriesLength + 2;
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
	public CharacterEigen process(String path) throws IOException {
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
        normalizeComponents(components);
        return createEigen(components);
	}

	private CharacterEigen createEigen(List<List<Point>> components) {
		double[] main = fourierTransform(components.get(0));
		double[][] auxiliaries = new double[components.size()-1][];
		Point mc = getCenter(components.get(0));
		int i = -1;
		// use for each instead of indices for best performance independent of
		// concreate class of components
		for (List<Point> c : components) {
			// skip first main component
			if (i < 0) {
				i++;
				continue;
			}
			// build auxiliary array
			/* 
			 * XXX có thể cần tính độ lệch của phần phụ so với phần chính 
			 * từ giai đoạn trước
			 */
			Point ac = getCenter(c);
			auxiliaries[i] = concat(fourierTransform(c), ac.x - mc.x, ac.y
					- mc.y);
		}
		return new CharacterEigen(main, auxiliaries);
	}
	
	private Point getCenter(List<Point> points) {
		Point c = new Point(0, 0); 
		for (Point p : points) {
			c.x += p.x;
			c.y += p.y;
		}
		c.x /= points.size();
		c.y /= points.size();
		return c;
	}
	
	private double[] concat(double[] arr, double... values) {
		double[] newArr = new double[arr.length + values.length];
		System.arraycopy(arr, 0, newArr, 0, arr.length);
		System.arraycopy(values, 0, newArr, arr.length, values.length);
		return newArr;
	}
	
	private void normalizeComponents(List<List<Point>> components) {
		Collections.sort(components, new Comparator<List<Point>>() {
			@Override
			public int compare(List<Point> l1, List<Point> l2) {
				return l2.size() - l1.size();
			}
		});
		while (components.size() > componentCount) {
			components.remove(componentCount);
		}
        
		Point center = getCenter(components.get(0));
		for (List<Point> list : components) {
			
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

	protected double[] fourierTransform(List<Point> points) {
		final int L = points.size();
		final double TWO_PI_OVER_L = 2 * Math.PI / L;
		
		double[] c = new double[seriesLength * 4];
		int a_r = 0; // phan thuc cua a
		int a_i = seriesLength; // phan ao cua a
		int b_r = seriesLength*2; // phan thuc cua b
		int b_i = seriesLength*3; // phan ao cua b
		for (int n = 0; n < seriesLength; n++) {
			int k = 0;
			for (Point p : points) {
				double phi = TWO_PI_OVER_L * k * n;
				double cosFactor = Math.cos(phi);
				double sinFactor = Math.sin(phi);
				// tinh cac he so
				c[a_r+n] += cosFactor * p.x;
				c[a_i+n] += sinFactor * p.x;
				c[b_r+n] += cosFactor * p.y;
				c[b_i+n] += sinFactor * p.y;
				k++;
			}
			c[a_r+n] /= L;
			c[a_i+n] /= -L;
			c[b_r+n] /= L;
			c[b_i+n] /= -L;
		}
		return c;
	}

}
