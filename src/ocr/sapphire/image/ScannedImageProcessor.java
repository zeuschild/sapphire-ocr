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
package ocr.sapphire.image;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

import ocr.sapphire.sample.Sample;
import ocr.sapphire.sample.SampleIO;
import ocr.sapphire.sample.Sample.Type;

public class ScannedImageProcessor {

    public static final String IMAGE_DIR = "../data/images";
    public static final String SAMPLE_DIR = "../data/samples";

    public static void main(String[] args) throws IOException {
        long startTime = System.currentTimeMillis();
        File dir = new File(IMAGE_DIR);
        int counter = 0;
        prepareDirectory(SAMPLE_DIR);
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                continue;
            }
            BufferedImage image = ImageIO.read(file);
            image = preprocess(image);
            System.out.println("Processing: " + counter);
            for (Sample sample : process(file, image)) {
                SampleIO.write(sample, String.format("%s/%s-%c-%d.sample",
                        SAMPLE_DIR, file.getName(),
                        sample.getCharacter(), (int) sample.getCharacter()));
                ++counter;
            }
        }
        long stopTime = System.currentTimeMillis();
        System.out.println("Total: " + counter);
        System.out.println("Time (ms): " + (stopTime - startTime));
    }

    /**
     * Make sure the directory exists and is empty
     * @param dir
     * @return
     */
    private static File prepareDirectory(String path) {
        File dir = new File(path);
        if (dir.exists()) {
            for (File file : dir.listFiles()) {
                file.delete();
            }
        } else {
            dir.mkdir();
        }
        return dir;
    }

    private static BufferedImage preprocess(BufferedImage image) {
        return image;
    }

    private static Sample[] process(File file, BufferedImage image) {
        List<Sample> samples = new LinkedList<Sample>();
        boolean upperCase = !isLowerCase(image);
        for (int row = 0; row < MAP.length; row++) {
            for (int col = 0; col < MAP[row].length; col++) {
                char ch = MAP[row][col];
                if (ch == ' ' || isJunk(file, ch)) {
                    continue;
                }
                if (upperCase) {
                    ch = Character.toUpperCase(ch);
                }
                BufferedImage data = extractCharacter(image, row, col);
                samples.add(new Sample(data, ch, Type.NORMAL));
            }
        }
        Sample[] sampleArr = new Sample[samples.size()];
        return samples.toArray(sampleArr);
    }

    private static boolean isJunk(File file, char ch) {
        for (Junk junk : junks) {
            if (!junk.fileName.equalsIgnoreCase(file.getName())) {
                continue;
            }
            if (junk.fromChar == junk.toChar) {
                if (junk.fromChar == ch) {
                    return true;
                }
            } else {
                int index = getMapIndex(ch);
                if (junk.fromIndex <= index && index <= junk.toIndex) {
                    return true;
                }
            }
        }
        return false;
    }

    private static BufferedImage extractCharacter(BufferedImage image, int row,
            int col) {
        final int WIDTH = 44, HEIGHT = 58;
        final int[] X1 = {39, 137, 236, 338, 435, 534, 633};
        final int[] Y1 = {123, 263, 412, 561, 710, 870};
        return image.getSubimage(f(X1[col]), f(Y1[row]), f(WIDTH), f(HEIGHT));
    }

    /**
     * Hàm chuyển toạ độ từ ảnh gốc sang ảnh scan (dùng cho cả trục tung và trục
     * hoành
     *
     * @param x
     * @return
     */
    private static int f(int x) {
        return (int) (x * 3.074 + 61.638);
    }

    private static boolean isLowerCase(BufferedImage image) {
        return hasDotAt(image, 36, 1001, 45, 1012);
    }

    private static boolean hasDotAt(BufferedImage image, int x1,
            int y1, int x2, int y2) {
        x1 = f(x1);
        x2 = f(x2);
        y1 = f(y1);
        y2 = f(y2);
        // kích thước điểm đánh dấu
        final double MN = (x2 - x1 + 1) * (y2 - y1 + 1);
        // ngưỡng xác định có chấm
        final int THRESHOLD = 120;

        // double r = 0, g = 0, b = 0;
        WritableRaster raster = image.getRaster();
        double c = 0; // color???
        for (int i = x1; i <= x2; i++) {
            for (int j = y1; j < y2; j++) {
                int p = (raster.getSample(i, j, 0) < THRESHOLD ? 0 : 255);
                c += p / MN;
            }
        }
        return c <= THRESHOLD;
    }

    private static final class Junk {

        public String fileName;
        public char fromChar, toChar;
        public int fromIndex, toIndex;

        private Junk(String fileName, char fromChar, char toChar) {
            super();
            this.fileName = fileName;
            this.fromChar = fromChar;
            this.fromIndex = getMapIndex(fromChar);
            this.toChar = toChar;
            this.toIndex = getMapIndex(toChar);
        }

        private Junk(String fileName, char ch) {
            this(fileName, ch, ch);
        }
    };
    private static final char[][] MAP = {
        {' ', 'a', 'ă', 'â', 'b', 'c', 'd'},
        {'đ', 'e', 'ê', 'f', 'g', 'h', 'i'},
        {'j', 'k', 'l', 'm', 'n', 'o', 'ô'},
        {'ơ', 'p', 'q', 'r', 's', 't', 'u'},
        {'ư', 'v', 'w', 'x', 'y', 'z', ' '},
        {'á', 'à', 'ả', 'ã', 'ạ', ' ', ' '},};
    private static final Junk[] junks = {
        new Junk("10002.jpg", 'x'),
        new Junk("10006.jpg", 'ô', 'ơ'),
        new Junk("10006.jpg", 'ư'),
        new Junk("10017.jpg", 'b'),
        new Junk("10022.jpg", 'a'),
        new Junk("10025.jpg", 'j'),
        new Junk("10027.jpg", 'j'),
        new Junk("10028.jpg", 'k'),
        new Junk("10031.jpg", 'b'),
        new Junk("10038.jpg", 'ạ'),
        new Junk("10040.jpg", 'j'),
        new Junk("10040.jpg", 'm', 'n'),
        new Junk("10044.jpg", 'b'),
        new Junk("10045.jpg", 'b'),
        new Junk("10047.jpg", 'b'),
        new Junk("10049.jpg", 'b'),
        new Junk("10045.jpg", 'z'),
        new Junk("10046.jpg", 'ạ'),
        new Junk("10047.jpg", 'a', 'â'),
        new Junk("10047.jpg", 'á', 'ạ'),
        new Junk("10048.jpg", 'j'),
        new Junk("10050.jpg", 'j'),
        new Junk("10050.jpg", 'j'),
        new Junk("10050.jpg", 'h'),
        new Junk("10050.jpg", 'x', 'y'),
        new Junk("10050.jpg", 'á', 'ạ'),
        new Junk("10056.jpg", 'á', 'ạ'),
        new Junk("10058.jpg", 'x'),
        new Junk("10060.jpg", 'á', 'ạ'),
        new Junk("10062.jpg", 'ạ'),
        new Junk("10063.jpg", 'z'),
        new Junk("10068.jpg", 'b'),
        new Junk("10068.jpg", 'x'),
        new Junk("10068.jpg", 'z'),
        new Junk("10072.jpg", 'a', 'ạ'),
        new Junk("10073.jpg", 'b'),
        new Junk("20009.jpg", 'b'),
        new Junk("20010.jpg", 'á', 'ạ'),
        new Junk("20011.jpg", 'á', 'ạ'),
        new Junk("20020.jpg", 'b'),
        new Junk("20026.jpg", 'b'),
        new Junk("20031.jpg", 'z'),
        new Junk("20031.jpg", 'w'),
        new Junk("20037.jpg", 'b'),
        new Junk("20042.jpg", 'd', 'ê'),
        new Junk("20043.jpg", 'b'),
        new Junk("20043.jpg", 'ô'),
        new Junk("20043.jpg", 'x'),
        new Junk("20051.jpg", 'b'),
        new Junk("20051.jpg", 'x'),
    };

    private static int getMapIndex(char ch) {
        int index = 0;
        for (char[] row : MAP) {
            for (char mapChar : row) {
                if (ch == mapChar) {
                    return index;
                }
                index++;
            }
        }
        return -1;
    }
}
