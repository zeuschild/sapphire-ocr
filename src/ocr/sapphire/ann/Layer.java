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

import java.io.Serializable;
import java.util.Arrays;

/**
 *
 * @author cumeo89
 */
public abstract class Layer implements Serializable {

    protected int size;
    protected transient double output[];
    protected transient double error[];
    protected double biasWeight[];
    protected transient double deltaBiasWeight[];

    protected transient Layer prevLayer, nextLayer;
    protected transient WeightMatrix prevWeight, nextWeight;

    public Layer() {
        // for yamlbeans to serialize
    }

    public Layer(int size) {
        this.size = size;
        biasWeight = new double[size];
        initialize();
    }

    public void initialize() {
        output = new double[size];
        error = new double[size];
        deltaBiasWeight = new double[size];
        for (int i = 0; i < size; i++) {
            biasWeight[i] = Math.random()*0.1 - 0.05;
        }
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

    protected abstract double threshold(double sum);

    public void computeOutput(double input[]) {
        int length = Math.min(input.length, output.length);
        System.arraycopy(input, 0, output, 0, length);
        Arrays.fill(output, length, output.length, 0);
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
            sum += biasWeight[i];
            output[i] = threshold(sum);
        }
    }

    public double[] getOutput() {
        return output;
    }

    public abstract void computeError(double ideal[]);

    public abstract void computeError();

    public double[] getError() {
        return error;
    }

    public double[] getBiasWeight() {
        return biasWeight;
    }

    public double[] getDeltaBiasWeight() {
        return deltaBiasWeight;
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
