import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.0.0"
}

repositories {
    mavenCentral()
}

tasks.withType<KotlinCompile>().all {
    compilerOptions {
        freeCompilerArgs.set(listOf("-Xcontext-receivers"))
    }
}
