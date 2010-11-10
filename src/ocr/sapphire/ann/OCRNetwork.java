/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ocr.sapphire.ann;

/**
 *
 * @author Do Bich Ngoc
 */
import java.util.Arrays;
import java.io.*;
import java.awt.image.*;
import javax.imageio.*;
import ocr.sapphire.image.ImagePreprocessor;

public class OCRNetwork {
    private Network network;
    private ImagePreprocessor preprocessor;
    private BufferedImage currentImage;

    private double input[];
    private double ideal[];
    private int inputCount, outputCount;

    private char result;

    public OCRNetwork() {
        preprocessor = new ImagePreprocessor(6, 15);
        network = new Network(90, 60, 16);
        inputCount = 90;
        outputCount = 16;
        input = new double[90];
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
        long code = (int) result;
        String binString = Long.toBinaryString(code);
        int j = 15;
        for (int i = binString.length() - 1; i >= 0; i--) {
            if (binString.charAt(i) == '0') {
                ideal[j] = 0;
            }
            else {
                ideal[j] = 1;
            }
            j--;
        }
    }

    public char recognize() {
        network.recognize(input);
        System.out.println(Arrays.toString(network.getOutput()));
        double[] output = network.getOutput();
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < outputCount; i++) {
            if (output[i] < 0.5) {
                s.append("0");
            }
            else {
                s.append("1");
            }
        }
        long code = Long.valueOf(s.toString(), 2);
        return (char) code;
    }

    public void train() {
        network.train(input, ideal);
    }

    public static void main(String args[]) {
        OCRNetwork net = new OCRNetwork();

        net.setCurrentImage("a.png");
        net.setResult('A');
        net.prepareInput();
        net.prepareIdeal();
        net.train();

        net.setCurrentImage("b.png");
        net.setResult('B');
        net.prepareInput();
        net.prepareIdeal();
        net.train();

        net.setCurrentImage("c.png");
        net.setResult('C');
        net.prepareInput();
        net.prepareIdeal();
        net.train();

        net.setCurrentImage("a1.png");
        net.prepareInput();
        net.prepareIdeal();
        System.out.println(net.recognize());

        net.setCurrentImage("b1.png");
        net.prepareInput();
        net.prepareIdeal();
        System.out.println(net.recognize());

        net.setCurrentImage("c1.png");
        net.prepareInput();
        net.prepareIdeal();
        System.out.println(net.recognize());

    }



}
