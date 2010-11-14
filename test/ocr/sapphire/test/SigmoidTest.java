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

import ocr.sapphire.ann.Sigmoid;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author cumeo89
 */
public class SigmoidTest {

    @Test
    public void compute() {
        assertEqual(26.5387924263);
        assertEqual(18.5387924263);
        assertEqual(15.759249823);
        assertEqual(11.592093482);
        assertEqual(9.8234982723);
        assertEqual(2.903258252);
        assertEqual(1.39820523);
        assertEqual(0.5387924263);

        assertEqual(-26.5387924263);
        assertEqual(-18.5387924263);
        assertEqual(-15.759249823);
        assertEqual(-11.592093482);
        assertEqual(-9.8234982723);
        assertEqual(-2.903258252);
        assertEqual(-1.39820523);
        assertEqual(-0.5387924263);
    }

    private double realSigmoid(double x) {
        return 1 / (1 + Math.exp(-x));
    }

    private boolean approximate(double x, double y) {
        return Math.abs(x-y) <= 0.000001;
    }

    private void assertEqual(double x) {
        Assert.assertTrue(approximate(Sigmoid.sigmoid(x), realSigmoid(x)));
    }

}
