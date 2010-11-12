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
package ocr.sapphire;

import com.esotericsoftware.yamlbeans.YamlConfig;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import ocr.sapphire.ann.Layer;
import ocr.sapphire.ann.Network;
import ocr.sapphire.ann.OCRNetwork;
import ocr.sapphire.ann.WeightMatrix;
import ocr.sapphire.image.PreprocessorConfig;
import ocr.sapphire.sample.CharacterProcessedSample;

/**
 *
 * @author cumeo89
 */
public final class Utils {

    public static final YamlConfig DEFAULT_YAML_CONFIG = new YamlConfig();

    static {
        DEFAULT_YAML_CONFIG.setPrivateFields(true);
        DEFAULT_YAML_CONFIG.setClassTag("sample",
                CharacterProcessedSample.class);
        DEFAULT_YAML_CONFIG.setClassTag("config",
                PreprocessorConfig.class);
        DEFAULT_YAML_CONFIG.setClassTag("ocr",
                OCRNetwork.class);
        DEFAULT_YAML_CONFIG.setClassTag("network",
                Network.class);
        DEFAULT_YAML_CONFIG.setClassTag("layer",
                Layer.class);
        DEFAULT_YAML_CONFIG.setClassTag("weight",
                WeightMatrix.class);
    }

    /**
     * Returns a copy of the object, or null if the object cannot
     * be serialized.
     */
    public static <T> T copy(T orig) {
        Object obj = null;
        try {
            // Write the object out to a byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(orig);
            out.flush();
            out.close();

            // Make an input stream from the byte array and read
            // a copy of the object back in.
            ObjectInputStream in = new ObjectInputStream(
                    new ByteArrayInputStream(bos.toByteArray()));
            obj = in.readObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return (T) obj;
    }

    public static char toChar(double[] arr) {
        int v = 0;
        for (int i = 0; i < 16; i++) {
            if (arr[i] >= 0.5) {
                v += (1 << i);
            }
        }
        return (char) v;
    }

    public static double[] toDoubleArray(char ch) {
        double[] arr = new double[16];
        for (int i = 0; i < 16; i++) {
            if ((ch & (1 << i)) != 0) {
                arr[i] = 1.0;
            }
        }
        return arr;
    }
}
