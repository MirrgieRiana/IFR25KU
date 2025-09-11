import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import dev.architectury.plugin.TransformingTask
import dev.architectury.transformer.transformers.base.AssetEditTransformer
import net.fabricmc.loom.task.RemapJarTask

plugins {
    id("com.github.johnrengelman.shadow")
    id("com.modrinth.minotaur")
}

architectury {
    platformSetupLoomIde()
    fabric()
}

loom {
    splitEnvironmentSourceSets()
    accessWidenerPath = file("src/main/resources/miragefairy2024.accesswidener")
    mixin {
        add(sourceSets.main.get(), "miragefairy2024-fabric-main-refmap.json")
        add(sourceSets.named("client").get(), "miragefairy2024-fabric-client-refmap.json")
    }
}

configurations {
    val commonMain by creating {
        isCanBeResolved = true
        isCanBeConsumed = false
    }
    getByName("compileClasspath").extendsFrom(commonMain)
    getByName("runtimeClasspath").extendsFrom(commonMain)
    getByName("developmentFabric").extendsFrom(commonMain)

    val commonClient by creating {
        isCanBeResolved = true
        isCanBeConsumed = false
    }
    getByName("clientCompileClasspath").extendsFrom(commonClient)
    getByName("clientRuntimeClasspath").extendsFrom(commonClient)

    // Files in this configuration will be bundled into your mod using the Shadow plugin.
    // Don't use the `shadow` configuration from the plugin itself as it's meant for excluding files.
    val shadowBundle by creating {
        isCanBeResolved = true
        isCanBeConsumed = false
    }
}

repositories {
    maven("https://maven.shedaniel.me") // RoughlyEnoughItems
    maven("https://maven.wispforest.io/releases/") // owo-lib
    maven("https://maven.minecraftforge.net/") // com.github.glitchfiend:TerraBlender-fabric
    maven("https://www.cursemaven.com") { // Jade
        content { includeGroup("curse.maven") }
    }
}

loom {
    runs {
        // これにより、datagen API を実行する新しい gradle タスク "gradlew runDatagen" が追加されます。
        register("datagen") {
            inherit(runs["server"])
            name("Data Generation")
            vmArg("-Dfabric-api.datagen")
            vmArg("-Dfabric-api.datagen.output-dir=${rootProject.file("common/src/generated/resources")}")
            vmArg("-Dfabric-api.datagen.modid=miragefairy2024")
            vmArg("-Dmiragefairy2024.datagen.platform=common")

            runDir("build/datagen")
        }
        register("datagenNeoForge") {
            inherit(runs["server"])
            name("NeoForge Data Generation")
            vmArg("-Dfabric-api.datagen")
            vmArg("-Dfabric-api.datagen.output-dir=${rootProject.file("neoforge/src/generated/resources")}")
            vmArg("-Dfabric-api.datagen.modid=miragefairy2024")
            vmArg("-Dmiragefairy2024.datagen.platform=neoforge")

            runDir("build/datagen")
        }
    }
}
rootProject.tasks.named("datagen").configure { dependsOn(tasks.named("runDatagen")) }
rootProject.tasks.named("datagen").configure { dependsOn(tasks.named("runDatagenNeoForge")) }

dependencies {

    // Loader
    modImplementation("net.fabricmc:fabric-loader:${rootProject.properties["fabric_loader_version"] as String}") // Fabric Loader

    // Platform
    modImplementation("net.fabricmc.fabric-api:fabric-api:${rootProject.properties["fabric_api_version"] as String}") // Fabric API
    // modImplementation("net.fabricmc.fabric-api:fabric-api-deprecated:${rootProject.properties["fabric_api_version"] as String}") // Deprecated Fabric API
    modImplementation("net.fabricmc:fabric-language-kotlin:${rootProject.properties["fabric_kotlin_version"] as String}") // Kotlin
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${rootProject.properties["kotlin_coroutines_version"] as String}") // Kotlin Coroutines
    modImplementation("dev.architectury:architectury-fabric:${rootProject.properties["architectury_api_version"] as String}") // Architectury API

    // Module
    "commonMain"(project(path = ":common", configuration = "mainNamedElements")) { isTransitive = false } // common
    "commonClient"(project(path = ":common", configuration = "namedElements")) { isTransitive = false } // common
    //"clientImplementation"(rootProject.project("common").sourceSets.named("client").get().output) // common client
    "shadowBundle"(project(path = ":common", configuration = "transformProductionFabric")) // common shadow
    implementation(project(path = ":mirrg.kotlin")) // mirrg.kotlin
    "shadowBundle"(project(path = ":mirrg.kotlin")) { isTransitive = false } // mirrg.kotlin shadow

    // Library
    implementation("mirrg.kotlin:mirrg.kotlin.helium:${rootProject.properties["mirrg_kotlin_helium_version"] as String}")
    "shadowBundle"("mirrg.kotlin:mirrg.kotlin.helium:${rootProject.properties["mirrg_kotlin_helium_version"] as String}") { isTransitive = false }

    modRuntimeOnly("me.shedaniel:RoughlyEnoughItems-fabric:16.0.799")
    modCompileOnly("me.shedaniel:RoughlyEnoughItems-api-fabric:16.0.799")
    modCompileOnly("me.shedaniel:RoughlyEnoughItems-default-plugin-fabric:16.0.799")
    modCompileOnly("me.shedaniel.cloth:basic-math:0.6.1")

    modCompileOnly("curse.maven:jade-324717:6738760")
    modRuntimeOnly("curse.maven:jade-324717:6738760")

    modImplementation("io.wispforest:owo-lib:0.12.15.4+1.21")

    modApi("me.shedaniel.cloth:cloth-config:15.0.140")

    modImplementation("com.github.glitchfiend:TerraBlender-fabric:1.21.1-4.1.0.8")

}

// https://github.com/modrinth/minotaur
modrinth {
    token = System.getenv("MODRINTH_TOKEN")
    projectId = "ifr25ku"
    //versionNumber = project.mod_version
    versionType = if ("alpha" in project.version.toString()) "alpha" else if ("beta" in project.version.toString()) "beta" else "release"
    uploadFile = tasks["remapJar"]
    //gameVersions = ["1.20.2"]
    //loaders = ["fabric"]
    changelog.set(getChangeLog(rootProject.file("CHANGELOG.md"), rootProject.properties["mod_version"] as String))
    dependencies {
        required.project("fabric-api")
        required.project("fabric-language-kotlin")
        required.project("owo-lib")
        required.project("cloth-config")
        required.project("terrablender")
        required.project("architectury-api")
    }
}
rootProject.tasks.named("uploadModrinth").configure { dependsOn(tasks.named("modrinth")) }

tasks.named<ProcessResources>("processResources") {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}

tasks.named<Jar>("jar") {
    from(sourceSets["client"].output)
}

tasks.named<ShadowJar>("shadowJar") {
    from(sourceSets["client"].output)
    configurations = listOf(project.configurations.getByName("shadowBundle"))
    archiveClassifier.set("dev-shadow")
    relocate("mirrg.kotlin", "miragefairy2024.shadow.mirrg.kotlin")
    relocate("mirrg.kotlin.helium", "miragefairy2024.shadow.mirrg.kotlin.helium")
}

tasks.named<RemapJarTask>("remapJar") {
    inputFile.set(tasks.named<ShadowJar>("shadowJar").flatMap { it.archiveFile })
}

/**
 * architecturyはsplit sourceをサポートしないためデフォルトrefmapファイル名を想定してjsonを変換する
 * @see dev.architectury.plugin.transformers.AddRefmapName
 */
rootProject.project("common").tasks.named<TransformingTask>("transformProductionFabric") {
    this(AssetEditTransformer { _, output ->
        val gson = GsonBuilder().setPrettyPrinting().create()
        val listeners = mutableListOf<() -> Unit>()
        output.handle { path, bytes ->
            fun convert(mixinJsonPath: String, refmapFileName: String) {
                if (path == mixinJsonPath) {
                    listeners += {
                        println("Converting `$mixinJsonPath` to use refmap `$refmapFileName`")
                        output.modifyFile(path) {
                            val json = gson.fromJson(bytes.decodeToString(), JsonObject::class.java)
                            json.addProperty("refmap", refmapFileName)
                            gson.toJson(json).encodeToByteArray()
                        }
                    }
                }
            }
            convert("/miragefairy2024.mixins.json", "miragefairy2024-common-main-refmap.json")
            convert("/miragefairy2024.client.mixins.json", "miragefairy2024-common-client-refmap.json")
        }
        listeners.forEach {
            it()
        }
    })
}
