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

    public int getInputCount() {
        return componentCount * 4 * seriesLength;
    }

}
