import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.getByType

plugins {
    kotlin("jvm")
    id("net.neoforged.moddev") version "2.0.148"
    id("com.gradleup.shadow")
}

val shadowBundle by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
}

val neoforge_version = rootProject.property("neoforge_version") as String
val architectury_version = rootProject.property("architectury_version") as String

base {
    archivesName = "mc26experiment-neoforge"
}

// NeoForge(ModDevGradle)には、Fabric Loomのsplit EnvironmentSourceSets()のような
// 既製の機能が無いので、「Isolated Source Sets」の手順で手動でclientソースセットを作るのだ～🌱
sourceSets {
    create("client")
}

// dependencies{}ブロックの中では project(...) が ProjectDependency を返す別の関数に化けてしまうので、
// 外側であらかじめ :common のソースセットを取得しておくのだ～🌱
val commonSourceSets = project(":common").extensions.getByType<SourceSetContainer>()
val commonMainSourceSet = commonSourceSets.getByName("main")
val commonClientSourceSetOutput = commonSourceSets.getByName("client").output

neoForge {
    version = neoforge_version

    addModdingDependenciesTo(sourceSets.getByName("client"))

    mods {
        register("mc26experiment") {
            // commonはfabricとneoforgeの2つから参照される独立したプロジェクトなので、
            // resources(mixins.jsonなど)も含めて、ここで明示的にグループ化する必要があるのだ～🌱
            // (そうしないと、FMLがmixins.jsonをneoforge側のクラスパスの中にしか探さず、見つからないのだ～🌱)
            sourceSet(commonMainSourceSet)
            sourceSet(commonSourceSets.getByName("client"))
            sourceSet(sourceSets.main.get())
            sourceSet(sourceSets.getByName("client"))
        }
    }

    runs {
        register("client") {
            client()
        }
        register("server") {
            server()
        }
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    jarJar(kotlin("stdlib"))

    implementation("dev.architectury:architectury-neoforge:$architectury_version")

    implementation(project(":common"))
    "clientImplementation"(sourceSets.main.get().output) // neoforgeモジュール自身のmainソースセットにも依存させるのだ～🌱
    "clientImplementation"(commonClientSourceSetOutput)

    shadowBundle(project(":common"))
    shadowBundle(kotlin("stdlib"))
}

tasks.named<ShadowJar>("shadowJar") {
    from(commonClientSourceSetOutput)
    from(sourceSets.getByName("client").output)
    configurations = listOf(shadowBundle)
    archiveClassifier.set("all")
    relocate("kotlin", "mc26experiment.shadow.kotlin") {
        // kotlin.Metadataは全バージョンでメンバー構成が同じだから、relocateせずに素のままにしておくのだ～🌱
        exclude("kotlin.Metadata")
    }
    // kotlin-stdlibが引き込むIDE用のアノテーションは、実行時には要らないのだ～🌱
    exclude("org/jetbrains/annotations/**")
    exclude("org/intellij/**")
    exclude("META-INF/maven/**")

    // relocateがリソースのファイル名の中の「kotlin」まで置換してしまうので、.kotlin_moduleの名前を戻すのだ～🌱
    doLast {
        val jarFile = archiveFile.get().asFile
        val tempFile = File(jarFile.parentFile, jarFile.name + ".tmp")
        ZipFile(jarFile).use { zipFile ->
            ZipOutputStream(tempFile.outputStream().buffered()).use { zipOutputStream ->
                zipFile.entries().asSequence().forEach { zipEntry ->
                    val name = zipEntry.name.replace(".shadow.kotlin_module", ".kotlin_module")
                    zipOutputStream.putNextEntry(ZipEntry(name))
                    if (!zipEntry.isDirectory) zipFile.getInputStream(zipEntry).use { it.copyTo(zipOutputStream) }
                    zipOutputStream.closeEntry()
                }
            }
        }
        tempFile.renameTo(jarFile)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(25)
}

tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_25)
    }
}

tasks.named<ProcessResources>("processResources") {
    inputs.property("version", project.version)
    filesMatching("META-INF/neoforge.mods.toml") {
        expand("version" to project.version)
    }
}

tasks.named<Jar>("jar") {
    from(sourceSets.getByName("client").output)
}
