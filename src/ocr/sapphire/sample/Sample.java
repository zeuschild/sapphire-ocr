package ocr.sapphire.sample;

import java.awt.image.BufferedImage;

public class Sample {

	private BufferedImage image;
	private char character;
	private Type type = Type.NORMAL;

	public Sample(BufferedImage image, char character, Type type) {
		super();
		this.image = image;
		this.character = character;
		this.type = type;
	}

	public enum Type {
		NORMAL
	}

	public BufferedImage getImage() {
		return image;
	}

	public char getCharacter() {
		return character;
	}

	public Type getType() {
		return type;
	}
	
}
