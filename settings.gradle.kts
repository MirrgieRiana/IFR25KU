pluginManagement {
    repositories {
        maven(url = "https://maven.fabricmc.net/")
        maven(url = "https://maven.architectury.dev/")
        maven(url = "https://files.minecraftforge.net/maven/")
        gradlePluginPortal()
    }
    includeBuild("build-logic")
}

rootProject.name = "IFR25KU"

include("mirrg.kotlin")

include("common")
include("fabric")
include("neoforge")
