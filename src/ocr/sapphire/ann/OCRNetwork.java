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
package ocr.sapphire.ann;

/**
 *
 * @author Do Bich Ngoc
 */
import java.io.*;
import java.awt.image.*;
import javax.imageio.*;
import ocr.sapphire.util.Utils;
import ocr.sapphire.image.EdgeBasedImagePreprocessor;
import ocr.sapphire.image.ImagePreprocessor;
import ocr.sapphire.image.PreprocessorConfig;
import ocr.sapphire.image.RegionBasedImagePreprocessor;
import ocr.sapphire.sample.ProcessedSample;

public class OCRNetwork implements Serializable {

    private Network network;
    private transient ImagePreprocessor preprocessor;
    private transient BufferedImage currentImage;
    private transient double input[];
    private transient double ideal[];
    private transient char result;

    public OCRNetwork() {
        this(new PreprocessorConfig(3, 10));
    }

    public OCRNetwork(PreprocessorConfig config, int... hiddenLayers) {
        this(new RegionBasedImagePreprocessor(config), hiddenLayers);

    }

    public OCRNetwork(ImagePreprocessor preprocessor, int... hiddenLayers) {
        this.preprocessor = preprocessor;
        int[] layerSizes = new int[hiddenLayers.length+2];
        layerSizes[0] = preprocessor.getMaxInputCount();
        System.arraycopy(hiddenLayers, 0, layerSizes, 1, hiddenLayers.length);
        layerSizes[layerSizes.length-1] = Utils.getOutputCount();
        this.network = new Network(layerSizes);
        input = new double[layerSizes[0]];
        ideal = new double[layerSizes[layerSizes.length-1]];
    }

    public void setCurrentImage(BufferedImage currentImage) {
        this.currentImage = currentImage;
    }

    public void setCurrentImage(String url) throws IOException {
        currentImage = ImageIO.read(new File(url));
    }

    public void setResult(char result) {
        this.result = result;
    }

    public void prepareInput() {
        preprocessor.process(currentImage);
        input = preprocessor.getInputs();
    }

    public void prepareIdeal() {
        ideal = Utils.toDoubleArray(result);
    }

    public char recognize() {
        network.recognize(input);
//        System.out.println(Arrays.toString(input));
//        System.out.println(Arrays.toString(network.getOutput()));
        double[] output = network.getOutput();
        return (char) Utils.toChar(output);
    }

    public double[] recognize(double[] input) {
        return network.recognize(input);
    }

    /**
     * Compute error for the last call of recognize in comparison to
     * specified ideal output.
     * @param idealOutput
     * @return
     */
    public double getError(double[] idealOutput) {
        this.ideal = idealOutput;
        return getError();
    }

    public void train() {
        network.train(input, ideal);
    }

    /**
     * Train the network based on specified sample.
     * @param sample
     */
    public void train(ProcessedSample sample) {
        network.train(sample.getInputs(), sample.getOutputs());
    }

    /**
     * Return the error of the last <code>train()</code> call.
     * @return
     */
    public double getError() {
        double error = 0, delta = 0;
        double[] output = network.getOutput();
        for (int i = 0; i < network.getOutputCount(); i++) {
            delta = ideal[i] - output[i];
            error += delta * delta;
        }
        error *= 0.5;
        return error;
    }

    public Network getNetwork() {
        return network;
    }

    public static void main(String args[]) throws IOException {
        OCRNetwork net = new OCRNetwork();
        BufferedImage a = ImageIO.read(new File("a.png"));
        net.preprocessor.process(a);
        double ai[] = net.preprocessor.getInputs();
        double ao[] = Utils.toDoubleArray('A');

        BufferedImage b = ImageIO.read(new File("b.png"));
        net.preprocessor.process(b);
        double bi[] = net.preprocessor.getInputs();
        double bo[] = Utils.toDoubleArray('B');

        BufferedImage c = ImageIO.read(new File("c.png"));
        net.preprocessor.process(c);
        double ci[] = net.preprocessor.getInputs();
        double co[] = Utils.toDoubleArray('C');

        for (int i = 0; i < 2000; i++) {
            System.out.println(i);

            net.input = bi;
            net.ideal = bo;
            net.train();

            net.input = ai;
            net.ideal = ao;
            net.train();

            net.input = ci;
            net.ideal = co;
            net.train();
        }

//        System.out.println(Utils.toYaml(net));

        net.setCurrentImage("a.png");
        net.prepareInput();
        System.out.println(net.recognize());

        net.setCurrentImage("b.png");
        net.prepareInput();
        System.out.println(net.recognize());

        net.setCurrentImage("c.png");
        net.prepareInput();
        System.out.println(net.recognize());
    }
}
