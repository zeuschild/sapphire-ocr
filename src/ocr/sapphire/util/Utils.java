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

import com.esotericsoftware.yamlbeans.YamlConfig;
import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringWriter;
import ocr.sapphire.ann.SigmoidLayer;
import ocr.sapphire.ann.Network;
import ocr.sapphire.ann.OCRNetwork;
import ocr.sapphire.ann.WeightMatrix;
import ocr.sapphire.image.Point;
import ocr.sapphire.image.PreprocessorConfig;
import ocr.sapphire.image.Rectangle;
import ocr.sapphire.sample.CharacterProcessedSample;

/**
 *
 * @author cumeo89
 */
public final class Utils {

    public static final String VIETNAMESE_CHARACTERS =
            "aAàÀảẢãÃáÁạẠăĂằẰẳẲẵẴắẮặẶâÂầẦẩẨẫẪấẤậẬbBcCdDđĐeEèÈẻẺẽẼéÉẹẸêÊềỀểỂễỄếẾệỆ"
            + "fFgGhHiIìÌỉỈĩĨíÍịỊjJkKlLmMnNoOòÒỏỎõÕóÓọỌôÔồỒổỔỗỖốỐộỘơƠờỜởỞỡỠớỚợỢpPqQrRsStTu"
            + "UùÙủỦũŨúÚụỤưƯừỪửỬữỮứỨựỰvVwWxXyYỳỲỷỶỹỸýÝỵỴzZ";
    public static final String NO_CASE_CHARACTERS =
            "cCjJoOòÒỏỎõÕóÓọỌôÔồỒổỔỗỖốỐộỘơƠờỜởỞỡỠớỚợỢpuUùÙủỦũŨúÚụỤưƯừỪửỬữỮứỨựỰvVxXzZ";
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
                SigmoidLayer.class);
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

    public static String toYaml(Object obj) throws YamlException {
        StringWriter sw = new StringWriter();
        YamlWriter yw = new YamlWriter(sw, DEFAULT_YAML_CONFIG);
        yw.write(obj);
        yw.close();
        return sw.toString();
    }

    public static Rectangle getBounds(Iterable<Point> points) {
        double x1 = Double.MAX_VALUE, y1 = Double.MAX_VALUE;
        double x2 = Double.MIN_VALUE, y2 = Double.MIN_VALUE;
        for (Point p : points) {
            if (p.x < x1) {
                x1 = p.x;
            }
            if (p.y < y1) {
                y1 = p.y;
            }
            if (p.x > x2) {
                x2 = p.x;
            }
            if (p.y > y2) {
                y2 = p.y;
            }
        }
        return new Rectangle(x1, y1, x2 - x1, y2 - y1);
    }

    private static final CharacterConverter CONVERTER = new UnicodeConverter();

    public static char toChar(double[] arr) {
        return CONVERTER.toChar(arr);
    }

    public static double[] toDoubleArray(char ch) {
        return CONVERTER.toDoubleArray(ch);
    }

    public static int getOutputCount() {
        return CONVERTER.getOutputCount();
    }

}
