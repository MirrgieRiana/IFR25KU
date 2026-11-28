import java.util.Properties

plugins {
    kotlin("jvm")
}

val experiment = Properties().apply {
    rootProject.file("../gradle.properties").inputStream().use { load(it) }
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(25)
    sourceSets.main {
        kotlin.srcDir(rootProject.file("../../../mirrg.kotlin/src/main/kotlin"))
    }
}

dependencies {
    compileOnly("com.google.code.gson:gson:${experiment.getProperty("gson_version")}")
    compileOnly("org.slf4j:slf4j-api:${experiment.getProperty("slf4j_version")}")
}
