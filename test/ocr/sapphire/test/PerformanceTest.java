/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ocr.sapphire.test;

import java.io.IOException;
import ocr.sapphire.ann.OCRNetwork;
import org.junit.Test;

/**
 *
 * @author cumeo89
 */
public class PerformanceTest {

    public PerformanceTest() {
    }

    @Test
    public void train() throws IOException {
        OCRNetwork net = new OCRNetwork();
        net.setCurrentImage("a.png");
        net.setResult('A');
        net.prepareInput();
        net.prepareIdeal();
        for (int i = 0; i < 30000; i++) {
            net.train();
        }
    }
}
