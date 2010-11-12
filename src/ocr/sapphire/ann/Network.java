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

    private int layerNumber;
    private int size[];
    private double rate = 0.2;
    private ArrayList<Layer> layer;
    private ArrayList<WeightMatrix> weight;

    public Network() {
        // for yamlbeans to serialize
    }

    public Network(int... size) {
        layerNumber = size.length;
        this.size = size;
        layer = new ArrayList<Layer>(layerNumber);
        weight = new ArrayList<WeightMatrix>(layerNumber - 1);
        initialize();
    }

    private void initialize() {
        layer.add(new Layer(size[0]));
        for (int i = 1; i < layerNumber; i++) {
            layer.add(new Layer(size[i]));
            weight.add(new WeightMatrix(size[i-1], size[i]));
        }
        layer.get(0).setNextLayer(layer.get(1));
        layer.get(0).setNextWeight(weight.get(0));
        for (int i = 1; i < layerNumber - 1; i++) {
            layer.get(i).setPrevLayer(layer.get(i - 1));
            layer.get(i).setNextLayer(layer.get(i + 1));
            layer.get(i).setPrevWeight(weight.get(i - 1));
            layer.get(i).setNextWeight(weight.get(i));
        }
        layer.get(layerNumber - 1).setPrevLayer(layer.get(layerNumber - 2));
        layer.get(layerNumber - 1).setPrevWeight(weight.get(layerNumber - 2));
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    private void feedFoward(double input[]) {
        layer.get(0).computeOutput(input);
        for (int i = 1; i < layerNumber; i++) {
            layer.get(i).computeOutput();
        }
    }

    private void backPropangation(double ideal[]) {
        layer.get(layerNumber - 1).computeError(ideal);
        for (int i = layerNumber - 2; i >= 0; i--) {
            layer.get(i).computeError();
        }
    }

    private void updateWeight() {
        int x, y;
        for (int k = 0; k < layerNumber - 1; k++) {
            x = size[k];
            y = size[k+1];
            WeightMatrix temp = weight.get(k);
            for (int i = 0; i < x; i++) {
                for(int j = 0; j < y; j++) {
                    double w = temp.getWeight(i, j);
                    w = w + rate * layer.get(k).getOutput()[i] * layer.get(k + 1).getError()[j];
                    temp.setWeight(i, j, w);
                }
            }
        }
    }

    public void train(double input[], double ideal[]) {
        feedFoward(input);
        backPropangation(ideal);
        updateWeight();
    }

    public double[] recognize(double[] input)  {
        feedFoward(input);
        return layer.get(layerNumber - 1).getOutput();
    }

    public double[] getOutput() {
        return layer.get(layerNumber - 1).getOutput();
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
        Network net = new Network(2, 10, 10, 10, 1);
        net.setRate(0.7);
        double input[]  = {1, 0};
        double ideal[] = {1};
        for (int i = 0; i < 84; i++) {
            net.feedFoward(input);
            net.backPropangation(ideal);
            net.updateWeight();
            System.out.println(net.getOutput()[0] + " ");
        }
    }

}
