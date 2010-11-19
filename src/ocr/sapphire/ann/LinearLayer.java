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
 * @author cumeo89
 */
public class LinearLayer extends Layer {

    public LinearLayer() {
    }

    public LinearLayer(int size) {
        super(size);
    }

    protected double threshold(double sum) {
        return sum > 1 ? 1 : sum < 0 ? 0 : sum;
    }

    public void computeError(double ideal[]) {
        for (int i = 0; i < size; i++) {
            error[i] = ideal[i] - output[i];
        }
    }

    public void computeError() {
        int outputSize = nextLayer.getSize();
        double nextError[] = nextLayer.getError();
        for (int i = 0; i < size; i++) {
            double sum = 0;
            for (int j = 0; j < outputSize; j++) {
                sum += nextWeight.getWeight(i, j) * nextError[j];
            }
            error[i] = sum;
        }
    }

}

