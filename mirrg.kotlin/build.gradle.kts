import net.fabricmc.loom.api.LoomGradleExtensionAPI

plugins {
    kotlin("jvm") version "2.1.21"
    id("dev.architectury.loom") version "1.9.436"
}

extensions.configure<LoomGradleExtensionAPI> {
    silentMojangMappingsLicense()
}

repositories {
    mavenCentral()
    maven("https://maven.parchmentmc.org") // mapping
}

dependencies {
    "minecraft"("net.minecraft:minecraft:${libs.versions.minecraft.get()}")
    "mappings"(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${libs.versions.minecraft.get()}:${libs.versions.parchmentMappings.get()}@zip")
    })
}
