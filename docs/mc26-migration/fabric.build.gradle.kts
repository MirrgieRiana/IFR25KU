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
    id("net.fabricmc.fabric-loom") version "1.18.4"
    id("com.gradleup.shadow")
}

// 配布するjarへ同梱するものを集める構成なのだ～🌱
val shadowBundle = configurations.create("shadowBundle") {
    isCanBeResolved = true
    isCanBeConsumed = false
}

val minecraft_version = rootProject.property("minecraft_version") as String
val fabric_loader_version = rootProject.property("fabric_loader_version") as String
val fabric_api_version = rootProject.property("fabric_api_version") as String
val architectury_version = rootProject.property("architectury_version") as String

evaluationDependsOn(":common") // commonのclientソースセットを直接参照するために必要なのだ～🌱

// dependencies{}ブロックの中では project(...) が ProjectDependency を返す別の関数に化けてしまうので、
// 外側であらかじめ :common の client ソースセットの出力を取得しておくのだ～🌱
val commonClientSourceSetOutput = project(":common").extensions.getByType<SourceSetContainer>().getByName("client").output

base {
    archivesName = "mc26experiment-fabric"
}

loom {
    splitEnvironmentSourceSets()

    // datagen の run を登録できるかを確かめるのだ～🌱
    runs {
        register("datagen") {
            inherit(runs["server"])
            // 非推奨のname/vmArg/runDirの代わりに、RunConfigurationのプロパティへ設定するのだ～🌱
            getDisplayName().set("Data Generation")
            getJvmArguments().add("-Dfabric-api.datagen")
            getJvmArguments().add("-Dfabric-api.datagen.output-dir=" + rootProject.file("common/src/generated/resources"))
            getJvmArguments().add("-Dfabric-api.datagen.modid=mc26experiment")
            getRunDirectory().set(layout.buildDirectory.dir("datagen"))
        }
    }

    mods {
        register("mc26experiment") {
            sourceSet(sourceSets.main.get())
            sourceSet(sourceSets.getByName("client"))
            sourceSet("main", ":common")
            sourceSet("client", ":common")
        }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraft_version")
    implementation("net.fabricmc:fabric-loader:$fabric_loader_version")
    implementation("net.fabricmc.fabric-api:fabric-api:$fabric_api_version")

    implementation("dev.architectury:architectury-fabric:$architectury_version")

    implementation(kotlin("stdlib"))

    // 26.1.2は難読化が無いので、非リマップ版Loom(net.fabricmc.fabric-loom)には
    // 旧来の「namedElements」のような特別な configuration が存在しないのだ～🌱
    // (:common:outgoingVariantsで確認した結果、java/kotlinプラグイン標準のapiElements/runtimeElementsしか無いのだ～🌱)
    // だから、ただのプロジェクト依存をそのまま使えばいいのだ～🌱
    implementation(project(":common"))
    "clientImplementation"(commonClientSourceSetOutput)

    shadowBundle(project(":common"))
    shadowBundle(project(":mirrg"))
    shadowBundle(kotlin("stdlib"))
}

tasks.named<ShadowJar>("shadowJar") {
    // commonのclientソースセットは、プロジェクト依存には乗らないから、直接足すのだ～🌱
    from(commonClientSourceSetOutput)
    from(sourceSets.getByName("client").output)
    configurations = listOf(shadowBundle)
    archiveClassifier.set("all")
    // mirrgは他のMODも同梱しうるため、名前を衝突させないように移すのだ～🌱
    relocate("mirrg", "mc26experiment.shadow.mirrg")

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
    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}
