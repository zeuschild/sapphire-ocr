package ocr.sapphire.sample;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.imageio.ImageIO;

import ocr.sapphire.sample.Sample.Type;


public final class SampleIO {

	private SampleIO() {
	}

	public static Sample read(String path) throws IOException {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(path));
			char ch = in.readLine().charAt(0);
			BufferedImage image = ImageIO.read(new File(path + ".png"));
			return new Sample(image, ch, Type.NORMAL);
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}

	public static Sample read(File file) throws IOException {
		return read(file.getAbsolutePath());
	}

	public static void write(Sample sample, String path) throws IOException {
		PrintWriter out = null;
		try {
			out = new PrintWriter(path);
			out.println(sample.getCharacter());
			ImageIO.write(sample.getImage(), "PNG", new File(path + ".png"));
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

	public static void write(Sample sample, File file) throws IOException {
		write(sample, file.getAbsolutePath());
	}

}
