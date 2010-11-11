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

import java.io.IOException;
import java.io.PrintWriter;
import ocr.sapphire.ann.OCRNetwork;
import ocr.sapphire.ann.Utils;
import ocr.sapphire.sample.ProcessedSample;
import ocr.sapphire.sample.ProcessedSampleReader;

/**
 *
 * @author cumeo89
 */
public class NeuronNetworkTrainer {

    public static final String TRAINING_FILE = "training.yaml";
    public static final String VALIDATE_FILE = "validate.yaml";

    /**
     * How many iteration before errors are evaluauted and conditions are checked
     */
    public static final int EVALUATE_RATE = 100;

    /**
     * How many negative iteration (iteration with higher error than previous
     * iteration) before we stop the algorithm
     */
    public static final int NEGATIVE_ITERATION_BEFORE_STOP = 2000;
    private OCRNetwork ann;
    private OCRNetwork bestNetwork = null;

    public NeuronNetworkTrainer() throws IOException {
        ann = new OCRNetwork();
    }

    public void run() throws IOException {
        PrintWriter out = new PrintWriter("result.cvs");
        double bestValidateError = 1;
        double previousValidateError = 1;
        double negativeIterationCounter = 0;
        int iteration = 0;
        outerFor: for (;;) {
            ProcessedSampleReader training = new ProcessedSampleReader(TRAINING_FILE);
            ProcessedSample sample;
            while ((sample = training.read()) != null) {
                ann.train(sample);

                if (++iteration % EVALUATE_RATE == 0) {
                    double trainingError = ann.getError();
                    double validateError = validate();
                    out.println(iteration + "\t" + trainingError + "\t" + validateError);

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
                }
            }
        }
        out.close();
    }

    private double validate() throws IOException {
        ProcessedSampleReader validate = new ProcessedSampleReader(VALIDATE_FILE);
        double totalError = 0;
        int counter = 0;
        ProcessedSample sample = null;
        while ((sample = validate.read()) != null) {
            ann.recognize(sample.getInputs());
            totalError += ann.getError(sample.getOutputs());
            counter++;
        }
        return totalError / counter;
    }

    public static void main(String[] args) {
    }
}
