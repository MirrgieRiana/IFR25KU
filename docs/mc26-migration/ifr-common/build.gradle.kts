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
    maven("https://api.modrinth.com/maven") { content { includeModule("maven.modrinth", "pf-emi") } }
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
        include("common/src/main/resources/miragefairy2024.mixins.json", "common/src/client/resources/miragefairy2024.client.mixins.json")
        include("fabric/src/main/resources/fabric.mod.json", "fabric/src/main/resources/miragefairy2024.fabric.mixins.json", "fabric/src/main/resources/miragefairy2024.accesswidener")
        include("fabric/src/client/resources/miragefairy2024.client.fabric.mixins.json", "neoforge/src/main/resources/miragefairy2024.client.neoforge.mixins.json")
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
val portedFabric = preparePortedSources.map { it.destinationDir.resolve("fabric/src") }
val migrationFabric = providers.gradleProperty("migrationFabric").isPresent
val migrationSources = providers.gradleProperty("migrationSources").orNull?.split(",").orEmpty()
val migrationClientSources = providers.gradleProperty("migrationClientSources").orNull?.split(",").orEmpty()

sourceSets.main {
    kotlin.srcDir(portedCommon.map { it.resolve("main/kotlin") })
    resources.srcDir(portedCommon.map { it.resolve("main/resources") })
    if (utilitiesOnly) {
        kotlin.include("miragefairy2024/util/**")
    } else {
        java.srcDir(portedCommon.map { it.resolve("main/java") })
        if (migrationFabric) {
            kotlin.srcDir(portedFabric.map { it.resolve("main/kotlin") })
            java.srcDir(portedFabric.map { it.resolve("main/java") })
            resources.srcDir(portedFabric.map { it.resolve("main/resources") })
        }
        if (migrationSources.isNotEmpty()) {
            kotlin.include(migrationSources)
            java.include(migrationSources)
        }
    }
}

sourceSets.named("client") {
    kotlin.srcDir(portedCommon.map { it.resolve("client/kotlin") })
    java.srcDir(portedCommon.map { it.resolve("client/java") })
    resources.srcDir(portedCommon.map { it.resolve("client/resources") })
    if (migrationFabric) {
        kotlin.srcDir(portedFabric.map { it.resolve("client/kotlin") })
        java.srcDir(portedFabric.map { it.resolve("client/java") })
        resources.srcDir(portedFabric.map { it.resolve("client/resources") })
    }
    if (migrationClientSources.isNotEmpty()) {
        kotlin.include(migrationClientSources)
        java.include(migrationClientSources)
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
    implementation("net.fabricmc.fabric-api:fabric-api:${experiment.getProperty("fabric_api_version")}")
    if (providers.gradleProperty("migrationClientClasspath").isPresent) {
        compileOnly(loom.namedMinecraftJars)
    }
    compileOnly("dev.architectury:architectury:${experiment.getProperty("architectury_version")}")
    compileOnly("com.github.glitchfiend:TerraBlender-fabric:26.1.2-26.1.2.0.3")
    compileOnly("me.shedaniel:RoughlyEnoughItems-api-fabric:26.1.819")
    compileOnly("me.shedaniel:RoughlyEnoughItems-default-plugin-fabric:26.1.819")
    compileOnly("me.shedaniel.cloth:basic-math:0.6.1")
    if (providers.gradleProperty("migrationEmiFork").isPresent) {
        compileOnly("maven.modrinth:pf-emi:2.0.0+26.1.2+fabric")
    }
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("mirrg.kotlin:mirrg.kotlin.helium-kotlin-2-0:4.6.0")
    implementation(project(":mirrg"))
}
