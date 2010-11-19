package ocr.sapphire.sample;

import java.io.Closeable;
import java.io.IOException;

import ocr.sapphire.image.PreprocessorConfig;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;
import ocr.sapphire.util.Utils;

public class ProcessedSampleReader implements Closeable {

    private PreprocessorConfig config;
    private YamlReader yamlReader;

    public ProcessedSampleReader(String path) throws IOException {
        yamlReader = new YamlReader(new FileReader(path),
                Utils.DEFAULT_YAML_CONFIG);
        config = yamlReader.read(PreprocessorConfig.class);
    }

    public ProcessedSample read() throws YamlException {
        return yamlReader.read(CharacterProcessedSample.class);
    }

    public List<ProcessedSample> readAll() throws YamlException {
        List<ProcessedSample> samples = new LinkedList<ProcessedSample>();
        ProcessedSample sample = null;
        while ((sample = read()) != null) {
            samples.add(sample);
        }
        return samples;
    }

    public PreprocessorConfig getConfig() {
        return config;
    }

    public int getOutputCount() {
        return 16;
    }

    @Override
    public void close() throws IOException {
        yamlReader.close();
    }
}
