package ocr.sapphire.sample;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import ocr.sapphire.image.PreprocessorConfig;

public class SamplePreparer {

    public static final PreprocessorConfig DEFAULT_CONFIG = new PreprocessorConfig(
            6, 10);
    private static final String VALIDATE_PATH = "validate.yaml";
    private static final String TRAINING_PATH = "training.yaml";
    private static final String SAMPLE_DIR = "../data/samples";

    public static void main(String[] args) throws IOException {
        ProcessedSampleWriter trainingWriter = null;
        ProcessedSampleWriter validateWriter = null;
        try {
            trainingWriter = new ProcessedSampleWriter(TRAINING_PATH,
                    DEFAULT_CONFIG);
            validateWriter = new ProcessedSampleWriter(VALIDATE_PATH,
                    DEFAULT_CONFIG);

            File sampleDir = new File(SAMPLE_DIR);
            FileFilter filter = new SampleFileFilter();
            int counter = 0;
            for (File sampleFile : sampleDir.listFiles(filter)) {
                // remove 10% of them: try small data first
                if (Math.random() > 1.0 / 10.0) {
                    continue;
                }

                Sample sample = SampleIO.read(sampleFile);
                System.out.printf("%4d. %s --> ", ++counter,
                        sampleFile.getName());
                if (Math.random() < 2.0 / 3.0) {
                    System.out.println("training");
                    trainingWriter.write(sample);
                } else {
                    System.out.println("validate");
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
