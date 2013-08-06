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
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

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

    private final WebView browser = new WebView();
    private final WebEngine webEngine = browser.getEngine();
    private final String exportFile;
    private final int width;
    private final int height;

    public Browser(String rootURL, String export, int width, int height) {
        //apply the styles
        getStyleClass().add("browser");
        // load the web page
        webEngine.load(rootURL);
        //add the web view to the scene
        getChildren().add(browser);
        browser.setFontSmoothingType(FontSmoothingType.GRAY);
        this.exportFile = export;
        this.width = width;
        this.height = height;
    }

    public WebEngine getEngine() {
        return webEngine;
    }

    private void handleError(Exception e) {
        throw new RuntimeException("Unable to export to PDF", e);
    }

    public void doExport(final Profile profile, final int width, final int height) {
        final PauseTransition pt = new PauseTransition();
        pt.setDuration(Duration.millis(profile.getPause()));
        final AtomicInteger cpt = new AtomicInteger();
        final Document document = new Document(new Rectangle(width, height), 0, 0, 0, 0);
        try {
            PdfWriter.getInstance(document, new FileOutputStream(exportFile));
        } catch (DocumentException | FileNotFoundException e) {
            handleError(e);
            return;
        }
        document.open();
        profile.setDocument(document);
        pt.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                WritableImage image = browser.snapshot(null, null);
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
                double scaler = ((document.getPageSize().getWidth() - document.leftMargin()
                        - document.rightMargin()) / image.getWidth()) * 100;

                try {
                    com.itextpdf.text.Image image2 =
                            com.itextpdf.text.Image.getInstance(bufferedImage, null);
                    image2.scalePercent((float) scaler);
                    document.add(image2);
                    document.newPage();
                    int current = cpt.incrementAndGet();
                    int nbSlides = profile.getSlideCount();
                    System.out.println("Exported slide " + current + (nbSlides > 0 ? "/" + nbSlides : ""));
                    if (!profile.isLastSlide(current)) {
                        profile.nextSlide();
                        pt.setDuration(Duration.millis(profile.getPause()));
                        pt.play();
                    } else {
                        profile.finish();
                        document.close();
                        System.out.println("Export complete.");
                        Platform.exit();
                    }
                } catch (IOException | DocumentException e) {
                    handleError(e);
                }
            }
        });
        webEngine.getLoadWorker().stateProperty().addListener(
                new ChangeListener<Worker.State>() {
                    public void changed(ObservableValue ov, Worker.State oldState, Worker.State newState) {
                        if (newState == Worker.State.SUCCEEDED) {
                            profile.setup();
                            profile.ready(new Runnable() { public void run() { pt.play(); } });
                        }
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
