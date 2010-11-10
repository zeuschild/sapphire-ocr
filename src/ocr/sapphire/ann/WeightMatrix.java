/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ocr.sapphire.ann;

/**
 *
 * @author Do Bich Ngoc
 */
public class WeightMatrix {
    private int previousLayerSize;
    private int nextLayerSize;
    private double weight[][];

    public WeightMatrix(int previousLayerSize, int nextLayerSize) {
        this.previousLayerSize = previousLayerSize;
        this.nextLayerSize = nextLayerSize;
        weight = new double[previousLayerSize][nextLayerSize];
        initialize();
    }

    private void initialize() {
        for (int i = 0; i < previousLayerSize; i++) {
            for (int j = 0; j < nextLayerSize; j++) {
                weight[i][j] = Math.random() * 0.1 - 0.05;
            }
        }
    }

    public void setWeight(int i, int j, double w) {
        weight[i][j] = w;
    }

    public double getWeight(int i, int j) {
        return weight[i][j];
    }

    public void print() {
        for (int i = 0; i < previousLayerSize; i++) {
            for (int j = 0; j < nextLayerSize; j++) {
                System.out.print(weight[i][j] + " ");
            }
            System.out.println();
        }
    }

}
