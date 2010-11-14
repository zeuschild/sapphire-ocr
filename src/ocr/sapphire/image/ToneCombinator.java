package ocr.sapphire.image;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Random;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

public class ToneCombinator {

    private static final char[] CHARS = {'ă', 'â', 'e', 'ê', 'i', 'o', 'ô',
        'ơ', 'u', 'ư'};
    private static final char[] TONES = {'á', 'à', 'ả', 'ã', 'ạ'};
    private static final char[][] CHARS_WITH_TONE = {
        {'ắ', 'ấ', 'é', 'ế', 'í', 'ó', 'ố', 'ớ', 'ú', 'ứ'},
        {'ằ', 'ầ', 'è', 'ề', 'ì', 'ò', 'ồ', 'ờ', 'ù', 'ừ'},
        {'ẳ', 'ẩ', 'ẻ', 'ể', 'ỉ', 'ỏ', 'ổ', 'ở', 'ủ', 'ử'},
        {'ẵ', 'ẫ', 'ẽ', 'ễ', 'ĩ', 'õ', 'ỗ', 'ỡ', 'ũ', 'ữ'},
        {'ặ', 'ậ', 'ẹ', 'ệ', 'ị', 'ọ', 'ộ', 'ợ', 'ụ', 'ự'},};
    public static final String CHAR_DIR = "chars";
    public static final String TONE_DIR = "tone";
    public static final String OUTPUT_DIR = "temp";
    private BufferedImage[][] LOWER_TONE_IMAGES;
    private BufferedImage[][] UPPER_TONE_IMAGES;
    private Random r = new Random(System.currentTimeMillis());
    private int counter = 0;

    public ToneCombinator() {
    }

    public void run() throws IOException {
        loadTones();
        File charDir = new File(CHAR_DIR);
        for (int i = 0; i < CHARS.length; i++) {
            combine(charDir, i, false);
            combine(charDir, i, true);
        }
    }

    private void combine(File charDir, int i, boolean upper) throws IOException {
        final char character = upper ? Character.toUpperCase(CHARS[i])
                : CHARS[i];
        String[] charFileNames = charDir.list(new FilenameFilter() {

            private Pattern pattern = Pattern.compile(".+" + character + "-\\d+\\.sample\\.png");

            @Override
            public boolean accept(File dir, String name) {
                return pattern.matcher(name).matches();
            }
        });
        for (String charFileName : charFileNames) {
            BufferedImage charImage = ImageIO.read(new File(charDir,
                    charFileName));
            String prefix = OUTPUT_DIR + "/" + charFileName.substring(0, charFileName.indexOf(character));
            for (int j = 0; j < TONES.length; j++) {
//                // lọc bớt vì quá nhiều
//                if (r.nextDouble() > 1.0/3.0) {
//                    continue;
//                }

                char tonedChar = upper ? Character.toUpperCase(CHARS_WITH_TONE[j][i])
                        : CHARS_WITH_TONE[j][i];

                BufferedImage tonedCharImage = new BufferedImage(
                        charImage.getWidth(), charImage.getHeight(),
                        charImage.getType());
                Graphics2D g = tonedCharImage.createGraphics();
                g.drawImage(charImage, 0, 0, null);
                g.setXORMode(Color.WHITE);
                g.drawImage(randomElement(upper ? UPPER_TONE_IMAGES[j]
                        : LOWER_TONE_IMAGES[j]), 0, 0, null);
                g.dispose();

                String tonedCharFileName = String.format("%s%c-%d.png",
                        prefix, tonedChar, (int) tonedChar);
                ImageIO.write(tonedCharImage, "PNG",
                        new File(tonedCharFileName));

                counter++;
                if (counter % 100 == 0) {
                    System.out.println(counter);
                }
//				if (counter >= 5) {
//					return;
//				}
            }
        }
    }

    private <T> T randomElement(T[] arr) {
        return arr[r.nextInt(arr.length)];
    }

    private void loadTones() throws IOException {
        LOWER_TONE_IMAGES = new BufferedImage[5][];
        UPPER_TONE_IMAGES = new BufferedImage[5][];

        File toneDir = new File(TONE_DIR);
        for (int i = 0; i < TONES.length; i++) {
            final char tone = TONES[i];
            LOWER_TONE_IMAGES[i] = loadTone(toneDir, tone);
            UPPER_TONE_IMAGES[i] = loadTone(toneDir,
                    Character.toUpperCase(tone));
        }
    }

    private BufferedImage[] loadTone(File toneDir, final char tone)
            throws IOException {
        String[] toneFileNames = toneDir.list(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.indexOf(tone) >= 0;
            }
        });
        BufferedImage[] images = new BufferedImage[toneFileNames.length];
        for (int i = 0; i < toneFileNames.length; i++) {
            images[i] = ImageIO.read(new File(toneDir, toneFileNames[i]));
        }
        return images;
    }

    public static void main(String[] args) throws IOException {
        new ToneCombinator().run();
    }
}
