import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    // Gradle のローカルビルドロジック（プリコンパイルドプラグイン）として扱う
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.code.gson:gson:2.10.1")
}

// Kotlin コンパイラオプション
tasks.withType<KotlinCompile>().all {
    compilerOptions {
        freeCompilerArgs.set(listOf("-Xcontext-receivers"))
    }
}

// プラグイン定義（実装は最小限でOK）
gradlePlugin {
    plugins {
        register("ifr25kuBuildLogic") {
            id = "ifr25ku.buildlogic"
            implementationClass = "ifr25ku.buildlogic.BuildLogicPlugin"
        }
    }
}