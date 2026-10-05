plugins {
    kotlin("jvm") version "2.0.0"
}

repositories {
    mavenCentral()
    maven("https://raw.githubusercontent.com/MirrgieRiana/mirrg.kotlin/refs/heads/maven/maven/") { // mirrg.kotlin.helium
        content {
            includeGroup("mirrg.kotlin")
        }
    }
}

dependencies {
    implementation(project(path = ":mirrg.kotlin")) // mirrg.kotlin
    implementation("mirrg.kotlin:${libs.versions.mirrgKotlinHelium.get()}") // mirrg.kotlin.helium
}
