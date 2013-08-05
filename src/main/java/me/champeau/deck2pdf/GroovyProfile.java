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
import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.GroovyShell;
import groovy.lang.MissingPropertyException;
import javafx.scene.web.WebEngine;
import org.codehaus.groovy.runtime.MethodClosure;

import java.io.Reader;

/**
 * The Groovy provide can be used for complex interactions with the web engine. The Groovy script consists
 * minimally of 2 variables: totalSlides and nextSlide. The script exposes two additional variables:
 * <ul>
 *     <li>engine, of type {@link WebEngine}</li>
 *     <li>js, a function that will call javascript code using the engine (shortcut for engine.executeScript()</li>
 * </ul>
 * For example, a deck.js profile can be written like this:
 * <code>
 *     totalSlides = { js("\$.deck('getSlides').length") }
 *     nextSlide = { js("\$.deck('next')") }
 * </code>
 */
public class GroovyProfile extends Profile {
    private final Binding binding;
    protected GroovyProfile(final WebEngine engine, final Reader script) {
        super(engine);
        binding = new NullBinding();
        binding.setVariable("engine", engine);
        binding.setVariable("js", new MethodClosure(this, "executeJS"));
        new GroovyShell(binding).evaluate(script);
    }

    public Object executeJS(String code) {
        try {
            return engine.executeScript(code);
        } catch (netscape.javascript.JSException e) {
            System.err.println("Error while executing the following code:");
            System.err.println(code);
            throw e;
        }
    }

    @Override
    public void setDocument(final Document document) {
        super.setDocument(document);
        binding.setVariable("document", document);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean isLastSlide(final int slideIdx) {
        Closure<Boolean> fun = (Closure<Boolean>) binding.getVariable("isLastSlide");
        if (fun==null) {
            return getSlideCount()==slideIdx;
        }
        return fun.call();
    }

    @Override
    public void nextSlide() {
        ((Closure)binding.getVariable("nextSlide")).call();
    }

    @Override
    public int getPause() {
        Object pause = binding.getVariable("pause");
        if (pause==null) {
            return super.getPause();
        } else if (pause instanceof Closure) {
            return (Integer)((Closure)pause).call();
        } else if (pause instanceof Integer) {
            return (Integer) pause;
        } else {
            throw new RuntimeException("'pause' function returned an unexpected value");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public int getSlideCount() {
        Closure<Integer> fun = (Closure<Integer>) binding.getVariable("totalSlides");
        if (fun==null){
            return super.getSlideCount();
        }
        return fun.call();
    }

    @Override
    public void setup() {
        Closure fun = (Closure) binding.getVariable("setup");
        if (fun!=null){
            fun.call();
        }
    }

    @Override
    public void finish() {
        Closure fun = (Closure) binding.getVariable("finish");
        if (fun!=null){
            fun.call();
        }
    }

    private static class NullBinding extends Binding {
        @Override
        public Object getVariable(final String name) {
            Object variable = null;
            try {
                variable = super.getVariable(name);
            } catch (MissingPropertyException e) {
                return null;
            }
            return variable;
        }
    }
}
