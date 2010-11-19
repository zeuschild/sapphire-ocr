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
package ocr.sapphire.util;

import static ocr.sapphire.util.Utils.VIETNAMESE_CHARACTERS;
import static ocr.sapphire.util.Utils.NO_CASE_CHARACTERS;

/**
 *
 * @author cumeo89
 */
public class CompactConverter implements CharacterConverter {

    public char toChar(double[] arr) {
        int index = 0;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] >= 0.5) {
                index |= 1 << i;
            }
        }
        if (index < 0) {
            index = 0;
        }
        if (index >= VIETNAMESE_CHARACTERS.length()) {
            index = VIETNAMESE_CHARACTERS.length() - 1;
        }
        char ch = VIETNAMESE_CHARACTERS.charAt(index);
        if (NO_CASE_CHARACTERS.indexOf(ch) >= 0) {
            ch = Character.toLowerCase(ch);
        }
        return ch;

    }

    public double[] toDoubleArray(char ch) {
        double[] arr = new double[8];
        if (NO_CASE_CHARACTERS.indexOf(ch) >= 0) {
            ch = Character.toLowerCase(ch);
        }
        int index = VIETNAMESE_CHARACTERS.indexOf(ch);
        if (index >= 0) {
            for (int i = 0; i < 8; i++) {
                if ((index & (1 << i)) != 0) {
                    arr[i] = 1.0;
                } else {
                    arr[i] = 0.0;
                }
            }
        }
        return arr;
    }

    public int getOutputCount() {
        return 8;
    }
}
