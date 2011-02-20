package ocr.sapphire.sample;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import ocr.sapphire.image.AbstractImagePreprocessor;

import ocr.sapphire.image.PreprocessorConfig;
import ocr.sapphire.image.RegionBasedImagePreprocessor;

public class SamplePreparer {

    public static final PreprocessorConfig DEFAULT_CONFIG = new PreprocessorConfig(
            3, 10);
    private static final String VALIDATE_PATH = "validate-small.yaml";
    private static final String TRAINING_PATH = "training-small.yaml";
    private static final String SAMPLE_DIR = "../data/samples2";

    public static void main(String[] args) throws IOException {
        ProcessedSampleWriter trainingWriter = null;
        ProcessedSampleWriter validateWriter = null;
        try {
            trainingWriter = new ProcessedSampleWriter(TRAINING_PATH, DEFAULT_CONFIG);
            validateWriter = new ProcessedSampleWriter(VALIDATE_PATH, DEFAULT_CONFIG);
            AbstractImagePreprocessor processor = new RegionBasedImagePreprocessor(DEFAULT_CONFIG);

            File sampleDir = new File(SAMPLE_DIR);
            FileFilter filter = new SampleFileFilter();

            Map<Character, List<ProcessedSample>> sampleMap = new HashMap<Character, List<ProcessedSample>>();
            for (File sampleFile : sampleDir.listFiles(filter)) {
                try {
                    System.out.println("Processing: " + sampleFile.getName());
                    Sample sample = SampleIO.read(sampleFile);
                    if (sampleMap.get(sample.getCharacter()) == null) {
                        sampleMap.put(sample.getCharacter(), new ArrayList<ProcessedSample>());
                    }
                    processor.process(sample.getImage());
                    CharacterProcessedSample processedSample = new CharacterProcessedSample(
                            processor.getInputs(), sample.getCharacter());
                    sampleMap.get(sample.getCharacter()).add(processedSample);
                } catch (RuntimeException ex) {
                    System.out.println("Error: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }

            int counter = 0;
            Random r = new Random(System.currentTimeMillis());
            for (List<ProcessedSample> samples : sampleMap.values()) {
                int total = samples.size();
                while (samples.size() >= total / 3) {
                    ProcessedSample sample = samples.remove(r.nextInt(samples.size()));
                    System.out.println(++counter + " training");
                    trainingWriter.write(sample);
                }
                for (ProcessedSample sample : samples) {
                    System.out.println(++counter + " validate");
                    validateWriter.write(sample);
                }
            }
        } finally {
            if (trainingWriter != null) {
                trainingWriter.close();
            }
            if (validateWriter != null) {
                validateWriter.close();
            }
        }
    }
}
