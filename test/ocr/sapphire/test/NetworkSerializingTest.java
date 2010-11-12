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
import com.esotericsoftware.yamlbeans.YamlWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import ocr.sapphire.Utils;
import ocr.sapphire.ann.Layer;
import org.junit.Test;

/**
 * Test the serialization and deserialization of artificial neural network
 * @author cumeo89
 */
public class NetworkSerializingTest {

    @Test
    public void serializeLayer() throws YamlException {
        Layer layer = new Layer(3);
        StringWriter sw = new StringWriter();
        YamlWriter yamlWriter = new YamlWriter(sw, Utils.DEFAULT_YAML_CONFIG);
        yamlWriter.write(layer);
        yamlWriter.close();
        System.out.println(sw.toString());
    }

    @Test
    public void serializeLayerList() throws YamlException {
        ArrayList<Layer> layers = new ArrayList<Layer>();
        layers.add(new Layer(3));
        layers.add(new Layer(2));
        layers.add(new Layer(4));
        StringWriter sw = new StringWriter();
        YamlWriter yamlWriter = new YamlWriter(sw, Utils.DEFAULT_YAML_CONFIG);
        yamlWriter.write(layers);
        yamlWriter.close();
        System.out.println(sw.toString());
    }

}
