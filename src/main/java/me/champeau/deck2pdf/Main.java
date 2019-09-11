package me.champeau.deck2pdf;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import javafx.scene.paint.Color;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
 * Entry point for the application.
 *
 * @author Cédric Champeau
 */
public class Main extends Application {

    private final static String FONTS_DIRECTORY_OPTION = "fontsdir";

    public static final int WIDTH = 1500;
    public static final int HEIGHT = 1000;
    // JPG or PNG compression level / quality
    public static final float COMPRESSION_QUALITY = 95.0f;

    private Scene scene;
    @Override public void start(Stage stage) {
        Map<String,String> opts = getParameters().getNamed();
        int width = parseArgumentAsInt(opts, "width", WIDTH);
        int height = parseArgumentAsInt(opts, "height", HEIGHT);
        float quality = parseArgumentAsFloat(opts, "quality", COMPRESSION_QUALITY);
        // The format option is used when no export file is specified.
        // Otherwise, the format is derived from the name of the export file.
        String format = opts.get("format");
        if (format == null) {
            format = "pdf";
        }

        stage.setTitle("PDF Export Web View");
        List<String> unnamed = filterUnnamed(getParameters().getUnnamed());
        if (unnamed.isEmpty()) {
            System.err.println("You must provide at least the name of the file to convert");
            System.exit(-1);
        }
        String path = null;
        String firstArg = unnamed.get(0);
        try {
            URL url = new URL(firstArg);
            path = url.toString();
        } catch (MalformedURLException e) {
            String url = null;
            try {
                path = new File(firstArg).toURI().toURL().toString();
            } catch (MalformedURLException e2) {
                System.err.println("Unable to load source file:" + e2.getMessage());
                System.exit(-1);
            }
        }

        String exportFile = "output." + format;
        if (unnamed.size() > 1) {
            exportFile = unnamed.get(1);
        }

        loadCustomFonts(opts);

        Browser browser = new Browser(path, exportFile, width, height, quality);
        scene = new Scene(browser, width, height, Color.web("#666970"));
        stage.setScene(scene);
        stage.show();
        Profile profile = ProfileLoader.loadProfile(opts.get("profile"), browser.getEngine(), opts);
        browser.doExport(profile, width, height);
    }


    /**
     * JavaFX 2.2 does not support CSS3 @font-face so this is a workaround that allows loading
     * custom fonts from a directory specified on command line. The key for the command line
     * option is 'fontsdir'
     * @param opts the command line options
     */
    private static void loadCustomFonts(final Map<String, String> opts) {
        String fontsDir = opts.get(FONTS_DIRECTORY_OPTION);
        if (fontsDir!=null) {
            File dir = new File(fontsDir);
            for (File font : dir.listFiles()) {
                if (font.isFile()) {
                    try {
                        Font.loadFont(new FileInputStream(font), -1);
                        System.out.println("Loaded font " + font);
                    } catch (FileNotFoundException e) {
                        System.err.println("Unable to load font from file "+font);
                    }
                }
            }
        }
    }

    private int parseArgumentAsInt(final Map<String, String> opts, String key, int defaultValue) {
        return opts.get(key)!=null?Integer.valueOf(opts.get(key)):defaultValue;
    }
    
    private float parseArgumentAsFloat(final Map<String, String> opts, String key, float defaultValue) {
        return opts.get(key) != null ? Float.valueOf(opts.get(key)) :defaultValue;
    }

    private List<String> filterUnnamed(List<String> unnamed) {
        List<String> result =
                unnamed
                        .stream()
                        .filter( p ->
                                !p.startsWith("-D") &&
                                !p.startsWith("me.champeau.deck2pdf.Main") &&
                                !p.startsWith("-classpath") &&
                                !p.isBlank() &&
                                !p.isEmpty()
                        )
                        .collect(Collectors.toList());

        return result;
    }

    public static void main(String[] args){
        launch(args);
    }
}
