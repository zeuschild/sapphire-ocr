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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import javax.imageio.ImageIO;
import ocr.sapphire.image.ImagePreprocessor;
import ocr.sapphire.image.RegionBasedImagePreprocessor;
import org.junit.Test;

/**
 *
 * @author cumeo89
 */
public class FourierTransformTest {

    @Test
    public void reverseAll() throws IOException {
        File inputDir = new File("../data/samples");
        File[] files = inputDir.listFiles(new FilenameFilter() {

            public boolean accept(File dir, String name) {
                return name.endsWith(".png");
            }
        });

        ImagePreprocessor preprocessor = new RegionBasedImagePreprocessor(4, 10);
        for (int i = 0; i < files.length; i++) {
            System.out.println(i);
            BufferedImage image = ImageIO.read(files[i]);
            preprocessor.process(image);
            BufferedImage reverseImage = preprocessor.reverseToSingleImage();
            File output = new File("../data/reverse/" + files[i].getName());
            ImageIO.write(reverseImage, "PNG", output);
            File edgeFile =  new File("../data/edge/" + files[i].getName());
            ImageIO.write(preprocessor.getContourImage(), "PNG", edgeFile);
        }
    }

    private void singleTransform(String character) throws IOException {
        ImagePreprocessor preprocessor = new RegionBasedImagePreprocessor(4, 10);
        BufferedImage image = ImageIO.read(new File(character + ".png"));
        preprocessor.process(image);
        ImageIO.write(preprocessor.getContourImage(), "PNG", new File(character + "-edges.png"));
        int i = 0;
        for (BufferedImage reverseImage : preprocessor.reverse()) {
            ImageIO.write(reverseImage, "PNG", new File(character + "-reverse" + (++i) + ".png"));
        }
        ImageIO.write(preprocessor.reverseToSingleImage(), "PNG", new File(character + "-reverse.png"));
    }

//    @Test
    public void singleTransform() throws IOException {
        singleTransform("a");
//        singleTransform("b");
//        singleTransform("c");
    }

}
