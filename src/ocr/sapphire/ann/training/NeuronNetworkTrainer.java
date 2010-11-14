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
package ocr.sapphire.ann.training;

import com.esotericsoftware.yamlbeans.YamlWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import ocr.sapphire.ann.OCRNetwork;
import ocr.sapphire.Utils;
import ocr.sapphire.sample.CharacterProcessedSample;
import ocr.sapphire.sample.ProcessedSample;
import ocr.sapphire.sample.ProcessedSampleReader;

/**
 *
 * @author cumeo89
 */
public class NeuronNetworkTrainer {

    /**
     * How many iteration before errors are evaluauted and conditions are checked
     */
    public static final int EVALUATE_RATE = 200;
    /**
     * How many negative iteration (iteration with higher error than previous
     * iteration) before we stop the algorithm
     */
    public static final int NEGATIVE_ITERATION_BEFORE_STOP = 2000;
    private OCRNetwork ann;
    private OCRNetwork bestNetwork = null;
    private String reportFile;
    private List<ProcessedSample> validateSet;
    private List<ProcessedSample> trainingSet;
    private double performance;
    private double validateError;

    public NeuronNetworkTrainer(String trainingFile, String validateFile, String reportFile)
            throws IOException {
        this.trainingSet = readSamples(trainingFile);
        //this.validateSet = readSamples(validateFile);
        this.validateSet = readSamples(trainingFile);
        this.reportFile = reportFile;
        ann = new OCRNetwork();
    }

    private List<ProcessedSample> readSamples(String path) throws IOException {
        List<ProcessedSample> samples = new LinkedList<ProcessedSample>();
        ProcessedSampleReader reader = null;
        try {
            reader = new ProcessedSampleReader(path);
            ProcessedSample sample = null;
            while ((sample = reader.read()) != null) {
                samples.add(sample);
            }
            return samples;
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    public void run() throws IOException {
        PrintWriter out = null;
        try {
            out = new PrintWriter(reportFile);
            out.println("Iteration\tTraining error\tValidating error\tPerformance");

            double bestValidateError = Double.MAX_VALUE;
            double previousValidateError = 0;
            double negativeIterationCounter = 0;
            double totalIterationError = 0;
            int iteration = 0;
            outerFor:
            for (;;) {
                for (ProcessedSample sample : trainingSet) {
                    ann.train(sample);
                    totalIterationError += ann.getError();

                    iteration++;

                    if (iteration % EVALUATE_RATE == 0) {
                        System.out.println(iteration);

                        double trainingError = totalIterationError / EVALUATE_RATE;
                        totalIterationError = 0;
                        computeValidateErrorAndPerformance();
                        out.format(Locale.FRENCH, "%d\t%f\t%f\t%f\n",
                                iteration, trainingError, validateError, performance);

                        // save the best network if needed
                        if (validateError < bestValidateError) {
                            bestNetwork = Utils.copy(ann);
                            bestValidateError = validateError;
                        }

                        // check the halt condition
                        if (validateError < previousValidateError) {
                            negativeIterationCounter += EVALUATE_RATE;
                            if (negativeIterationCounter > NEGATIVE_ITERATION_BEFORE_STOP) {
                                break outerFor;
                            }
                        } else {
                            negativeIterationCounter = 0;
                        }
                        previousValidateError = validateError;

                        // for testing purpose
                        if (iteration >= 10000) {
                            return;
                        }
                    }
                }
            }
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    private void computeValidateErrorAndPerformance() throws IOException {
        int correctCount = 0;
        double totalError = 0;
        for (ProcessedSample sample : validateSet) {
            double[] output = ann.recognize(sample.getInputs());
            if (Utils.toChar(output) == ((CharacterProcessedSample)sample).getCharacter()) {
                correctCount++;
            }
            totalError += ann.getError(sample.getOutputs());
        }
        performance = correctCount / (double)validateSet.size();
        validateError = totalError / validateSet.size();
    }

    public OCRNetwork getBestNetwork() {
        return bestNetwork;
    }

    private static void writeNetwork(OCRNetwork network, String path) throws IOException {
        YamlWriter writer = null;
        try {
            writer = new YamlWriter(new FileWriter(path), Utils.DEFAULT_YAML_CONFIG);
            writer.write(network);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        NeuronNetworkTrainer trainer = new NeuronNetworkTrainer(
                "training.yaml", "validate.yaml", "training.csv");
        trainer.run();
        writeNetwork(trainer.getBestNetwork(), "network.yaml");
        trainer.ann.setCurrentImage("c.png");
        trainer.ann.prepareInput();
        System.out.println(trainer.ann.recognize());
    }
}
