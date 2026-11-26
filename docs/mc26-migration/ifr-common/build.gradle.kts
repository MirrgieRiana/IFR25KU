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
    enableTransitiveAccessWideners = true
}

val preparePortedSources = tasks.register<Sync>("preparePortedSources") {
    from(repositoryRoot) {
        include("common/src/**/*.kt", "common/src/**/*.java")
        include("fabric/src/**/*.kt", "fabric/src/**/*.java")
        include("neoforge/src/**/*.kt", "neoforge/src/**/*.java")
    }
    into(layout.buildDirectory.dir("portedSources"))

    val migrationPatch = layout.projectDirectory.file("sources-26.1.2.patch")
    inputs.file(migrationPatch)
    doLast {
        val outputPath = destinationDir.relativeTo(repositoryRoot.asFile).invariantSeparatorsPath
        providers.exec {
            workingDir(repositoryRoot.asFile)
            commandLine("git", "apply", "--no-index", "--directory=$outputPath", "--", migrationPatch.asFile.absolutePath)
        }.result.get().assertNormalExitValue()
    }
}

val portedCommon = preparePortedSources.map { it.destinationDir.resolve("common/src") }
val migrationSources = providers.gradleProperty("migrationSources").orNull?.split(",").orEmpty()

sourceSets.main {
    kotlin.srcDir(portedCommon.map { it.resolve("main/kotlin") })
    if (utilitiesOnly) {
        kotlin.include("miragefairy2024/util/**")
    } else {
        java.srcDir(portedCommon.map { it.resolve("main/java") })
        if (migrationSources.isNotEmpty()) {
            kotlin.include(migrationSources)
            java.include(migrationSources)
        }
    }
}

sourceSets.named("client") {
    kotlin.srcDir(portedCommon.map { it.resolve("client/kotlin") })
    java.srcDir(portedCommon.map { it.resolve("client/java") })
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
    implementation("net.fabricmc.fabric-api:fabric-api:${experiment.getProperty("fabric_api_version")}")
    if (providers.gradleProperty("migrationClientClasspath").isPresent) {
        compileOnly(loom.namedMinecraftJars)
    }
    compileOnly("dev.architectury:architectury:${experiment.getProperty("architectury_version")}")
    compileOnly("com.github.glitchfiend:TerraBlender-fabric:26.1.2-26.1.2.0.3")
    compileOnly("me.shedaniel:RoughlyEnoughItems-api-fabric:26.1.819")
    compileOnly("me.shedaniel:RoughlyEnoughItems-default-plugin-fabric:26.1.819")
    compileOnly("me.shedaniel.cloth:basic-math:0.6.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("mirrg.kotlin:mirrg.kotlin.helium-kotlin-2-0:4.6.0")
    implementation(project(":mirrg"))
}
