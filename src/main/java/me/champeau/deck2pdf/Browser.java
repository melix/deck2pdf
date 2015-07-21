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
package me.champeau.deck2pdf;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Region;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Duration;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/*
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
 *
 */

/**
 * This class is responsible for converting a slide deck into a PDF file. It performs the following:
 * <p/>
 * <ul> <li>opens a web view with the slide deck</li> <li>captures each slide after one second</li> <li>puts the capture
 * into a PDF file</li> <li>exits the VM</li> </ul>
 *
 * @author Cédric Champeau
 */
class Browser extends Region {

    // ex. %04d
    private static final Pattern NUMBER_FORMAT_PATTERN = Pattern.compile("%0?[1-9]\\d*d");
    private static final String IMAGE_EXT_REGEX = "\\.(png|jpg)$";
    
    private final WebView browser = new WebView();
    private final WebEngine webEngine = browser.getEngine();
    private final String exportFile;
    private final String format;
    private final int width;
    private final int height;
    private final float quality;

    public Browser(String rootURL, String exportFile, int width, int height, float quality) {
        //apply the styles
        getStyleClass().add("browser");
        // load the web page
        webEngine.load(rootURL);
        //add the web view to the scene
        getChildren().add(browser);
        browser.setFontSmoothingType(FontSmoothingType.GRAY);
        this.width = width;
        this.height = height;
        this.quality = quality;
        if (exportFile.endsWith(".png")) {
            this.format = "png";
        } else if (exportFile.endsWith(".jpg")) {
            // WARNING jpg doesn't work on OpenJDK
            this.format = "jpg";
        } else {
            if (!exportFile.endsWith(".pdf")) {
                exportFile = exportFile + ".pdf";
            }
            this.format = "pdf";
        }
        this.exportFile = exportFile;
    }

    public WebEngine getEngine() {
        return webEngine;
    }

    private void handleError(Exception e) {
        throw new RuntimeException("Unable to export slide deck", e);
    }

    public void doExport(final Profile profile, final int width, final int height) {
        final PauseTransition pt = new PauseTransition();
        pt.setDuration(Duration.millis(profile.getPause()));
        final AtomicInteger slideCounter = new AtomicInteger();
        final AtomicInteger numSlidesCache = new AtomicInteger(-1);
        final Document document;
        final ImageWriter imageWriter;
        final ImageWriteParam imageWriteParams;
        // TODO move setup to the constructor of a SlideWriter class based on format (e.g., PDFSlideWriter)
        if (format.equals("pdf")) {
            document = new Document(new Rectangle(width, height), 0, 0, 0, 0);
            try {
                PdfWriter.getInstance(document, new FileOutputStream(exportFile));
            } catch (DocumentException | FileNotFoundException e) {
                handleError(e);
                return;
            }
            document.open();
            profile.setDocument(document);
            imageWriter = null;
            imageWriteParams = null;
        } else if (format.equals("jpg")) {
            document = null;
            imageWriter = ImageIO.getImageWritersByFormatName("jpeg").next();
            imageWriteParams = imageWriter.getDefaultWriteParam();
            if (this.quality > 0 && this.quality <= 100) {
                imageWriteParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                imageWriteParams.setCompressionQuality(this.quality / 100.0f);
            }
        } else {
            document = null;
            imageWriter = null;
            imageWriteParams = null;
        }
        pt.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                WritableImage snapshot = browser.snapshot(null, null);
                // Remove alpha-channel from buffered image to reduce size and enable jpg export
                BufferedImage image = new BufferedImage((int) snapshot.getWidth(), (int) snapshot.getHeight(), BufferedImage.OPAQUE);
                SwingFXUtils.fromFXImage(snapshot, null).copyData(image.getRaster());

                int numSlides = numSlidesCache.get();
                if (numSlides == -1) {
                    numSlides = profile.getSlideCount();
                    numSlidesCache.set(numSlides);
                }
                int current = slideCounter.incrementAndGet();
                // TODO delegate work to SlideWriter#writeSlide method
                try {
                    if (format.equals("pdf")) {
                        com.itextpdf.text.Image image2 =
                                com.itextpdf.text.Image.getInstance(image, null);
                        double scaler = ((document.getPageSize().getWidth() - document.leftMargin()
                                - document.rightMargin()) / snapshot.getWidth()) * 100;
                        image2.scalePercent((float) scaler);
                        document.add(image2);
                        document.newPage();
                    } else {
                        File slideFile;
                        if (NUMBER_FORMAT_PATTERN.matcher(exportFile).matches()) {
                            slideFile = new File(String.format(exportFile, current));
                        } else {
                            // QUESTION should we enforce a minimum length of the digit for consistency?
                            //int totalCols = Math.max(3, ((int) Math.log10(numSlides)) + 1);
                            int totalCols = ((int) Math.log10(numSlides)) + 1;
                            String formattedSlideNum = String.format("%0" + totalCols + "d", current);
                            slideFile = new File(exportFile.replaceFirst(IMAGE_EXT_REGEX, "-" + formattedSlideNum + ".$1"));
                        }
                        if (slideFile.exists()) {
                            slideFile.delete();
                        }
                        if (format.equals("jpg")) {
                            ImageOutputStream ios = ImageIO.createImageOutputStream(slideFile);
                            imageWriter.setOutput(ios);
                            try {
                                imageWriter.write(null, new IIOImage(image, null, null), imageWriteParams);
                            } finally {
                                try {
                                    ios.flush();
                                } catch (IOException e) {}
                                try {
                                    ios.close();
                                } catch (IOException e) {}
                            }
                        } else {
                            ImageIO.write(image, format, slideFile);
                        }
                    }
                    System.out.println("Exported slide " + current + (numSlides > 0 ? "/" + numSlides : ""));
                    if (!profile.isLastSlide(current)) {
                        profile.nextSlide();
                        pt.setDuration(Duration.millis(profile.getPause()));
                        pt.play();
                    } else {
                        profile.finish();
                        // QUESTION can't profile close the document?
                        if (document != null) {
                            document.close();
                        }
                        if (imageWriter != null) {
                            imageWriter.dispose();
                        }
                        System.out.println("Export complete!");
                        Platform.exit();
                    }
                } catch (IOException | DocumentException e) {
                    handleError(e);
                }
            }
        });
        webEngine.getLoadWorker().stateProperty().addListener(
                (ov, oldState, newState) -> {
                    if (newState == Worker.State.SUCCEEDED) {
                        profile.setup();
                        profile.ready(pt::play);
                    }
                });
    }

    @Override
    protected void layoutChildren() {
        double w = getWidth();
        double h = getHeight();
        layoutInArea(browser, 0, 0, w, h, 0, HPos.CENTER, VPos.CENTER);
    }

    @Override
    protected double computePrefWidth(double height) {
        return width;
    }

    @Override
    protected double computePrefHeight(double width) {
        return height;
    }
}
