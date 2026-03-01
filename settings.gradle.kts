pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven(url = "https://maven.fabricmc.net/")
        maven(url = "https://maven.minecraftforge.net/")
        maven(url = "https://maven.architectury.dev/")
    }
    includeBuild("build-logic")
}

rootProject.name = "IFR25KU"

include("mirrg.kotlin")

include("common")
include("fabric")
if ("neoforge" in providers.gradleProperty("enabled_platforms").getOrElse("fabric,neoforge").split(",")) {
    include("neoforge")
}
