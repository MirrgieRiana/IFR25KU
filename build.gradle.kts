import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import net.fabricmc.loom.api.LoomGradleExtensionAPI

plugins {
    id("dev.architectury.loom") version "1.7-SNAPSHOT" apply false
    id("maven-publish")
    kotlin("jvm") version "2.0.0" apply false
    id("architectury-plugin") version "3.4-SNAPSHOT"
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
    id("com.modrinth.minotaur") version "2.+"
    application
}

architectury {
    minecraft = rootProject.properties["minecraft_version"] as String
}

allprojects {
    group = rootProject.properties["maven_group"] as String
    version = rootProject.properties["mod_version"] as String
}

fun Iterable<Project>.f(block: Project.() -> Unit) = forEach { it.block() }
subprojects.filter { it.name in listOf("common", "fabric", "neoforge") }.f {
    apply(plugin = "kotlin")
    apply(plugin = "dev.architectury.loom")
    apply(plugin = "architectury-plugin")
    apply(plugin = "maven-publish")

    pluginManager.withPlugin("dev.architectury.loom") {
        val loom = extensions.getByType<LoomGradleExtensionAPI>()

        base {
            // Set up a suffixed format for the mod jar names, e.g. `example-fabric`.
            archivesName = "${rootProject.properties["archives_name"] as String}-${project.name}"
        }

        repositories {
            // Add repositories to retrieve artifacts from in here.
            // You should only use this when depending on other mods because
            // Loom adds the essential maven repositories to download Minecraft and libraries from automatically.
            // See https://docs.gradle.org/current/userguide/declaring_repositories.html
            // for more information about repositories.

            maven("https://maven.parchmentmc.org") // mapping
        }

        // configurationの追加のためにdependenciesより上にある必要がある
        extensions.configure<LoomGradleExtensionAPI> {
            silentMojangMappingsLicense()

            runs {
                // これにより、datagen API を実行する新しい gradle タスク "gradlew runDatagen" が追加されます。
                register("datagen") {
                    inherit(runs["server"])
                    name("Data Generation")
                    vmArg("-Dfabric-api.datagen")
                    vmArg("-Dfabric-api.datagen.output-dir=${file("src/main/generated")}")
                    vmArg("-Dfabric-api.datagen.modid=miragefairy2024")

                    runDir("build/datagen")
                }
                named("client") {
                    vmArgs += listOf("-Xmx4G")
                    programArgs += listOf("--username", "Player1")
                }
                named("server") {
                    runDir = "run_server" // ファイルロックを回避しクライアントと同時に起動可能にする
                }
            }
        }

        dependencies {
            // バージョンを変更するには、gradle.properties ファイルを参照してください。
            "minecraft"("net.minecraft:minecraft:${rootProject.properties["minecraft_version"] as String}")
            "mappings"(loom.layered {
                officialMojangMappings()
                parchment("org.parchmentmc.data:parchment-${rootProject.properties["minecraft_version"] as String}:${rootProject.properties["parchment_mappings"] as String}@zip")
            })
        }

        java {
            // Loomは自動的にsourcesJarをRemapSourcesJarタスクおよび "build" タスク(存在する場合)に添付します。
            // この行を削除すると、ソースが生成されません。
            withSourcesJar()

            sourceCompatibility = JavaVersion.VERSION_21
            targetCompatibility = JavaVersion.VERSION_21
        }

        tasks.withType<JavaCompile>().configureEach {
            options.release.set(21)
        }

        tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
            kotlinOptions {
                jvmTarget = "21"
                freeCompilerArgs = listOf("-Xcontext-receivers")
            }
        }

        tasks.named<Copy>("processResources") {
            exclude("**/*.pdn")
            exclude("**/*.scr.png")
            exclude("**/*.sc2.png")
            exclude("**/*.wav")
        }

        tasks.named<Jar>("jar") {
            from("LICENSE") {
                rename { "${it}_${project.base.archivesName.get()}" }
            }
        }

        tasks["modrinth"].dependsOn(tasks["modrinthSyncBody"])

        // https://github.com/modrinth/minotaur
        modrinth {
            token = System.getenv("MODRINTH_TOKEN")
            projectId = "miragefairy2024-kakera-unofficial"
            //versionNumber = project.mod_version
            versionType = "beta"
            uploadFile = tasks["remapJar"]
            //gameVersions = ["1.20.2"]
            //loaders = ["fabric"]
            syncBodyFrom = rootProject.file("MODRINTH-BODY.md").readText()
            dependencies {
                required.project("fabric-api")
                required.project("fabric-language-kotlin")
                required.project("faux-custom-entity-data")
                required.project("owo-lib")
                required.project("cloth-config")
                required.project("terrablender")
            }
        }

        // Mavenパブリケーションの構成
        publishing {
            publications {
                register<MavenPublication>("mavenJava") {
                    //artifactId = project.base.archivesName.get()
                    from(components["java"])
                }
            }

            // 公開の設定方法については、https://docs.gradle.org/current/userguide/publishing_maven.html を参照してください。
            repositories {
                // ここに公開するリポジトリを追加します。
                // 注意: このブロックには、最上位のブロックと同じ機能はありません。
                // ここのリポジトリは、依存関係を取得するためではなく、アーティファクトを公開するために使用されます。
            }
        }

    }
}

tasks.register("fetchMirrgKotlin") {
    doFirst {
        fun fetch(fileName: String) {
            val file = project.rootDir.resolve("lib/mirrg.kotlin").resolve(fileName)
            when {
                file.parentFile.isDirectory -> Unit
                file.parentFile.exists() -> throw RuntimeException("Already exists: ${file.parentFile}")
                !file.parentFile.mkdirs() -> throw RuntimeException("Could not create the directory: ${file.parentFile}")
            }
            file.writeBytes(uri("https://raw.githubusercontent.com/MirrgieRiana/mirrg.kotlin/main/src/main/java/$fileName").toURL().readBytes())
        }
        fetch("mirrg/kotlin/gson/hydrogen/Gson.kt")
        fetch("mirrg/kotlin/gson/hydrogen/JsonWrapper.kt")
        fetch("mirrg/kotlin/hydrogen/Lang.kt")
        fetch("mirrg/kotlin/hydrogen/Number.kt")
        fetch("mirrg/kotlin/hydrogen/String.kt")
        fetch("mirrg/kotlin/java/hydrogen/Number.kt")
        fetch("mirrg/kotlin/slf4j/hydrogen/Logging.kt")
    }
}

tasks.register("showSourceSets") {
    doLast {
        allprojects.forEach { project ->
            println("# Project: '${project.name}'")
            project.sourceSets.asMap.forEach { (sourceSetName, sourceSet) ->
                println("## Source Set: '$sourceSetName'")
                sourceSet.allSource.srcDirs.forEach { file ->
                    println("Src: $file")
                }
                sourceSet.compileClasspath.forEach { file ->
                    if (file.isDirectory) println("Classpath: $file")
                }
            }
        }
    }
}

tasks.register("buildPages") {
    dependsOn("runDatagen")
    doLast {
        println("Building pages...")
        val en = GsonBuilder().create().fromJson(File("fabric/src/main/generated/assets/miragefairy2024/lang/en_us.json").readText(), JsonElement::class.java).asJsonObject
        val ja = GsonBuilder().create().fromJson(File("fabric/src/main/generated/assets/miragefairy2024/lang/ja_jp.json").readText(), JsonElement::class.java).asJsonObject
        val keys = (en.keySet() + en.keySet()).sorted()

        mkdir("build/pages")
        """
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <title>MF24KU Lang Table</title>
    <style>
        #langTable {
            border-collapse: collapse;
        }

        #langTable th {
            background-color: #ddd;
        }
        #langTable tr:nth-child(even) {
            background-color: #eee;
        }

        #langTable th, #langTable td {
            padding: 0 0.5em;
        }
        #langTable th.value, #langTable td.value {
            border-left: 1px solid #888;
        }

        #langTable td.key {
            word-break: break-all;
        }
        #langTable td.value {
            white-space: pre-line;
            vertical-align: top;
        }
    </style>
</head>
<body>
<h1>MF24KU Lang Table</h1>
<table id="langTable">
    <colgroup>
        <col style="width: 20%;">
        <col style="width: 40%;">
        <col style="width: 40%;">
    </colgroup>
    <thead>
    <tr>
        <th class="key">Key</th>
        <th class="value">English</th>
        <th class="value">Japanese</th>
    </tr>
    </thead>
    <tbody>
        ${
            keys.joinToString("") { key ->
                """
<tr>
    <td class="key">$key</td>
    <td class="value">${(en.get(key) as JsonPrimitive?)?.asString ?: "-"}</td>
    <td class="value">${(ja.get(key) as JsonPrimitive?)?.asString ?: "-"}</td>
</tr>
                """.trimIndent()
            }
        }
    </tbody>
</table>
</body>
</html>
        """.let { File("build/pages/lang_table.html").writeText(it) }
        """
[MF24KU Lang Table](lang_table.html)
        """.let { File("build/pages/index.md").writeText(it) }
    }
}
