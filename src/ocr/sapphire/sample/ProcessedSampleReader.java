package ocr.sapphire.sample;

import java.io.Closeable;
import java.io.IOException;

import ocr.sapphire.image.PreprocessorConfig;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import java.io.FileReader;
import ocr.sapphire.Utils;

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
