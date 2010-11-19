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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import ocr.sapphire.ann.OCRNetwork;
import ocr.sapphire.util.Utils;
import ocr.sapphire.image.PreprocessorConfig;
import ocr.sapphire.sample.CharacterProcessedSample;
import ocr.sapphire.sample.ProcessedSample;
import ocr.sapphire.sample.ProcessedSampleReader;
import org.apache.commons.io.output.NullWriter;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author cumeo89
 */
public class NeuronNetworkTrainer {

    /**
     * How many negative iteration (iteration with higher error than previous
     * iteration) before we stop the algorithm
     */
    public static final int USELESS_ITERATION_BEFORE_STOP = 100;
    private OCRNetwork ann;
    private OCRNetwork bestNetwork = null;
    private String reportFile;
    private PreprocessorConfig config;
    private List<ProcessedSample> validateSet;
    private List<ProcessedSample> trainingSet;
    private boolean reportTrainingError = true;
    private double performance;
    private double validateError;
    private double trainingError;
    private double bestPerformance;
    private double bestValidateError;
    private String validateFile = null;
    private String trainingFile = null;
    private double trainingTime; // minutes

    public NeuronNetworkTrainer(String trainingFile, String validateFile,
            String reportFile, double learningRate, double momentum, int... layerSizes)
            throws IOException {
        this.trainingFile = trainingFile;
        this.validateFile = validateFile;
        this.trainingSet = readSamples(trainingFile);
        this.validateSet = readSamples(validateFile);
        this.reportFile = reportFile;
        ann = new OCRNetwork(config, layerSizes);
        ann.getNetwork().setRate(learningRate);
        ann.getNetwork().setMomentum(momentum);
    }

    public NeuronNetworkTrainer(boolean reportTrainingError, PreprocessorConfig config,
            List<ProcessedSample> trainingSet, List<ProcessedSample> validateSet,
            String reportFile, double learningRate, double momentum, int... layerSizes) {
        this.reportTrainingError = reportTrainingError;
        this.config = config;
        this.trainingSet = trainingSet;
        this.validateSet = validateSet;
//        this.validateSet = readSamples(trainingFile);
        this.reportFile = reportFile;
        ann = new OCRNetwork(config, layerSizes);
        ann.getNetwork().setRate(learningRate);
        ann.getNetwork().setMomentum(momentum);
    }

    private List<ProcessedSample> readSamples(String path) throws IOException {
        ProcessedSampleReader reader = null;
        try {
            reader = new ProcessedSampleReader(path);
            config = reader.getConfig();
            return reader.readAll();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    public void run() throws IOException {
        long start = System.currentTimeMillis();

        PrintWriter out = null;
        try {
            if (reportFile == null) {
                out = new PrintWriter(NullWriter.NULL_WRITER);
            } else {
                out = new PrintWriter(reportFile);
            }

            if (!StringUtils.isEmpty(trainingFile)) {
                out.println("Training set:\t" + trainingFile);
            }
            if (!StringUtils.isEmpty(validateFile)) {
                out.println("Validate set:\t" + validateFile);
            }

            out.println("Parameters:");
            out.println("Learning rate:\t" + ann.getNetwork().getRate());
            out.println("Momentum:\t" + ann.getNetwork().getMomentum());
            out.println("Number of hidden layers:\t" + (ann.getNetwork().getLayerCount() - 2));
            for (int i = 1; i < ann.getNetwork().getLayers().size() - 1; i++) {
                out.println("Layer " + i + ":\t" + ann.getNetwork().getLayers().get(i).getSize());
            }

            if (reportTrainingError) {
                out.println("Iteration\tTraining error\tValidating error\tPerformance");
            } else {
                out.println("Iteration\tValidating error\tPerformance");
            }

            bestValidateError = Double.MAX_VALUE;
            bestPerformance = 0;
            double uselessIterationCounter = 0;
            int iteration = 0;
            outerFor:
            for (;;) {
                Collections.shuffle(trainingSet);
                for (ProcessedSample sample : trainingSet) {
                    ann.train(sample);
                }

                iteration++;
                System.out.println(iteration);

                computeErrorAndPerformance();
                if (reportTrainingError) {
                    out.format("%d\t%f\t%f\t%f\n",
                            iteration, trainingError, validateError, performance);
                } else {
                    out.format("%d\t%f\t%f\n",
                            iteration, validateError, performance);
                }

                // save the best network if needed
                if (validateError < bestValidateError) {
                    bestNetwork = Utils.copy(ann);
                    uselessIterationCounter = 0;
                } else {
                    uselessIterationCounter += 1;
                    if (uselessIterationCounter > USELESS_ITERATION_BEFORE_STOP) {
                        break outerFor;
                    }
                }
                if (performance > bestPerformance) {
                    bestPerformance = performance;
                }
                if (validateError < bestValidateError) {
                    bestValidateError = validateError;
                }
            }

            out.println("Best performance: " + bestPerformance);
            out.println("Best validate error: " + bestValidateError);
            out.println("20 most problematic chars: ");
            for (int i = 0; i < 20 && !errorChar.isEmpty(); i++) {
                char ch = errorChar.lastKey();
                out.println(ch);
                errorChar.remove(ch);
            }
        } finally {
            if (out != null) {
                out.close();
            }
        }

        long stop = System.currentTimeMillis();
        trainingTime = (stop - start) / 1000.0 / 60.0;
    }
    private SortedMap<Character, Integer> errorChar = new TreeMap<Character, Integer>();

    private void computeErrorAndPerformance() throws IOException {
        int correctCount = 0;
        double totalError = 0;
        for (ProcessedSample sample : validateSet) {
            double[] output = ann.recognize(sample.getInputs());
            if (Utils.toChar(output) == ((CharacterProcessedSample) sample).getCharacter()) {
                correctCount++;
            } else {
                char ch = ((CharacterProcessedSample) sample).getCharacter();
                if (!errorChar.containsKey(ch)) {
                    errorChar.put(ch, 1);
                } else {
                    errorChar.put(ch, errorChar.get(ch) + 1);
                }
            }
            totalError += ann.getError(sample.getOutputs());
        }
        performance = correctCount / (double) validateSet.size();
        validateError = totalError / validateSet.size();

        if (reportTrainingError) {
            totalError = 0;
            for (ProcessedSample sample : trainingSet) {
                ann.recognize(sample.getInputs());
                totalError += ann.getError(sample.getOutputs());
            }
            trainingError = totalError / trainingSet.size();
        }
    }

    public OCRNetwork getBestNetwork() {
        return bestNetwork;
    }

    public double getBestPerformance() {
        return bestPerformance;
    }

    public double getBestValidateError() {
        return bestValidateError;
    }

    public double getTrainingTime() {
        return trainingTime;
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
                "training.yaml", "validate.yaml", "result3.csv",
                0.5, 0.6, 100);
        trainer.run();
        writeNetwork(trainer.getBestNetwork(), "network-temp2.yaml");

        trainer.ann.setCurrentImage("c.png");
        trainer.ann.prepareInput();
        System.out.println(trainer.ann.recognize());

        System.out.println("Total time: " + trainer.getTrainingTime() + "ph");
    }
}
