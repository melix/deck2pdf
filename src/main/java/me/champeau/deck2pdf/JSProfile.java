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

import javafx.scene.web.WebEngine;

import java.util.Map;

/**
 * Base class for profiles that leverage Javascript to get the total number of slides
 * and jump to the next slide.
 *
 * This profile also allows overriding the default pause delay.
 *
 * @author Cédric Champeau
 */
public class JSProfile extends Profile {

    private String slideCountJS;
    private String nextSlideJS;
    private int pause = DEFAULT_PAUSE_MILLIS;

    public JSProfile(
            WebEngine engine,
            Map<String,?> options,
            String slideCountJS,
            String nextSlideJS) {
        super(engine, options);
        this.slideCountJS = slideCountJS;
        this.nextSlideJS = nextSlideJS;
    }

    /**
     * Sets the javascript code that will be called to display the next slide.
     * @param nextSlideJS javascript code to be displayed for the next slide
     */
    public void setNextSlideJS(final String nextSlideJS) {
        this.nextSlideJS = nextSlideJS;
    }

    /**
     * Sets the javascript code that will be used to retrieve the
     * total number of slides of this deck.
     * @param slideCountJS a javascript code that returns a integer
     */
    public void setSlideCountJS(final String slideCountJS) {
        this.slideCountJS = slideCountJS;
    }

    public void setPause(final int pause) {
        this.pause = pause;
    }

    public int getPause() {
        return pause;
    }

    @Override
    public int getSlideCount() {
        return (Integer) engine.executeScript(slideCountJS);
    }

    @Override
    public boolean isLastSlide(final int slideIdx) {
        return slideIdx==getSlideCount();
    }

    @Override
    public void nextSlide() {
        engine.executeScript(nextSlideJS);
    }
}
