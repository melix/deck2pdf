plugins {
    id("me.champeau.deck2pdf.module")
}

javafx {
    version = "23.0.1"
    modules = listOf("javafx.controls", "javafx.web", "javafx.swing")
}

dependencies {
    implementation(libs.itextpdf)
    implementation(libs.groovy.core)
}

application {
    mainClass = "me.champeau.deck2pdf.Main"
}

extraJavaModuleInfo {
    module("com.itextpdf:itextpdf", "com.itextpdf") {
        exports("com.itextpdf.awt")
        exports("com.itextpdf.text")
        exports("com.itextpdf.text.pdf")
        requires("java.desktop")
    }
}
