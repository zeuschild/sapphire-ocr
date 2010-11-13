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
package ocr.sapphire.test;

import ocr.sapphire.ann.Network;
import org.apache.commons.lang.ArrayUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author cumeo89
 */
public class NetworkTest {

    @Test
    public void identityFunction() {
        Network network = new Network(3, 3, 3);
        double[][] data = {
            {0.0, 0.0, 0.0},
            {1.0, 0.0, 0.0},
            {0.0, 1.0, 0.0},
            {0.0, 0.0, 1.0},
            {1.0, 1.0, 0.0},
            {0.0, 1.0, 1.0},
            {1.0, 0.0, 1.0},
            {1.0, 1.0, 1.0},};
        for (int k = 0; k < 1000000; k++) {
            for (int i = 0; i < data.length; i++) {
                network.train(data[i], data[i]);
            }
        }
        for (int i = 0; i < data.length; i++) {
            double[] output = network.recognize(data[i]);
            System.out.println(ArrayUtils.toString(output));
            Assert.assertArrayEquals(data[i], output, 0.5);
        }
    }
}
