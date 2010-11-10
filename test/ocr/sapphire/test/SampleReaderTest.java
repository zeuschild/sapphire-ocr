/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ocr.sapphire.test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import ocr.sapphire.image.PreprocessorConfig;
import ocr.sapphire.sample.ProcessedSampleReader;
import org.junit.Test;

/**
 *
 * @author cumeo89
 */
public class SampleReaderTest {

    @Test
    public void readSomeLines() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("training.yaml"));
        System.out.println(reader.readLine());
        System.out.println(reader.readLine());
        System.out.println(reader.readLine());
        System.out.println(reader.readLine());
        reader.close();
    }

    @Test
    public void readSomeSamples() throws IOException {
        ProcessedSampleReader reader = null;
        try {
            reader = new ProcessedSampleReader("training.yaml");
            System.out.println("component count: " + reader.getConfig().getComponentCount());
            System.out.println("series length: " + reader.getConfig().getSeriesLength());
            for (int i = 0; i < 3; i++) {
                System.out.println("sample " + (i + 1) + ": " + reader.read());
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }
}
