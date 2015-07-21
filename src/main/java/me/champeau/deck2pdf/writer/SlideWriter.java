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
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;
import me.champeau.deck2pdf.Profile;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public abstract class SlideWriter {
    private enum ExportFormat {
        pdf,
        png,
        jpeg,
        unknown;

        public boolean isPdf() {
            return this == pdf;
        }

        public boolean isJpeg() {
            return this == jpeg;
        }

        public boolean isPng() {
            return this == png;
        }

        static ExportFormat of(String fileName) {
            String fn = fileName.toLowerCase();
            if (fn.endsWith(".pdf")) {
                return pdf;
            }
            if (fn.endsWith(".png")) {
                return png;
            }
            if (fn.endsWith(".jpeg") || fn.endsWith(".jpg")) {
                return jpeg;
            }
            return unknown;
        }
    }

    public abstract void writeSlide(BufferedImage export, final int numSlides, final int current) throws SlideExportException;

    public void close() {
    }

    public static SlideWriter of(Profile profile, String exportFile, int width, int height, float quality) throws SlideExportException {
        ExportFormat format = ExportFormat.of(exportFile);
        if (format.isPdf()) {
            Document document = new Document(new Rectangle(width, height), 0, 0, 0, 0);
            try {
                PdfWriter.getInstance(document, new FileOutputStream(exportFile));
            } catch (DocumentException | FileNotFoundException e) {
                throw new SlideExportException(e);
            }
            document.open();
            profile.setDocument(document);
            return new PdfSlideWriter(document);
        } else if (format.isJpeg()) {
            return new JpegSlideWriter(exportFile, quality);

        } else {
            return new GenericImageSlideWriter(exportFile, format.toString());
        }
    }
}
