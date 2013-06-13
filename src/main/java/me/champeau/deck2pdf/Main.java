package me.champeau.deck2pdf;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javafx.scene.paint.Color;

import java.io.File;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

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

    public static final int WIDTH = 1500;
    public static final int HEIGHT = 1000;

    private Scene scene;
    @Override public void start(Stage stage) {
        Map opts = getParameters().getNamed();
        int width = (Integer) (opts.get("width")!=null?opts.get("width"):WIDTH);
        int height = (Integer) (opts.get("height")!=null?opts.get("height"):HEIGHT);

        stage.setTitle("PDF Export Web View");
        List<String> unnamed = getParameters().getUnnamed();
        if (unnamed.isEmpty()) {
            System.err.println("You must provide at least the name of the file to convert");
            System.exit(-1);
        }
        String url = null;
        try {
            url = new File(unnamed.get(0)).toURI().toURL().toString();
        } catch (MalformedURLException e) {
            System.err.println("Unable to load source file:" + e.getMessage());
            System.exit(-1);
        }

        String output = "output.pdf";
        if (unnamed.size()>1) {
            output = unnamed.get(1);
        }
        Browser browser = new Browser(url, output, width, height);
        scene = new Scene(browser, width, height, Color.web("#666970"));
        stage.setScene(scene);
        stage.show();
        browser.doExport();
    }

    public static void main(String[] args){
        launch(args);
    }
}
