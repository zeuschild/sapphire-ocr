/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ocr.sapphire.util;

/**
 *
 * @author cumeo89
 */
public interface CharacterConverter {

    char toChar(double[] arr);

    double[] toDoubleArray(char ch);

    int getOutputCount();

}
