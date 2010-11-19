/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ocr.sapphire.ann.training;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import ocr.sapphire.image.PreprocessorConfig;
import ocr.sapphire.sample.ProcessedSample;
import ocr.sapphire.sample.ProcessedSampleReader;

/**
 *
 * @author cumeo89
 */
public class ParameterTester {

    public static void main(String[] args) throws IOException {
        final String TRAINING_FILE = "training-small.yaml";
        final String VALIDATE_FILE = "validate-small.yaml";
        ProcessedSampleReader reader = null;
        PrintWriter out = null;
        try {
            // prepare
            reader = new ProcessedSampleReader(TRAINING_FILE);
            final PreprocessorConfig config = reader.getConfig();
            final List<ProcessedSample> trainingSet = reader.readAll();
            reader.close();
            reader = new ProcessedSampleReader(VALIDATE_FILE);
            final List<ProcessedSample> validateSet = reader.readAll();

            // try
            out = new PrintWriter("param.csv");
            out.println("Test different parameters");
            out.println("Training set:\t" + TRAINING_FILE);
            out.println("Validate set:\t" + VALIDATE_FILE);

            final TestCase[] cases = {
//                new TestCase(100, 3, 0.9, 0.7),
//                new TestCase(100, 3, 0.5, 0.7),
//                new TestCase(100, 3, 0.3, 0.7),
//                new TestCase(100, 3, 0.1, 0.7),
                new TestCase(100, 2, 0.5, 0),
                new TestCase(100, 2, 0.5, 0.04),
                new TestCase(100, 2, 0.5, 0.1),
                new TestCase(100, 2, 0.5, 0.3),
                new TestCase(100, 2, 0.5, 0.9),
//                new TestCase(100, 0, 0.5, 0.6),
//                new TestCase(100, 1, 0.5, 0.6),
//                new TestCase(100, 2, 0.5, 0.6),
//                new TestCase(80, 3, 0.5, 0.6),
//                new TestCase(100, 3, 0.5, 0.6),
//                new TestCase(120, 3, 0.5, 0.6),
//                new TestCase(100, 4, 0.5, 0.6),
//                new TestCase(50, 3, 0.5, 0.6),
//                new TestCase(150, 3, 0.5, 0.6),
//                new TestCase(250, 3, 0.2, 0.6),
//                new TestCase(400, 3, 0.2, 0.6),
            };

            out.println("Layer count\tLayer size\tLearning rate\tMomentum\tBest validate error\tBest performance\tTraining time");
            for (TestCase c : cases) {
                System.out.printf("Trying: (%d, %d, %.2f, %.2f)...\n", c.hiddenLayerCount, c.hiddenLayerSize, c.learningRate, c.momentum);
                NeuronNetworkTrainer trainer = new NeuronNetworkTrainer(false, config,
                        trainingSet, validateSet, null, c.learningRate, c.momentum, c.getHiddenLayerSizes());
                trainer.run();
                out.println(c.hiddenLayerCount + "\t" + c.hiddenLayerSize
                        + "\t" + c.learningRate + "\t" + c.momentum
                        + "\t" + trainer.getBestValidateError()
                        + "\t" + trainer.getBestPerformance()
                        + "\t" + trainer.getTrainingTime());
                System.out.printf("Time: %.2f'\n", trainer.getTrainingTime());
            }
        } finally {
            if (out != null) {
                out.close();
            }
            if (reader != null) {
                reader.close();
            }
        }
    }

    static class TestCase {

        int hiddenLayerSize;
        int hiddenLayerCount;
        double learningRate;
        double momentum;

        public TestCase(int hiddenLayerSize, int hiddenLayerCount, double learningRate, double momentum) {
            this.hiddenLayerSize = hiddenLayerSize;
            this.hiddenLayerCount = hiddenLayerCount;
            this.learningRate = learningRate;
            this.momentum = momentum;
        }

        int[] getHiddenLayerSizes() {
            int[] arr = new int[hiddenLayerCount];
            Arrays.fill(arr, hiddenLayerSize);
            return arr;
        }
    }
}
