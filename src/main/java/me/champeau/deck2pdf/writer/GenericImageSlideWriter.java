/*
 * Copyright 2003-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.champeau.deck2pdf.writer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class GenericImageSlideWriter extends MultiFileSlideWriter {
    private final String format;

    public GenericImageSlideWriter(String exportFile, String format) {
        super(exportFile);
        this.format = format;
    }

    @Override
    public void writeSlide(final BufferedImage export, final int numSlides, final int current) throws SlideExportException {
        try {
            ImageIO.write(export, format, getOutputFile(numSlides, current));
        } catch (IOException e) {
            throw new SlideExportException(e);
        }
    }
}
