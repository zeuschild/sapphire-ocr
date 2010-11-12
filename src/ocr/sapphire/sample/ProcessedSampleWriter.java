package ocr.sapphire.sample;

import java.io.Closeable;
import java.io.FileWriter;
import java.io.IOException;

import ocr.sapphire.image.ImagePreprocessor;
import ocr.sapphire.image.PreprocessorConfig;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlWriter;
import ocr.sapphire.Utils;

public class ProcessedSampleWriter implements Closeable {

	PreprocessorConfig config;
	private YamlWriter yamlWriter;
	private ImagePreprocessor processor;

	public ProcessedSampleWriter(String path, PreprocessorConfig config)
			throws IOException {
		this.config = config;
		processor = new ImagePreprocessor(config.getComponentCount(),
				config.getSeriesLength());
		yamlWriter = new YamlWriter(new FileWriter(path), Utils.DEFAULT_YAML_CONFIG);
		yamlWriter.write(config);
	}

	public void write(ProcessedSample sample) throws YamlException {
		yamlWriter.write(sample);
	}

	public void write(Sample sample) throws YamlException {
		double[][][] coefficients = processor.process(sample.getImage());
		double[] inputs = new double[config.getComponentCount() * 4
				* config.getSeriesLength()];

		int index = 0;
		for (int i = 0; i < coefficients.length; i++) {
			double[][] component = coefficients[i];
			for (int s = 0; s < 4; s++) {
				for (int k = 0; k < config.getSeriesLength(); k++) {
					inputs[index++] = component[s][k];
				}
			}
		}
		CharacterProcessedSample processedSample = new CharacterProcessedSample(
				inputs, sample.getCharacter());
		write(processedSample);
	}

	@Override
	public void close() throws IOException {
		yamlWriter.close();
	}

	public PreprocessorConfig getConfig() {
		return config;
	}

}
