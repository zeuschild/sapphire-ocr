/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ocr.sapphire.ann;

import java.io.Serializable;

/**
 *
 * @author Do Bich Ngoc
 */
public class Layer implements Serializable {
    private int size;
    private transient double output[];
    private transient double error[];

    private transient Layer prevLayer, nextLayer;
    private transient WeightMatrix prevWeight, nextWeight;

    public Layer() {
        // for yamlbeans to serialize
    }

    public Layer(int size) {
        this.size = size;
        output = new double[size];
        error = new double[size];
    }

    public int getSize() {
        return size;
    }

    public void setPrevLayer(Layer prevLayer) {
        this.prevLayer = prevLayer;
    }

    public void setNextLayer(Layer nextLayer) {
        this.nextLayer = nextLayer;
    }

    public void setPrevWeight(WeightMatrix prevWeight) {
        this.prevWeight = prevWeight;
    }

    public void setNextWeight(WeightMatrix nextWeight) {
        this.nextWeight = nextWeight;
    }    

    private double threshold(double sum) {
        //return 1.0 / (1 + Math.exp(-1.0 * sum));
        return Sigmoid.sigmoid(sum);
    }

    public void computeOutput(double input[]) {
        for (int i = 0; i < size; i++) {
            output[i] = input[i];
        }
    }

    public void computeOutput() {
        double sum;
        int inputSize = prevLayer.getSize();
        double input[] = prevLayer.getOutput();
        for (int i = 0; i < size; i++) {
            sum = 0;
            for (int j = 0; j < inputSize; j++) {
                sum += input[j] * prevWeight.getWeight(j, i);
            }
            output[i] = threshold(sum);
        }
    }

    public double[] getOutput() {
        return output;
    }

    public void computeError(double ideal[]) {
        for (int i = 0; i < size; i++) {
            double value = output[i];
            error[i] = value * (1 - value) * (ideal[i] - value);
        }
    }

    public void computeError() {
        double sum;
        int outputSize = nextLayer.getSize();
        double nextError[] = nextLayer.getError();
        for (int i = 0; i < size; i++) {
            sum = 0;
            for (int j = 0; j < outputSize; j++) {
                sum += nextWeight.getWeight(i, j) * nextError[j];
            }
            error[i] = output[i] * (1 - output[i]) * sum;
        }
    }

    public double[] getError() {
        return error;
    }

    public void print() {
        for (int i = 0; i < size; i++) {
            System.out.print(output[i] + " ");
        }
        System.out.println();
        for (int i = 0; i < size; i++) {
            System.out.print(error[i] + " ");
        }
        System.out.println();
    }

}

