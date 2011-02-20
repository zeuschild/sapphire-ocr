package ocr.sapphire.image;

import java.awt.image.BufferedImage;
import java.io.IOException;

public interface ImagePreprocessor {

	/**
	 * Read image from specified file and process
	 * 
	 * @param path
	 * @return
	 */
	public CharacterEigen process(String path) throws IOException;

	public CharacterEigen process(BufferedImage image);
	
	public int getMainInputCount();

	public int getAuxiliaryInputCount();

	public BufferedImage[] reverse();

	public BufferedImage reverseToSingleImage();

}