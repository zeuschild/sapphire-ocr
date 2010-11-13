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
import ocr.sapphire.Utils;
import ocr.sapphire.image.ImagePreprocessor;
import ocr.sapphire.sample.ProcessedSample;

public class OCRNetwork implements Serializable {
    private Network network;
    private transient ImagePreprocessor preprocessor;
    private transient BufferedImage currentImage;

    private transient double input[];
    private transient double ideal[];
    private transient int inputCount, outputCount;

    private transient char result;

    public OCRNetwork() {
        preprocessor = new ImagePreprocessor(6, 10);
        network = new Network(60, 40, 40, 40, 16);
        network.setRate(0.01);
        inputCount = 60;
        outputCount = 16;
        input = new double[inputCount];
        ideal = new double[16];
    }

    public OCRNetwork(Network network, ImagePreprocessor preprocessor) {
        this.network = network;
        this.preprocessor = preprocessor;
        inputCount = preprocessor.getComponentCount() * preprocessor.getSeriesLength();
        outputCount = 16;
        input = new double[inputCount];
        ideal = new double[16];
    }

    public void setCurrentImage(BufferedImage currentImage) {
        this.currentImage = currentImage;
    }

    public void setCurrentImage(String url) {
        try {
            currentImage = ImageIO.read(new File(url));
        }
        catch (IOException ex) {

        }
    }

    public void setResult(char result) {
        this.result = result;
    }

    public void prepareInput() {
        double[][][] coefficients = preprocessor.process(currentImage);
        int componentCount = preprocessor.getComponentCount();
        int coefficientCount = preprocessor.getSeriesLength();
        double real, image, a, b;
        int k = 0;

        // Component i-th
        for(int i = 0; i < componentCount; i++) {
            // a[j], b[j]
            for(int j = 0; j < coefficientCount; j++) {
                real = coefficients[i][0][j];
                image = coefficients[i][1][j];
                a = Math.sqrt(real * real + image * image);
                real = coefficients[i][2][j];
                image = coefficients[i][3][j];
                b = Math.sqrt(real * real + image * image);
                input[k] = Math.sqrt(a * a + b * b);
                k++;
            }
        }
    }

    public void prepareIdeal() {
        ideal = Utils.toDoubleArray(result);
    }

    public char recognize() {
        network.recognize(input);
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
        for (int i = 0; i < outputCount; i++) {
            delta = ideal[i] - output[i];
            error += delta * delta;
        }
        error *= 0.5;
       return error;
    }

    public static void main(String args[]) {
        OCRNetwork net = new OCRNetwork();

        for (int i = 0; i < 20; i++) {

            net.setCurrentImage("b.png");
            net.setResult('B');
            net.prepareInput();
            net.prepareIdeal();
            net.train();

            net.setCurrentImage("a.png");
            net.setResult('A');
            net.prepareInput();
            net.prepareIdeal();
            net.train();
            System.out.println(net.getError());

//            net.setCurrentImage("c.png");
//            net.setResult('C');
//            net.prepa reInput();
//            net.prepareIdeal();
//            net.train();
        }

        net.setCurrentImage("a.png");
        net.prepareInput();
        net.prepareIdeal();
        System.out.println(net.recognize());

        net.setCurrentImage("b.png");
        net.prepareInput();
        net.prepareIdeal();
        System.out.println(net.recognize());

        net.setCurrentImage("c.png");
        net.prepareInput();
        net.prepareIdeal();
        System.out.println(net.recognize());

    }

}
