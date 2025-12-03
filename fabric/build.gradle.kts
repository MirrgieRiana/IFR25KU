import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import dev.architectury.plugin.TransformingTask
import dev.architectury.transformer.transformers.base.AssetEditTransformer
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
    maven("https://maven.shedaniel.me") { // RoughlyEnoughItems
        content {
            includeGroup("me.shedaniel")
        }
    }
    maven("https://maven.terraformersmc.com/releases") { // EMI
        content {
            includeGroup("dev.emi")
        }
    }
    maven("https://maven.wispforest.io/releases/") { // owo-lib
        content {
            includeGroup("io.wispforest")
            includeGroup("io.wispforest.endec")
        }
    }
    maven("https://maven.minecraftforge.net/") { // com.github.glitchfiend:TerraBlender-fabric
        content {
            includeModule("com.github.glitchfiend", "TerraBlender-fabric")
        }
    }
    maven("https://www.cursemaven.com") { // Jade
        content {
            includeModule("curse.maven", "jade-324717")
        }
    }
    maven("https://raw.githubusercontent.com/MirrgieRiana/mirrg.kotlin/refs/heads/maven/maven/") { // mirrg.kotlin.helium
        content {
            includeGroup("mirrg.kotlin")
        }
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
    modImplementation("net.fabricmc:fabric-loader:${libs.versions.fabricLoader.get()}") // Fabric Loader

    // Platform
    modImplementation("net.fabricmc.fabric-api:fabric-api:${libs.versions.fabricApi.get()}") // Fabric API
    // modImplementation("net.fabricmc.fabric-api:fabric-api-deprecated:${libs.versions.fabricApi.get()}") // Deprecated Fabric API
    modImplementation("net.fabricmc:fabric-language-kotlin:${libs.versions.fabricKotlin.get()}") // Kotlin
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${libs.versions.kotlinCoroutines.get()}") // Kotlin Coroutines
    modImplementation("dev.architectury:architectury-fabric:${libs.versions.architecturyApi.get()}") // Architectury API

    // Module
    "commonMain"(project(path = ":common", configuration = "mainNamedElements")) { isTransitive = false } // common
    "commonClient"(project(path = ":common", configuration = "namedElements")) { isTransitive = false } // common
    //"clientImplementation"(rootProject.project("common").sourceSets.named("client").get().output) // common client
    "shadowBundle"(project(path = ":common", configuration = "transformProductionFabric")) // common shadow
    implementation(project(path = ":mirrg.kotlin")) // mirrg.kotlin
    "shadowBundle"(project(path = ":mirrg.kotlin")) { isTransitive = false } // mirrg.kotlin shadow

    // Library
    implementation("mirrg.kotlin:${libs.versions.mirrgKotlinHelium.get()}")
    "shadowBundle"("mirrg.kotlin:${libs.versions.mirrgKotlinHelium.get()}") { isTransitive = false }

    modRuntimeOnly("me.shedaniel:RoughlyEnoughItems-fabric:${libs.versions.rei.get()}")
    modCompileOnly("me.shedaniel:RoughlyEnoughItems-api-fabric:${libs.versions.rei.get()}")
    modCompileOnly("me.shedaniel:RoughlyEnoughItems-default-plugin-fabric:${libs.versions.rei.get()}")
    modCompileOnly("me.shedaniel.cloth:basic-math:${libs.versions.clothBasicMath.get()}")

    modCompileOnly("curse.maven:jade-324717:${libs.versions.jadeCurseForgeFabric.get()}")
    modRuntimeOnly("curse.maven:jade-324717:${libs.versions.jadeCurseForgeFabric.get()}")

    modImplementation("io.wispforest:owo-lib:${libs.versions.owoLibFabric.get()}")

    modApi("me.shedaniel.cloth:cloth-config:${libs.versions.clothConfig.get()}")

    modImplementation("com.github.glitchfiend:TerraBlender-fabric:${libs.versions.terraBlenderFabric.get()}")

    modCompileOnly("dev.emi:emi-fabric:${libs.versions.emi.get()}:api")
    modRuntimeOnly("dev.emi:emi-fabric:${libs.versions.emi.get()}")

}

// https://github.com/modrinth/minotaur
modrinth {
    token = System.getenv("MODRINTH_TOKEN")
    projectId = "ifr25ku"
    versionNumber = project.version.toString() + "+fabric" // Minecraftバージョンを入れるとalphaバージョンで長すぎて蹴られる
    versionType = if ("alpha" in project.version.toString()) "alpha" else if ("beta" in project.version.toString()) "beta" else "release"
    uploadFile = tasks["remapJar"]
    additionalFiles.add(tasks["sourcesJar"])
    //gameVersions = ["1.20.2"]
    //loaders = ["fabric"]
    changelog.set("This project maintains a comprehensive [CHANGELOG](https://mirrgieriana.github.io/IFR25KU/CHANGELOG.html) in Japanese.")
    dependencies {
        required.project("fabric-api")
        required.project("fabric-language-kotlin")
        required.project("owo-lib")
        required.project("cloth-config")
        required.project("terrablender")
        required.project("architectury-api")
        optional.project("rei")
        optional.project("emi")
    }
}
rootProject.tasks.named("upload").configure { dependsOn(tasks.named("modrinth")) }

curseforge {
    apiToken = rootProject.layout.projectDirectory.file("curseforge_token.txt").asFile.takeIf { it.exists() }?.readText()?.trim() ?: System.getenv("CURSEFORGE_TOKEN")
    publications.create("fabric") {
        projectId = "1346991"
        val client by lazy { CurseforgeClient(curseforge.apiToken.get()) }
        gameVersions.add(provider { client.createMinecraftGameVersion(loom.minecraftVersion.get()).let { GameVersion(it.first, it.second) } })
        gameVersions.add(provider { client.createGameVersion("environment", "server").let { GameVersion(it.first, it.second) } })
        gameVersions.add(provider { client.createGameVersion("environment", "client").let { GameVersion(it.first, it.second) } })
        gameVersions.add(provider { client.createGameVersion("modloader", "fabric").let { GameVersion(it.first, it.second) } })
        artifacts.create("main") {
            from(tasks.named("remapJar"))
            displayName.set("")
            releaseType = if ("alpha" in project.version.toString()) ReleaseType.ALPHA else if ("beta" in project.version.toString()) ReleaseType.BETA else ReleaseType.RELEASE
            changelog {
                format = ChangelogFormat.MARKDOWN
                content = "This project maintains a comprehensive [CHANGELOG](https://mirrgieriana.github.io/IFR25KU/CHANGELOG.html) in Japanese."
            }
            relations {
                requiredDependency("fabric-api")
                requiredDependency("fabric-language-kotlin")
                requiredDependency("owo-lib")
                requiredDependency("cloth-config")
                requiredDependency("terrablender-fabric")
                requiredDependency("architectury-api")
                optionalDependency("roughly-enough-items")
                optionalDependency("emi")
            }
        }
        artifacts.create("sources") {
            from(tasks.named("sourcesJar"))
        }
    }
}
tasks.named("publishFabricPublicationToCurseForge").configure { dependsOn(tasks.named("remapJar")) }
rootProject.tasks.named("upload").configure { dependsOn(tasks.named("publishFabricPublicationToCurseForge")) }

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
    archiveBaseName = "ifr25ku" // Modrinth・CurseForgeのプロジェクト名に準拠
    version = rootProject.version.toString() + "+fabric"
}

tasks.named<Jar>("sourcesJar") {
    with(project(":common").tasks.named<Jar>("sourcesJar").get())
    archiveBaseName = "ifr25ku" // Modrinth・CurseForgeのプロジェクト名に準拠
    version = rootProject.version.toString() + "+fabric"
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
