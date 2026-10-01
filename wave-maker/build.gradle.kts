plugins {
    kotlin("jvm") version "2.0.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(path = ":mirrg.kotlin")) // mirrg.kotlin
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${libs.versions.kotlinCoroutines.get()}") // Kotlin Coroutines
    implementation("com.google.code.gson:gson:${libs.versions.gson.get()}") // mirrg.kotlin.gson.hydrogen が返す JsonElement のために要るのだ～🌱
    implementation("org.slf4j:slf4j-api:${libs.versions.slf4j.get()}") // mirrg.kotlin.slf4j.hydrogen が返す Logger のために要るのだ～🌱
}
