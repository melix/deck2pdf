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

import java.io.File;
import java.util.regex.Pattern;

public abstract class MultiFileSlideWriter extends SlideWriter {
    // ex. %04d
    private static final Pattern NUMBER_FORMAT_PATTERN = Pattern.compile("%0?[1-9]\\d*d");
    private static final String IMAGE_EXT_REGEX = "\\.(png|jp(?:e)?g)$";

    protected final String exportFile;

    protected MultiFileSlideWriter(final String exportFile) {
        this.exportFile = exportFile;
    }

    protected File getOutputFile(final int numSlides, final int current) {
        File slideFile;
        if (NUMBER_FORMAT_PATTERN.matcher(exportFile).find()) {
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
        return slideFile;
    }

}
