import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    `java-library`
    kotlin("jvm")
    id("net.fabricmc.fabric-loom") version "1.18.4"
}

val minecraft_version = rootProject.property("minecraft_version") as String
val fabric_loader_version = rootProject.property("fabric_loader_version") as String
val architectury_version = rootProject.property("architectury_version") as String

// commonは、fabric側のビルドシステム(Fabric Loom)を間借りして、
// 難読化されていない26.1.2のMinecraftクラスパスだけを得るために使うのだ～🌱
// 26.1.2は難読化が無いので、ここでコンパイルしたクラスは、
// NeoForge側からもそのまま同じクラス名で解決できるのだ～🌱

dependencies {
    // mirrgは、Minecraftを1個も使わない純粋なKotlinのサブプロジェクトなのだ～🌱
    api(project(":mirrg"))

    minecraft("com.mojang:minecraft:$minecraft_version")

    // Mixinのアノテーションをコンパイルするためだけに必要なのだ～🌱
    // 実行時のMixin本体は、fabric-loaderとNeoForgeが、それぞれ自分で持ち込むのだ～🌱
    compileOnly("net.fabricmc:fabric-loader:$fabric_loader_version")

    // Architectury APIの共通部分なのだ～🌱 実行時はfabric版かneoforge版が持ち込むのだ～🌱
    compileOnly("dev.architectury:architectury:$architectury_version")
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

loom {
    splitEnvironmentSourceSets()
}
