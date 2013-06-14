# deck2pdf

```deck2pdf``` is a simple application that will convert your [deck.js](http://imakewebthings.com/deck.js/) slide deck into a PDF file.
This application relies on a JavaFX application, so you need at least a Java 7 installation that bundles JavaFX (which should be the case for all newer installs of Java).

## Install

[![Build Status](http://travis-ci.org/melix/deck2pdf.png)](http://travis-ci.org/melix/deck2pdf)

```
./gradlew distZip
```

Will generate a distribution into ```build/distributions/``` that you can unzip wherever you want.

## Usage

```deck2pdf <inputfile> <outputfile>```

Ex:

```deck2pdf slides.html slides.pdf```

Optionally, you can specify width and height:

```deck2pdf --width 1024 --height 768 slides.html slides.pdf```