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
import java.util.*;

/**
 *
 * @author Do Bich Ngoc
 */
public class Network implements Serializable {

    private int layerCount;
    private int size[];
    private boolean useLinearOutputLayer = false;
    private double rate = 0.5;
    private double momentum = 0.1;
    private ArrayList<Layer> layers;
    private ArrayList<WeightMatrix> weight;

    public Network() {
        // for yamlbeans to serialize
    }

    public Network(int... size) {
        layerCount = size.length;
        this.size = size;
        layers = new ArrayList<Layer>(layerCount);
        weight = new ArrayList<WeightMatrix>(layerCount - 1);
        initialize();
    }

    public void initialize() {
        layers.add(new SigmoidLayer(size[0]));
        for (int i = 1; i < layerCount; i++) {
            if (useLinearOutputLayer && i == layerCount - 1) {
                layers.add(new LinearLayer(size[i]));
            } else {
                layers.add(new SigmoidLayer(size[i]));
            }
            weight.add(new WeightMatrix(size[i - 1], size[i]));
        }
        layers.get(0).setNextLayer(layers.get(1));
        layers.get(0).setNextWeight(weight.get(0));
        for (int i = 1; i < layerCount - 1; i++) {
            layers.get(i).setPrevLayer(layers.get(i - 1));
            layers.get(i).setNextLayer(layers.get(i + 1));
            layers.get(i).setPrevWeight(weight.get(i - 1));
            layers.get(i).setNextWeight(weight.get(i));
        }
        layers.get(layerCount - 1).setPrevLayer(layers.get(layerCount - 2));
        layers.get(layerCount - 1).setPrevWeight(weight.get(layerCount - 2));
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public double getMomentum() {
        return momentum;
    }

    public void setMomentum(double momentum) {
        this.momentum = momentum;
    }

    private void feedFoward(double input[]) {
        layers.get(0).computeOutput(input);
        for (int i = 1; i < layerCount; i++) {
            layers.get(i).computeOutput();
        }
    }

    private void backPropagation(double ideal[]) {
        layers.get(layerCount - 1).computeError(ideal);
        for (int i = layerCount - 2; i >= 0; i--) {
            layers.get(i).computeError();
        }
    }

    private void updateWeight() {
        int x, y;
        for (int k = 0; k < layerCount - 1; k++) {
            x = size[k];
            y = size[k + 1];
            WeightMatrix temp = weight.get(k);
            // Update weight
            // from: neuron i-th of the previous layer
            // to: neuron j-th of the next layer
            for (int i = 0; i < x; i++) {
                for (int j = 0; j < y; j++) {
                    double w = temp.getWeight(i, j);
                    double d = temp.getDelta(i, j);
                    d = momentum * d + rate * layers.get(k).getOutput()[i] * layers.get(k + 1).getError()[j];
                    w = w + d;
                    temp.setWeight(i, j, w);
                    temp.setDelta(i, j, d);
                }
            }
            // Update bias weight
            double[] biasWeight = layers.get(k + 1).getBiasWeight();
            double[] deltaBiasWeight = layers.get(k + 1).getDeltaBiasWeight();
            for (int j = 0; j < y; j++) {
                deltaBiasWeight[j] = momentum * deltaBiasWeight[j] + rate * layers.get(k + 1).getError()[j];
                biasWeight[j] += deltaBiasWeight[j];
            }
        }
    }

    public void train(double input[], double ideal[]) {
        feedFoward(input);
        backPropagation(ideal);
        updateWeight();
    }

    public double[] recognize(double[] input) {
        feedFoward(input);
        return layers.get(layerCount - 1).getOutput();
    }

    public double[] getOutput() {
        return layers.get(layerCount - 1).getOutput();
    }

    public int getInputCount() {
        return layers.get(0).getSize();
    }

    public int getOutputCount() {
        return layers.get(layerCount - 1).getSize();
    }

//    public void print() {
//        for (int i = 0; i < layerNumber; i++) {
//            System.out.println("Layer " + (i + 1));
//            layer.get(i).print();
//        }
//        System.out.println();
//        for (int i = 0; i < layerNumber - 1; i++) {
//            System.out.println("Weight " + (i + 1));
//            weight.get(i).print();
//        }
//        System.out.println();
//    }
    public static void main(String args[]) {
        Network network = new Network(8, 3, 8);
        network.setRate(0.3);
        double[][] data = {
            {1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
            {0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
            {0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0},
            {0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0},
            {0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0},
            {0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0},
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0},
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0}};
        for (int k = 0; k < 5000; k++) {
            for (int i = 0; i < data.length; i++) {
                network.train(data[i], data[i]);
            }
        }
        for (int i = 0; i < data.length; i++) {
            double[] output = network.recognize(data[i]);
            System.out.println(Arrays.toString(output));
        }
    }

    public int getLayerCount() {
        return layerCount;
    }

    public ArrayList<Layer> getLayers() {
        return layers;
    }

}
