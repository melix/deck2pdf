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
import javafx.scene.web.WebEngine;

import java.util.Map;

/**
 * A profile describes how the browser will interact with the slide deck
 * in order to switch from one slide to another and eventually finish the
 * export.
 *
 * Profiles are used to implement exporting for various HTML5 slide decks.
 *
 * @author Cédric Champeau
 */
public abstract class Profile {

    protected static final int DEFAULT_PAUSE_MILLIS = 1000;
    protected final WebEngine engine;
    protected final Map<String, ?> options;

    protected Document document;

    protected Profile(final WebEngine engine, final Map<String, ?> options) {
        this.engine = engine;
        this.options = options;
    }

    public void setDocument(final Document document) {
        this.document = document;
    }

    /**
     * Implement this method if the slide deck provides a way to determine the total
     * number of slides.
     * @return -1 if the number of slides is not known, or a positive number if it is.
     */
    public int getSlideCount() {
        return -1;
    }

    /**
     * Given a slide number, tells if the slide is the last one.
     * @param slideIdx the slide number, starting from 1 (first slide has index 1, not 0)
     * @return true if this slide is the last one
     */
    public abstract boolean isLastSlide(int slideIdx);

    /**
     * Implement this method to jump to the next slide.
     */
    public abstract void nextSlide();

    /**
     * Returns the time to wait before going to the next slide.
     * @return Time to wait, in milliseconds.
     */
    public int getPause() {
        return DEFAULT_PAUSE_MILLIS;
    }

    /**
     * Called before the slides capture starts.
     */
    public void setup() {}

    /**
     * Called before the export is closed, gives the profile chances to cleanup
     * extra resources or perform additional tasks before the pdf file gets closed.
     */
    public void finish() {}


    /**
     * This method is called once setup is complete and deck2pdf is ready to export.
     * The action is a {@link Runnable} that will start the export of slides.
     * Advanced profiles may capture the action instead if directly running it, for
     * example if the slide deck defers loading of slides with javascript, in which
     * case export should be triggered from javascript instead of from deck2pdf itself.
     * @param action the action that will start the export
     */
    public void ready(Runnable action) {
        action.run();
    }
}
