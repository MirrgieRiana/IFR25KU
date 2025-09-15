import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.0.0"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("com.google.code.gson:gson:2.10.1")
    compileOnly("io.github.themrmilchmann.gradle.publish.curseforge:gradle-curseforge-publish:0.8.0")
}

tasks.withType<KotlinCompile>().all {
    compilerOptions {
        freeCompilerArgs.set(listOf("-Xcontext-receivers"))
    }
}
