package ocr.sapphire.image;

public class PreprocessorConfig {

    private int componentCount;
    private int seriesLength;

    public PreprocessorConfig() {
    }

    public PreprocessorConfig(int componentCount, int seriesLength) {
        this.componentCount = componentCount;
        this.seriesLength = seriesLength;
    }

    public int getComponentCount() {
        return componentCount;
    }

    public int getSeriesLength() {
        return seriesLength;
    }
}
