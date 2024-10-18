plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(libs.nexus.plugin)
    implementation(libs.javafx.plugin)
    implementation(libs.extra.modules.plugin)
}

kotlin {
    jvmToolchain(21)
}
