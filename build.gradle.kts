plugins {
    scala
    application
    alias(libs.plugins.shadow)
}

scala {
    scalaVersion = libs.versions.scala
    zincVersion = libs.versions.zinc
}

application {
    // Define the main class for the application.
    mainClass = "ru.cyberc3dr.scalaapp.App"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.scala.library)
    implementation(libs.guava)
}

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    destinationDirectory = file("$rootDir/build")
    archiveVersion = ""
    manifest {
        attributes["Main-Class"] = application.mainClass
    }
}

sourceSets.main {
    scala.srcDir("src")
    resources.srcDir("resources")
}

// Apply a specific Java toolchain to ease working on different environments.
java.toolchain.languageVersion = JavaLanguageVersion.of(21)
