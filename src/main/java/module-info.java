module deck2pdf.main {

    requires javafx.controls;

    requires itextpdf;
    requires org.codehaus.groovy;
    requires jdk.jsobject;

    requires javafx.web;
    requires javafx.swing;

    exports me.champeau.deck2pdf;

    opens me.champeau.deck2pdf to javafx.web;

}