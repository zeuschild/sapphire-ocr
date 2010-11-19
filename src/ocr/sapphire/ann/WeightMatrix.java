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

/**
 *
 * @author Do Bich Ngoc
 */
public class WeightMatrix implements Serializable {

    private double weight[][];
    private transient double delta[][];

    public WeightMatrix() {
        // for yamlbeans to serialize
    }

    public WeightMatrix(int previousLayerSize, int nextLayerSize) {
        weight = new double[previousLayerSize][nextLayerSize];
        delta = new double[previousLayerSize][nextLayerSize];
        initialize();
    }

    private void initialize() {
        for (int i = 0; i < previousLayerSize(); i++) {
            for (int j = 0; j < nextLayerSize(); j++) {
                weight[i][j] = Math.random() * 0.1 - 0.05;
            }
        }
    }

    private int previousLayerSize() {
        return weight.length;
    }

    private int nextLayerSize() {
        return weight[0].length;
    }

    public void setWeight(int i, int j, double w) {
        weight[i][j] = w;
    }

    public double getWeight(int i, int j) {
        return weight[i][j];
    }

    public void setDelta(int i, int j, double d) {
        delta[i][j] = d;
    }

    public double getDelta(int i, int j) {
        return delta[i][j];
    }

    public void print() {
        for (int i = 0; i < previousLayerSize(); i++) {
            for (int j = 0; j < nextLayerSize(); j++) {
                System.out.print(weight[i][j] + " ");
            }
            System.out.println();
        }
    }

}
