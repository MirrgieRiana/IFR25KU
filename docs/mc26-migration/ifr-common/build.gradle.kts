import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    kotlin("jvm") version "2.4.21"
    id("net.fabricmc.fabric-loom") version "1.18.4"
}

val experiment = Properties().apply {
    file("../gradle.properties").inputStream().use { load(it) }
}
val repositoryRoot = layout.projectDirectory.dir("../../..")
val utilitiesOnly = providers.gradleProperty("utilitiesOnly").isPresent

repositories {
    mavenCentral()
    maven("https://maven.architectury.dev/") { content { includeGroup("dev.architectury") } }
    maven("https://maven.shedaniel.me/") {
        content {
            includeGroup("me.shedaniel")
            includeGroup("me.shedaniel.cloth")
        }
    }
    maven("https://maven.minecraftforge.net/") { content { includeModule("com.github.glitchfiend", "TerraBlender-fabric") } }
    maven("https://raw.githubusercontent.com/MirrgieRiana/mirrg.kotlin/refs/heads/maven/maven/") { content { includeGroup("mirrg.kotlin") } }
}

loom {
    splitEnvironmentSourceSets()
}

sourceSets.main {
    if (!utilitiesOnly) {
        java.srcDir(repositoryRoot.dir("common/src/main/java"))
    }
    kotlin.srcDir(repositoryRoot.dir("common/src/main/kotlin"))
    if (utilitiesOnly) {
        kotlin.include("miragefairy2024/util/**")
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_25)
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${experiment.getProperty("minecraft_version")}")
    compileOnly("net.fabricmc:fabric-loader:${experiment.getProperty("fabric_loader_version")}")
    compileOnly("net.fabricmc.fabric-api:fabric-api:${experiment.getProperty("fabric_api_version")}")
    compileOnly("dev.architectury:architectury:${experiment.getProperty("architectury_version")}")
    compileOnly("com.github.glitchfiend:TerraBlender-fabric:26.1.2-26.1.2.0.3")
    compileOnly("me.shedaniel:RoughlyEnoughItems-api-fabric:26.1.819")
    compileOnly("me.shedaniel:RoughlyEnoughItems-default-plugin-fabric:26.1.819")
    compileOnly("me.shedaniel.cloth:basic-math:0.6.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("mirrg.kotlin:mirrg.kotlin.helium-kotlin-2-0:4.6.0")
    implementation(project(":mirrg"))
}
