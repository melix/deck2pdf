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

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Properties;

public class ProfileLoader {
    private static final String DEFAULT_PROFILE = "deckjs";

    public static Profile loadProfile(final String profile, final WebEngine engine, final Map<String,?> options) {
        if (profile==null) {
            return loadProfile(DEFAULT_PROFILE, engine, options);
        }
        Profile result = null;
        ClassLoader loader = ProfileLoader.class.getClassLoader();
        InputStream resource = loader.getResourceAsStream(profile + ".properties");
        if (resource!=null) {
            result = loadProfileFromPropertiesFile(resource, engine, options);
        } else {
            resource = loader.getResourceAsStream(profile + ".groovy");
            if (resource!=null) {
                result = loadProfileFromGroovy(resource, engine, options);
            }
        }
        try {
            if (profile.endsWith(".properties")) {
                resource = new BufferedInputStream(new FileInputStream(profile));
                result = loadProfileFromPropertiesFile(resource, engine, options);
            } else if (profile.endsWith(".groovy")) {
                resource = new BufferedInputStream(new FileInputStream(profile));
                result = loadProfileFromGroovy(resource, engine, options);
            }
        } catch (FileNotFoundException e) {
            result = null;
        }
        if (result==null) {
            throw new RuntimeException("Cannot find a profile named '"+profile);
        }
        return result;
    }

    private static GroovyProfile loadProfileFromGroovy(final InputStream resource, final WebEngine engine, final Map<String,?> options) {
        return new GroovyProfile(
              engine,
              options,
              new InputStreamReader(resource)
        );
    }

    private static Profile loadProfileFromPropertiesFile(final InputStream resource, final WebEngine engine, final Map<String,?> options) {
        Properties props = new Properties();
        try {
            props.load(resource);
        } catch (IOException e) {
            throw new RuntimeException(e); // ugly but blame Java checked exceptions!
        }
        String totalSlides = findProperty(props, "totalSlides");
        String nextSlide = findProperty(props,"nextSlide");
        String pause = props.getProperty("pause");

        JSProfile result = new JSProfile(engine, options, totalSlides, nextSlide);
        if (pause!=null) {
            result.setPause(Integer.valueOf(pause));
        }
        return result;
    }

    private static String findProperty(final Properties props, final String key) {
        String value = props.getProperty(key);
        if (value==null) {
            throw new RuntimeException("Profile doesn't define the "+key+" property");
        }
        return value;
    }
}
