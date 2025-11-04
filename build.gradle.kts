import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    id("dev.architectury.loom") version "1.7-SNAPSHOT" apply false
    //id("maven-publish")
    kotlin("jvm") version "2.0.0" apply false
    id("architectury-plugin") version "3.4-SNAPSHOT"
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
    id("com.modrinth.minotaur") version "2.+"
    id("io.github.themrmilchmann.curseforge-publish") version "0.8.0"
    application
    id("ifr25ku.buildlogic")
}

architectury {
    minecraft = libs.versions.minecraft.get()
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
        val libs = rootProject.extensions.getByType<VersionCatalogsExtension>().named("libs")
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
            maven(uri(rootProject.file("maven"))) {
                content { includeGroup("dev.emi") }
            }
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
            "minecraft"("net.minecraft:minecraft:${libs.findVersion("minecraft").get().requiredVersion}")
            "mappings"(loom.layered {
                officialMojangMappings()
                parchment("org.parchmentmc.data:parchment-${libs.findVersion("minecraft").get().requiredVersion}:${libs.findVersion("parchmentMappings").get().requiredVersion}@zip")
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

        tasks.withType<KotlinJvmCompile>().configureEach {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_21)
                freeCompilerArgs.add("-Xcontext-receivers")
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

    tasks.register<UnpackSourcesTask>("unpackSources") {
        dependsOn(tasks.firstOrNull { it.name == "genSources" })
    }

}


tasks.register("datagen")


tasks.register("upload")

tasks.register("generateModBody")
tasks.named("datagen").configure { dependsOn(tasks.named("generateModBody")) }

val generatedModrinthBodyFile = layout.projectDirectory.file("generated/src/modrinth/body.md")
tasks.register("generateModrinthModBody") {
    group = "documentation"

    outputs.file(generatedModrinthBodyFile)
    inputs.property("body", getModBody(MarkdownType.MODRINTH))

    doLast {
        generatedModrinthBodyFile.asFile.parentFile.mkdirs()
        generatedModrinthBodyFile.asFile.writeText(getModBody(MarkdownType.MODRINTH))
    }
}
tasks.named("generateModBody").configure { dependsOn(tasks.named("generateModrinthModBody")) }

val generatedCurseforgeBodyFile = layout.projectDirectory.file("generated/src/curseforge/body.md")
tasks.register("generateCurseforgeModBody") {
    group = "documentation"

    outputs.file(generatedCurseforgeBodyFile)
    inputs.property("body", getModBody(MarkdownType.CURSEFORGE))

    doLast {
        generatedCurseforgeBodyFile.asFile.parentFile.mkdirs()
        generatedCurseforgeBodyFile.asFile.writeText(getModBody(MarkdownType.CURSEFORGE))
    }
}
tasks.named("generateModBody").configure { dependsOn(tasks.named("generateCurseforgeModBody")) }

modrinth {
    token = rootProject.layout.projectDirectory.file("modrinth_token.txt").asFile.takeIf { it.exists() }?.readText()?.trim() ?: System.getenv("MODRINTH_TOKEN")
    projectId = "ifr25ku"
    syncBodyFrom = provider { generatedModrinthBodyFile.asFile.readText() }
}
tasks.named("modrinthSyncBody").configure { dependsOn(tasks.named("generateModrinthModBody")) }
tasks.named("upload").configure { dependsOn(tasks.named("modrinthSyncBody")) }

curseforge {
    apiToken = rootProject.layout.projectDirectory.file("curseforge_token.txt").asFile.takeIf { it.exists() }?.readText()?.trim() ?: System.getenv("CURSEFORGE_TOKEN")
}

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
    dependsOn(project("fabric").tasks.named("runDatagen"))

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
            filter {
                filteringCharset = "UTF-8"
                it.replace("<%= trs %>", computeTrs())
            }
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

run {
    fun toMarkdownTable(columns: List<String>, records: List<List<String>>): String {
        return listOf(
            columns,
            columns.map { column -> "-".repeat(column.length) },
            *records.toTypedArray(),
        ).joinToString("\n") { row -> "| ${row.joinToString(" | ")} |" }
    }

    fun output(outputFileName: String, content: String) {
        val outFile = layout.buildDirectory.file(outputFileName).get().asFile
        outFile.parentFile.mkdirs()
        outFile.writeText(content)

        println("Wrote to ${outFile.absolutePath}")
    }

    tasks.register("generateCurseforgeTable")

    tasks.register("generateCurseforgeVersionTypeTable") {
        group = "help"
        doLast {
            val versionTypes = CurseforgeClient(curseforge.apiToken.get()).versionTypes
            val markdown = toMarkdownTable(
                listOf("id", "name", "slug"),
                versionTypes
                    .sortedBy { """\d+""".toRegex().replace(it.slug) { m -> m.value.padStart(20, '0') } }
                    .map { versionType -> listOf(versionType.id, versionType.name, versionType.slug).map { "$it" } },
            )
            output("curseforgeTable/curseforge_version_types.md", markdown)
        }
    }
    tasks.named("generateCurseforgeTable").configure { dependsOn(tasks.named("generateCurseforgeVersionTypeTable")) }

    tasks.register("generateCurseforgeVersionTable") {
        group = "help"
        doLast {
            val versions = CurseforgeClient(curseforge.apiToken.get()).versions
            val markdown = toMarkdownTable(
                listOf("id", "name", "slug", "gameVersionTypeID", "apiVersion"),
                versions
                    .sortedBy { """\d+""".toRegex().replace(it.slug) { m -> m.value.padStart(20, '0') } }
                    .map { version -> listOf(version.id, version.name, version.slug, version.gameVersionTypeID, version.apiVersion).map { "$it" } },
            )
            output("curseforgeTable/curseforge_versions.md", markdown)
        }
    }
    tasks.named("generateCurseforgeTable").configure { dependsOn(tasks.named("generateCurseforgeVersionTable")) }
}
