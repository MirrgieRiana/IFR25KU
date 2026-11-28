pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        maven("https://maven.neoforged.net/releases") { name = "NeoForged" }
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "mc26-experiment"

include("mirrg")

include("common")
include("fabric")
include("neoforge")
