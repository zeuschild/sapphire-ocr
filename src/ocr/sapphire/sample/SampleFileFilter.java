package ocr.sapphire.sample;

import java.io.File;
import java.io.FileFilter;

public class SampleFileFilter implements FileFilter {

	@Override
	public boolean accept(File file) {
		if (file.isDirectory()) {
			return false;
		}
		return file.getName().endsWith(".sample");
	}

}
