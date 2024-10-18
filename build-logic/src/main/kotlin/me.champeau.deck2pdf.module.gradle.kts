plugins {
    id("application")
    id("org.openjfx.javafxplugin")
    id("org.gradlex.extra-java-module-info")
    id("maven-publish")
    id("io.github.gradle-nexus.publish-plugin")
    id("signing")
}

group = "me.champeau.deck2pdf"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(23))
    }
    withSourcesJar()
    withJavadocJar()
}

publishing {
    repositories {
        maven {
            name = "build"
            setUrl(layout.buildDirectory.dir("repo").map { uri(it) })
        }
    }
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            pom.addRequiredMetadataForPublicationOnMavenCentral()
            artifact(tasks.named<Zip>("distZip").flatMap { it.archiveFile })
        }
    }
}

nexusPublishing {
    repositories {
        sonatype {
            username.set(providers.systemProperty("central.username"))
            password.set(providers.systemProperty("central.password"))
        }
    }
}

// Follow the requirements described at https://central.sonatype.org/pages/requirements.html
// (which assume that POM is the king)
fun MavenPom.addRequiredMetadataForPublicationOnMavenCentral() {
    name.set(providers.provider { "${project.group}:${project.name}" })
    description.set(providers.provider { project.description })
    url.set("https://github.com/melix/deck2pdf")
    licenses {
        license {
            name.set("The Apache Software License, Version 2.0")
            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
        }
    }

    developers {
        developer {
            name.set("Cédric Champeau")
            email.set("cedric.champeau@gmail.com")
            organization.set("Personal")
            organizationUrl.set("https://github.com/melix")
        }
    }

    scm {
        connection.set("scm:git:git://github.com/melix/deck2pdf.git")
        developerConnection.set("scm:git:ssh://github.com/melix/deck2pdf.git")
        url.set("https://github.com/melix/deck2pdf/tree/master")
    }
}

signing {
    setRequired {
        gradle.taskGraph.allTasks.any {
            it.name.startsWith("publish")
        }
    }
    publishing.publications.configureEach {
        sign(this)
    }
    useGpgCmd()
}
