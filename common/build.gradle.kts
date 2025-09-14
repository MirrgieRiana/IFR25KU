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
    maven("https://maven.terraformersmc.com/releases") // EMI
    maven("https://maven.wispforest.io/releases/") // owo-lib
    maven("https://maven.minecraftforge.net/") // com.github.glitchfiend:TerraBlender-fabric
    maven("https://raw.githubusercontent.com/MirrgieRiana/mirrg.kotlin/refs/heads/maven/maven/") // mirrg.kotlin.helium
}

dependencies {
    // We depend on Fabric Loader here to use the Fabric @Environment annotations,
    // which get remapped to the correct annotations on each platform.
    // Do NOT use other classes from Fabric Loader.
    modImplementation("net.fabricmc:fabric-loader:${libs.versions.fabricLoader.get()}")

    modImplementation("net.fabricmc.fabric-api:fabric-api:${libs.versions.fabricApi.get()}")

    // Architectury API. This is optional, and you can comment it out if you don't need it.
    modImplementation("dev.architectury:architectury:${libs.versions.architecturyApi.get()}")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${libs.versions.kotlinCoroutines.get()}") // Kotlin Coroutines

    implementation(project(path = ":mirrg.kotlin"))

    // Library
    implementation("mirrg.kotlin:mirrg.kotlin.helium:${libs.versions.mirrgKotlinHelium.get()}")

    modRuntimeOnly("me.shedaniel:RoughlyEnoughItems-fabric:${libs.versions.rei.get()}")
    modCompileOnly("me.shedaniel:RoughlyEnoughItems-api-fabric:${libs.versions.rei.get()}")
    modCompileOnly("me.shedaniel:RoughlyEnoughItems-default-plugin-fabric:${libs.versions.rei.get()}")
    modCompileOnly("me.shedaniel.cloth:basic-math:${libs.versions.clothBasicMath.get()}")

    modImplementation("io.wispforest:owo-lib:${libs.versions.owoLibFabric.get()}")

    modApi("me.shedaniel.cloth:cloth-config:${libs.versions.clothConfig.get()}")

    modImplementation("com.github.glitchfiend:TerraBlender-fabric:${libs.versions.terraBlenderFabric.get()}")

    modCompileOnly("dev.emi:emi-fabric:${libs.versions.emi.get()}:api")

}
configurations.named("architecturyTransformerClasspath") {
    extendsFrom(configurations.named("clientCompileClasspath").get()) // transformProductionFabric でバニラのclient用クラスが見れなくて死ぬ対策
}

tasks.named<Jar>("jar") {
    duplicatesStrategy = DuplicatesStrategy.WARN // clientとmainのclassの出力先を分けた関係で IFR25KU-common.kotlin_module がclientとmainで重複するため
}
