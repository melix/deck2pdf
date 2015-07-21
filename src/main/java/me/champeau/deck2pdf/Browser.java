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

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Region;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import me.champeau.deck2pdf.writer.SlideExportException;
import me.champeau.deck2pdf.writer.SlideWriter;

import java.awt.image.BufferedImage;
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
        final SlideWriter writer;
        try {
            writer = SlideWriter.of(profile, exportFile, width, height, quality);
        } catch (SlideExportException e) {
            handleError(e);
            return;
        }
        pt.setOnFinished(actionEvent -> {
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
            try {
                writer.writeSlide(image, numSlides, current);
                System.out.printf("Exported slide %d%s%n", current, numSlides > 0 ? "/" + numSlides : "");
                if (!profile.isLastSlide(current)) {
                    profile.nextSlide();
                    pt.setDuration(Duration.millis(profile.getPause()));
                    pt.play();
                } else {
                    profile.finish();
                    writer.close();
                    System.out.println("Export complete!");
                    Platform.exit();
                }
            } catch (SlideExportException e) {
                handleError(e);
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
