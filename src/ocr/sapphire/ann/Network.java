/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
