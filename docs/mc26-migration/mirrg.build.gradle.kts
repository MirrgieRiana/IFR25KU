import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

// mirrg.kotlinはMinecraftのクラスを1個も使わないため、プラットフォームのプラグインを1個も要らないのだ～🌱
plugins {
    kotlin("jvm")
}

val gson_version = rootProject.property("gson_version") as String
val slf4j_version = rootProject.property("slf4j_version") as String

dependencies {
    // Minecraftが同じ版を持ち込むため、実行時にはMinecraft側のものが使われるのだ～🌱
    compileOnly("com.google.code.gson:gson:$gson_version")
    compileOnly("org.slf4j:slf4j-api:$slf4j_version")
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
