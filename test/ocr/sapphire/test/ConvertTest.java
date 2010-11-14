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

import ocr.sapphire.Utils;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author cumeo89
 */
public class ConvertTest {

    @Test
    public void toDoubleArray() {
        Assert.assertArrayEquals(new double[]{
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,},
                Utils.toDoubleArray('\u0000'), 0);
        Assert.assertArrayEquals(new double[]{
                    1.0, 0, 0, 0, 0, 0, 1.0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                Utils.toDoubleArray('A'), 0);
    }

    @Test
    public void toChar() {
        Assert.assertEquals('Ắ', Utils.toChar(new double[]{
                    0.00024, 0.89272, 0.9999, 0.8, 0.1002, 0.902527, 0.202912, 0.889293,
                    0.000032, 0.6382, 1.0, 0.9852, 0.8982, 0.29052, 0.1, 0.2, }));
        Assert.assertEquals('A', Utils.toChar(new double[]{
                    0.5, 0.1, 0.2, 0.3, 0.4, 0.25, 0.8, 0.1002,
                    0.00001, 0.202912, 0.025981, 0.000032, 0.012803, 0.00232, 0.30132, 0.2125}));
    }
}
