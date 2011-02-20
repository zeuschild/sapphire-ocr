package ocr.sapphire.sample;

import java.io.Closeable;
import java.io.FileWriter;
import java.io.IOException;

import ocr.sapphire.image.PreprocessorConfig;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlWriter;
import ocr.sapphire.image.AbstractImagePreprocessor;
import ocr.sapphire.image.ImagePreprocessor;
import ocr.sapphire.image.RegionBasedImagePreprocessor;
import ocr.sapphire.util.Utils;

public class ProcessedSampleWriter implements Closeable {

    PreprocessorConfig config;
    private YamlWriter yamlWriter;
    private AbstractImagePreprocessor processor;

    public ProcessedSampleWriter(String path, PreprocessorConfig config)
            throws IOException {
        this.config = config;
        processor = new RegionBasedImagePreprocessor(config.getComponentCount(),
                config.getSeriesLength());
        yamlWriter = new YamlWriter(new FileWriter(path), Utils.DEFAULT_YAML_CONFIG);
        yamlWriter.write(config);
    }

    public void write(ProcessedSample sample) throws YamlException {
        yamlWriter.write(sample);
    }

    public void write(Sample sample) throws YamlException {
        processor.process(sample.getImage());
        CharacterProcessedSample processedSample = new CharacterProcessedSample(
                processor.getInputs(), sample.getCharacter());
        write(processedSample);
    }

    @Override
    public void close() throws IOException {
        yamlWriter.close();
    }

    public PreprocessorConfig getConfig() {
        return config;
    }

    public ImagePreprocessor getProcessor() {
        return processor;
    }
    
}
