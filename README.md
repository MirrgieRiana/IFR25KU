# IFR25KU

This is an unofficial fork of MirageFairy2024, a Minecraft mod originally developed by The Developer of MirageFairy, Generation 7, and is now independently developed by Yoruno Kakera.

Since this mod is compatible with the original MirageFairy2024 save data, it cannot be installed simultaneously due to conflicts.

---

Links related to the original MirageFairy:

- [Original MirageFairy2024 Repository](https://github.com/MirageFairy/MirageFairy2024)

# Documentation

## Official Documents

The specifications of this mod are officially documented only in Japanese in [CHANGELOG](https://mirrgieriana.github.io/IFR25KU/CHANGELOG.html).

There is currently no comprehensive official documentation explaining the specifications of this mod, and the only available resources are unofficial wikis.

---

Other official links:

- [Japanese Official Website of MirageFairy-Kakera-Unofficial](https://miragefairy-kakera-unofficial.notion.site/)
- [IFR25KU Lang Table](https://mirrgieriana.github.io/IFR25KU/lang_table.html)
- [Modrinth (Mod Distribution Page)](https://modrinth.com/mod/ifr25ku)

## Unofficial Documents

Currently known unofficial documentation:

- [MirageFairy2024-KU非公式Wiki Wiki*](https://wikiwiki.jp/mifai2024/)

# Licensing and Attribution

## Minecraft Resources

The copyright for all texture data derived from Minecraft, including any found in `*.pdn` file layers, belongs to the original author.

## Legacy Mod Resources

The following files are part of MirageFairy2019 and are licensed under the **[CC BY-SA 3.0](https://creativecommons.org/licenses/by-sa/3.0/)** license from MirageFairy Server:

- `common/src/main/resources/assets/miragefairy2024/textures/block/nephrite_block.pdn`
- `common/src/main/resources/assets/miragefairy2024/textures/block/nephrite_block.png`
- `common/src/main/resources/assets/miragefairy2024/textures/item/rum.png`
- `common/src/main/resources/assets/miragefairy2024/textures/item/poison.png`
- `common/src/main/resources/assets/miragefairy2024/textures/item/veropedeliquora.png`
- `common/src/main/resources/assets/miragefairy2024/textures/item/cidre.png`
- `common/src/main/resources/assets/miragefairy2024/textures/item/fairy_liqueur.png`
- `common/src/main/resources/assets/miragefairy2024/textures/item/bottle3.pdn`
- `common/src/main/resources/assets/miragefairy2024/textures/item/bottle4.pdn`
- `common/src/main/resources/assets/miragefairy2024/textures/item/topaz_gem.pdn`
- `common/src/main/resources/assets/miragefairy2024/textures/item/topaz.png`

For further details, please refer to [the original repository](https://github.com/MirageFairy/MirageFairy2019).

## Codes (Programs, Data Packs)

### Copyright

Contributions for version ≤ v0.14.1: Copyright 2024 The Developer of MirageFairy, Generation 7

Contributions for version > v0.14.1: Copyright 2025 The Developer of MirageFairy, Generation 7, Yoruno Kakera

### License

These files are provided under the **[Apache License 2.0](LICENSE)**.

## Other Resources (excluding those specified above)

### Copyright

Contributions for version ≤ v0.14.1: Copyright 2024 The Developer of MirageFairy, Generation 7

Contributions for version > v0.14.1: Copyright 2025 The Developer of MirageFairy, Generation 7, Yoruno Kakera

### License

These files are provided under any of the following licenses:

- **[Apache License 2.0](LICENSE)**
- **[CC BY 3.0](https://creativecommons.org/licenses/by/3.0/)**
- **[CC BY 4.0](https://creativecommons.org/licenses/by/4.0/)**

# Developer Guide

## Using IFR25KU as a Dependency

Developers can depend on IFR25KU via the Modrinth Maven repository. Add the repository and the dependency to your Gradle build.

**Gradle Kotlin DSL (`build.gradle.kts`)**

```kotlin
repositories {
    maven { url = uri("https://api.modrinth.com/maven") }
}

dependencies {
    modImplementation("maven.modrinth:ifr25ku:<version>+<platform>")
}
```

**Gradle Groovy DSL (`build.gradle`)**

```groovy
repositories {
    maven { url = "https://api.modrinth.com/maven" }
}

dependencies {
    modImplementation "maven.modrinth:ifr25ku:<version>+<platform>"
}
```

**Notes**

- Check the [Modrinth page](https://modrinth.com/mod/ifr25ku/versions) for available versions.
- Pick the platform variant that matches your mod loader: `neoforge` or `fabric`.
