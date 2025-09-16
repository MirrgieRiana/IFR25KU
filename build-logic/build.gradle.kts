import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.code.gson:gson:2.10.1")
}

tasks.withType<KotlinCompile>().all {
    compilerOptions {
        freeCompilerArgs.set(listOf("-Xcontext-receivers"))
    }
}

gradlePlugin {
    plugins {
        register("buildLogic") {
            id = "ifr25ku.buildlogic"
            implementationClass = "BuildLogicPlugin"
        }
    }
}
