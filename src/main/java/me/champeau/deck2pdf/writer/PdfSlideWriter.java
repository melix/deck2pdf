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

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;

import java.awt.image.BufferedImage;
import java.io.IOException;

public class PdfSlideWriter extends SlideWriter {
    private final Document document;

    public PdfSlideWriter(final Document document) {
        this.document = document;
    }

    @Override
    public void writeSlide(final BufferedImage image, final int numSlides, final int current) throws SlideExportException {
        try {
            com.itextpdf.text.Image image2 =
                    com.itextpdf.text.Image.getInstance(image, null);
            double scaler = ((document.getPageSize().getWidth() - document.leftMargin()
                    - document.rightMargin()) / image.getWidth()) * 100;
            image2.scalePercent((float) scaler);
            document.add(image2);
            document.newPage();
        } catch (IOException | DocumentException e) {
            throw new SlideExportException(e);
        }
    }

    @Override
    public void close() {
        document.close();
    }
}
