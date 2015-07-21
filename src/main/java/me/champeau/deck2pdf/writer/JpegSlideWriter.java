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

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class JpegSlideWriter extends MultiFileSlideWriter {
    private final ImageWriter imageWriter;
    private final ImageWriteParam imageWriteParams;

    public JpegSlideWriter(String exportFile, float quality) {
        super(exportFile);
        imageWriter = ImageIO.getImageWritersByFormatName("jpeg").next();
        imageWriteParams = imageWriter.getDefaultWriteParam();
        if (quality > 0 && quality <= 100) {
            imageWriteParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            imageWriteParams.setCompressionQuality(quality / 100.0f);
        }
    }

    @Override
    public void writeSlide(final BufferedImage export, final int numSlides, final int current) throws SlideExportException {
        try {
            ImageOutputStream ios = ImageIO.createImageOutputStream(getOutputFile(numSlides, current));
            imageWriter.setOutput(ios);
            try {
                imageWriter.write(null, new IIOImage(export, null, null), imageWriteParams);
            } finally {
                ios.flush();
                ios.close();
            }
        } catch (IOException e) {
            throw new SlideExportException(e);
        }
    }

    @Override
    public void close() {
        imageWriter.dispose();
    }
}
