package ocr.sapphire.sample;

import ocr.sapphire.Utils;

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
        return Utils.toDoubleArray(character);
    }

    public char getCharacter() {
        return character;
    }
}
