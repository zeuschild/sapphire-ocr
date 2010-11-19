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

import com.esotericsoftware.yamlbeans.YamlException;
import java.util.Arrays;
import ocr.sapphire.util.Utils;
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
        for (int k = 0; k < 5000; k++) {
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
    
    @Test
    public void identityFunction2() {
        // This is the test in the book.
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
            System.out.println(ArrayUtils.toString(output));
            Assert.assertArrayEquals(data[i], output, 0.5);
        }
    }

    @Test
    public void simplestNetwork() throws YamlException {
        Network network = new Network(1, 1, 1);
        double[][] data = {{1.0}};
        for (int k = 0; k < 20; k++) {
            for (int i = 0; i < data.length; i++) {
                System.out.println("************* (" + k + ", " + i + ") ***********");
                System.out.println(Utils.toYaml(network));
                network.train(data[i], data[i]);
                System.out.println("output: " + Arrays.toString(network.getOutput()));
            }
        }

        System.out.println(Utils.toYaml(network));
        for (int i = 0; i < data.length; i++) {
            double[] output = network.recognize(data[i]);
            System.out.println(ArrayUtils.toString(output));
            Assert.assertArrayEquals(data[i], output, 0.5);
        }
    }
}
