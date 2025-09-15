import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import io.github.themrmilchmann.gradle.publish.curseforge.ChangelogFormat
import io.github.themrmilchmann.gradle.publish.curseforge.GameVersion
import io.github.themrmilchmann.gradle.publish.curseforge.ReleaseType
import net.fabricmc.loom.task.RemapJarTask

plugins {
    id("com.github.johnrengelman.shadow")
    id("com.modrinth.minotaur")
    id("io.github.themrmilchmann.curseforge-publish")
}

architectury {
    platformSetupLoomIde()
    neoForge()
}

loom {
    accessWidenerPath.set(project(":common").loom.accessWidenerPath)
}

sourceSets {
    main {
        resources {
            srcDir(file("src/generated/resources"))
        }
    }
}

configurations {
    val common by creating {
        isCanBeResolved = true
        isCanBeConsumed = false
    }
    getByName("compileClasspath").extendsFrom(common)
    getByName("runtimeClasspath").extendsFrom(common)
    getByName("developmentNeoForge").extendsFrom(common)

    // Files in this configuration will be bundled into your mod using the Shadow plugin.
    // Don't use the `shadow` configuration from the plugin itself as it's meant for excluding files.
    val shadowBundle by creating {
        isCanBeResolved = true
        isCanBeConsumed = false
    }
}

repositories {
    maven("https://maven.neoforged.net/releases") // NeoForged
    maven("https://maven.su5ed.dev/releases") // forgified-fabric-api
    maven("https://thedarkcolour.github.io/KotlinForForge/") // kotlin-for-forge
    maven("https://maven.shedaniel.me") // RoughlyEnoughItems
    maven("https://maven.wispforest.io/releases/") // owo-lib
    maven("https://maven.minecraftforge.net/") // com.github.glitchfiend:TerraBlender-neoforge
    maven("https://maven.blamejared.com") // JEI
    maven("https://maven.terraformersmc.com/releases") // EMI
    maven("https://www.cursemaven.com") // Jade
    maven("https://raw.githubusercontent.com/MirrgieRiana/mirrg.kotlin/refs/heads/maven/maven/") // mirrg.kotlin.helium
}

dependencies {

    // Loader
    neoForge("net.neoforged:neoforge:${libs.versions.neoforge.get()}") // NeoForge

    // Platform
    modImplementation("org.sinytra.forgified-fabric-api:forgified-fabric-api:${libs.versions.forgifiedFabricApi.get()}") // Forgified Fabric API
    //modImplementation("thedarkcolour:kotlinforforge:${libs.versions.kotlinForForge.get()}") // Kotlin
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:${libs.versions.kotlinCoroutines.get()}") // Kotlin Coroutines
    //forgeRuntimeLibrary("org.jetbrains.kotlinx:kotlinx-coroutines-core:${libs.versions.kotlinCoroutines.get()}") // Kotlin Coroutines
    "developmentForgeLike"("org.jetbrains.kotlinx:kotlinx-coroutines-core:${libs.versions.kotlinCoroutines.get()}") { isTransitive = false } // Kotlin Coroutines
    modImplementation("dev.architectury:architectury-neoforge:${libs.versions.architecturyApi.get()}") // Architectury API

    // Module
    "common"(project(path = ":common", configuration = "namedElements")) { isTransitive = false } // common
    "shadowBundle"(project(path = ":common", configuration = "transformProductionNeoForge")) // common shadow
    "common"(project(path = ":mirrg.kotlin")) // mirrg.kotlin
    "shadowBundle"(project(path = ":mirrg.kotlin")) { isTransitive = false } // mirrg.kotlin shadow

    // Library
    "common"("mirrg.kotlin:mirrg.kotlin.helium:${libs.versions.mirrgKotlinHelium.get()}")
    "shadowBundle"("mirrg.kotlin:mirrg.kotlin.helium:${libs.versions.mirrgKotlinHelium.get()}") { isTransitive = false }

    modRuntimeOnly("me.shedaniel:RoughlyEnoughItems-neoforge:${libs.versions.rei.get()}")
    modCompileOnly("me.shedaniel:RoughlyEnoughItems-api-neoforge:${libs.versions.rei.get()}")
    modCompileOnly("me.shedaniel:RoughlyEnoughItems-default-plugin-neoforge:${libs.versions.rei.get()}")
    modCompileOnly("me.shedaniel.cloth:basic-math:${libs.versions.clothBasicMath.get()}")

    modCompileOnly("mezz.jei:jei-1.21.1-common-api:${libs.versions.jei.get()}")
    modCompileOnly("mezz.jei:jei-1.21.1-neoforge-api:${libs.versions.jei.get()}")
    modRuntimeOnly("mezz.jei:jei-1.21.1-neoforge:${libs.versions.jei.get()}")

    modCompileOnly("dev.emi:emi-neoforge:${libs.versions.emi.get()}:api")
    modRuntimeOnly("dev.emi:emi-neoforge:${libs.versions.emi.get()}")

    modCompileOnly("curse.maven:jade-324717:${libs.versions.jadeCurseNeoForge.get()}")
    modRuntimeOnly("curse.maven:jade-324717:${libs.versions.jadeCurseNeoForge.get()}")

    modImplementation("io.wispforest:owo-lib-neoforge:${libs.versions.owoLibNeoForge.get()}")// { isTransitive = true }
    forgeRuntimeLibrary(include(api("io.wispforest:endec:${libs.versions.endec.get()}")!!)!!)
    forgeRuntimeLibrary(include(api("io.wispforest.endec:netty:${libs.versions.endecNetty.get()}")!!)!!)
    forgeRuntimeLibrary(include(api("io.wispforest.endec:gson:${libs.versions.endecGson.get()}")!!)!!)
    forgeRuntimeLibrary(include(api("io.wispforest.endec:jankson:${libs.versions.endecJankson.get()}")!!)!!)
    forgeRuntimeLibrary(include(api("blue.endless:jankson:${libs.versions.jankson.get()}")!!)!!)

    modImplementation("com.github.glitchfiend:TerraBlender-neoforge:${libs.versions.terraBlenderNeoForge.get()}")
}

// https://github.com/modrinth/minotaur
modrinth {
    token = System.getenv("MODRINTH_TOKEN")
    projectId = "ifr25ku"
    //versionNumber = project.mod_version
    versionType = if ("alpha" in project.version.toString()) "alpha" else if ("beta" in project.version.toString()) "beta" else "release"
    uploadFile = tasks["remapJar"]
    //gameVersions = ["1.20.2"]
    //loaders = ["neoforge"]
    changelog.set("This project maintains a comprehensive [CHANGELOG.md](https://github.com/MirrgieRiana/IFR25KU/blob/main/CHANGELOG.md) in Japanese.")
    dependencies {
        required.project("forgified-fabric-api")
        required.project("kotlin-for-forge")
        required.project("owo-lib")
        required.project("cloth-config")
        required.project("terrablender")
        required.project("architectury-api")
    }
}
rootProject.tasks.named("upload").configure { dependsOn(tasks.named("modrinth")) }

curseforge {
    apiToken = rootProject.layout.projectDirectory.file("curseforge_token.txt").asFile.takeIf { it.exists() }?.readText()?.trim() ?: System.getenv("CURSEFORGE_TOKEN")
    publications.create("neoforge") {
        projectId = "1346991"
        gameVersions.add(provider {
            val minecraftVersion = loom.minecraftVersion.get()
            val result = """(\d+)\.(\d+)\.(\d+)""".toRegex().matchEntire(minecraftVersion)!!
            GameVersion("minecraft-${result.groups[1]!!.value}-${result.groups[2]!!.value}", minecraftVersion)
        })
        gameVersions.add(GameVersion("environment", "server"))
        gameVersions.add(GameVersion("environment", "client"))
        gameVersions.add(GameVersion("modloader", "neoforge"))
        artifacts.create("main") {
            from(tasks.named("remapJar"))
            releaseType = if ("alpha" in project.version.toString()) ReleaseType.ALPHA else if ("beta" in project.version.toString()) ReleaseType.BETA else ReleaseType.RELEASE
            changelog {
                format = ChangelogFormat.MARKDOWN
                content = "This project maintains a comprehensive [CHANGELOG.md](https://github.com/MirrgieRiana/IFR25KU/blob/main/CHANGELOG.md) in Japanese."
            }
            relations {
                requiredDependency("forgified-fabric-api")
                requiredDependency("kotlin-for-forge")
                requiredDependency("owo-lib")
                requiredDependency("cloth-config")
                requiredDependency("terrablender-neoforge")
                requiredDependency("architectury-api")
            }
        }
    }
}
tasks.named("publishNeoforgePublicationToCurseForge").configure { dependsOn(tasks.named("remapJar")) }
rootProject.tasks.named("upload").configure { dependsOn(tasks.named("publishNeoforgePublicationToCurseForge")) }

tasks.named<ProcessResources>("processResources") {
    inputs.property("version", project.version)

    filesMatching("META-INF/neoforge.mods.toml") {
        expand(
            "version" to project.version,
            "forgified_fabric_api_version" to libs.versions.forgifiedFabricApi.get(),
            "kotlin_for_forge_version" to libs.versions.kotlinForForge.get(),
        )
    }
}

tasks.named<ShadowJar>("shadowJar") {
    configurations = listOf(project.configurations.getByName("shadowBundle"))
    archiveClassifier.set("dev-shadow")
    relocate("mirrg.kotlin", "miragefairy2024.shadow.mirrg.kotlin")
    relocate("mirrg.kotlin.helium", "miragefairy2024.shadow.mirrg.kotlin.helium")
    exclude("**/*refmap.json")
}

tasks.named<RemapJarTask>("remapJar") {
    inputFile.set(tasks.named<ShadowJar>("shadowJar").flatMap { it.archiveFile })
}
