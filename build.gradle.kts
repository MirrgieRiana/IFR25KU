import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

plugins {
    id("dev.architectury.loom") version "1.7-SNAPSHOT" apply false
    //id("maven-publish")
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
    //apply(plugin = "maven-publish")

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

        // runServer runDatagenでArchitectury Transformerがクライアント用のクラスを変換しようとして落ちる対策のためにclassの出力先を分ける
        sourceSets.all {
            java.destinationDirectory.set(layout.buildDirectory.dir("classes/${this.name}/java"))
        }
        extensions.configure<KotlinProjectExtension>("kotlin") {
            sourceSets.all {
                kotlin.destinationDirectory.set(layout.buildDirectory.dir("classes/${this.name}/kotlin"))
            }
        }

        // configurationの追加のためにdependenciesより上にある必要がある
        extensions.configure<LoomGradleExtensionAPI> {
            silentMojangMappingsLicense()

            runs {
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

        // Mavenパブリケーションの構成
        /*
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
        */

    }
}


tasks.register("datagen")


tasks.register("uploadModrinth")

val generatedModrinthBodyFile = layout.projectDirectory.file("generated/src/modrinth/body.md")
tasks.register("generateModrinthBody") {
    group = "modrinth"

    outputs.file(generatedModrinthBodyFile)
    inputs.property("body", getModrinthBody())

    doLast {
        generatedModrinthBodyFile.asFile.parentFile.mkdirs()
        generatedModrinthBodyFile.asFile.writeText(getModrinthBody())
    }
}
tasks["datagen"].dependsOn(tasks["generateModrinthBody"])

modrinth {
    token = rootProject.layout.projectDirectory.file("modrinth_token.txt").asFile.takeIf { it.exists() }?.readText()?.trim() ?: System.getenv("MODRINTH_TOKEN")
    projectId = "ifr25ku"
    syncBodyFrom = provider { generatedModrinthBodyFile.asFile.readText() }
}
tasks["modrinthSyncBody"].dependsOn(tasks["generateModrinthBody"])
tasks["uploadModrinth"].dependsOn(tasks["modrinthSyncBody"])


tasks.register("fetchMirrgKotlin") {
    doFirst {
        fun fetch(fileName: String) {
            val file = project.rootDir.resolve("mirrg.kotlin/src/main/kotlin").resolve(fileName)
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
        fetch("mirrg/kotlin/java/hydrogen/Optional.kt")
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

tasks.register<Copy>("buildPages") {
    dependsOn(project("fabric").tasks["runDatagen"])

    fun computeTrs(): String {
        val en = GsonBuilder().create().fromJson(File("common/src/generated/resources/assets/miragefairy2024/lang/en_us.json").readText(), JsonElement::class.java).asJsonObject
        val ja = GsonBuilder().create().fromJson(File("common/src/generated/resources/assets/miragefairy2024/lang/ja_jp.json").readText(), JsonElement::class.java).asJsonObject
        val keys = (en.keySet() + ja.keySet()).sorted()
        return keys.joinToString("") { key ->
            listOf(
                """<tr>""",
                """<td class="key">$key</td>""",
                """<td class="value">${(en.get(key) as JsonPrimitive?)?.asString ?: "-"}</td>""",
                """<td class="value">${(ja.get(key) as JsonPrimitive?)?.asString ?: "-"}</td>""",
                """</tr>""",
            ).joinToString("\n") { it }
        }
    }

    from("pages") {
        include("**/*")

        filesMatching("/lang_table.html") {
            expand("trs" to computeTrs())
        }
    }

    into(layout.buildDirectory.dir("pages"))
}

tasks.register("configurations") {
    group = "help"
    doLast {
        rootProject.allprojects.forEach { project ->
            println("=== Project: ${project.path} ===")
            project.configurations.forEach { configuration ->
                println("- ${if (configuration.isCanBeConsumed) "C" else "-"}${if (configuration.isCanBeResolved) "R" else "-"} ${configuration.name}")
            }
            println()
        }
    }
}
