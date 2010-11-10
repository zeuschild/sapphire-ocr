package ocr.sapphire.sample;

public class CharacterProcessedSample implements ProcessedSample {

	private double[] inputs;
	private char character;
	
	public CharacterProcessedSample() {
	}

	public CharacterProcessedSample(double[] inputs, char character) {
		super();
		this.inputs = inputs;
		this.character = character;
	}

	/* (non-Javadoc)
	 * @see ocr.sapphire.sample.ProcessedSample#getInputs()
	 */
	@Override
	public double[] getInputs() {
		return inputs;
	}

	/* (non-Javadoc)
	 * @see ocr.sapphire.sample.ProcessedSample#getOutputs()
	 */
	@Override
	public double[] getOutputs() {
		double[] arr = new double[16];
		for (int i = 0; i < 16; i++) {
			if ((character & (1 << i)) != 0) {
				arr[i] = 1.0;
			}
		}
		return arr;
	}
	
	public char getCharacter() {
		return character;
	}

}
