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
public class SigmoidLayer extends Layer {

    public SigmoidLayer() {
    }

    public SigmoidLayer(int size) {
        super(size);
    }

    @Override
    protected double threshold(double sum) {
        return Sigmoid.sigmoid(sum);
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

}