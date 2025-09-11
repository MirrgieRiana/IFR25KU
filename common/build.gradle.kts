architectury {
    common((rootProject.properties["enabled_platforms"] as String).split(","))
}

loom {
    splitEnvironmentSourceSets()
    mixin {
        add(sourceSets.main.get(), "miragefairy2024-common-main-refmap.json")
        add(sourceSets.named("client").get(), "miragefairy2024-common-client-refmap.json")
    }
}

sourceSets {
    main {
        resources {
            srcDir(file("src/generated/resources"))
        }
    }
}

// runServer runDatagenでArchitectury Transformerがクライアント用のクラスを変換しようとして落ちる対策のために成果物を分ける
configurations.create("mainNamedElements") {
    isCanBeResolved = false
    isCanBeConsumed = true
}
tasks.register<Jar>("mainJar") {
    destinationDirectory.set(layout.buildDirectory.dir("devlibs"))
    archiveClassifier.set("dev-main")
    from(sourceSets.named("main").get().output)
}
configurations.named("mainNamedElements") {
    outgoing.artifact(tasks.named("mainJar"))
}

repositories {
    maven("https://maven.shedaniel.me") // RoughlyEnoughItems
    maven("https://maven.wispforest.io/releases/") // owo-lib
    maven("https://maven.minecraftforge.net/") // com.github.glitchfiend:TerraBlender-fabric
}

dependencies {
    // We depend on Fabric Loader here to use the Fabric @Environment annotations,
    // which get remapped to the correct annotations on each platform.
    // Do NOT use other classes from Fabric Loader.
    modImplementation("net.fabricmc:fabric-loader:${rootProject.properties["fabric_loader_version"] as String}")

    modImplementation("net.fabricmc.fabric-api:fabric-api:${rootProject.properties["fabric_api_version"] as String}")

    // Architectury API. This is optional, and you can comment it out if you don't need it.
    modImplementation("dev.architectury:architectury:${rootProject.properties["architectury_api_version"] as String}")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${rootProject.properties["kotlin_coroutines_version"] as String}") // Kotlin Coroutines

    implementation(project(path = ":mirrg.kotlin"))

    // Library
    implementation("mirrg.kotlin:mirrg.kotlin.helium:${rootProject.properties["mirrg_kotlin_helium_version"] as String}")

    modRuntimeOnly("me.shedaniel:RoughlyEnoughItems-fabric:16.0.799")
    modCompileOnly("me.shedaniel:RoughlyEnoughItems-api-fabric:16.0.799")
    modCompileOnly("me.shedaniel:RoughlyEnoughItems-default-plugin-fabric:16.0.799")
    modCompileOnly("me.shedaniel.cloth:basic-math:0.6.1")

    modImplementation("io.wispforest:owo-lib:0.12.15.4+1.21")

    modApi("me.shedaniel.cloth:cloth-config:15.0.140")

    modImplementation("com.github.glitchfiend:TerraBlender-fabric:1.21.1-4.1.0.8")

}
configurations.named("architecturyTransformerClasspath") {
    extendsFrom(configurations.named("clientCompileClasspath").get()) // transformProductionFabric でバニラのclient用クラスが見れなくて死ぬ対策
}

tasks.named<Jar>("jar") {
    duplicatesStrategy = DuplicatesStrategy.WARN // clientとmainのclassの出力先を分けた関係で IFR25KU-common.kotlin_module がclientとmainで重複するため
}
