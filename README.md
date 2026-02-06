# IFR25KU

This project is an unofficial fork of [MF24KU](https://github.com/MirrgieRiana/MirageFairy2024-Kakera-Unofficial).

MF24KU is an unofficial fork of [MirageFairy2024](https://github.com/MirageFairy/MirageFairy2024).

MirageFairy2024 is a Minecraft mod originally developed by The Developer of MirageFairy, Generation 7.

IFR25KU is now independently developed by Yoruno Kakera.

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

- [Japanese Official Website of Kakera-Unofficial](https://kakera-unofficial.notion.site/)
- [IFR25KU Lang Table](https://mirrgieriana.github.io/IFR25KU/lang_table.html)
- [IFR25KU Lang Table (JSON)](https://mirrgieriana.github.io/IFR25KU/lang_table.json)
- [IFR25KU Lang Table (CSV)](https://mirrgieriana.github.io/IFR25KU/lang_table.csv)
- [Modrinth (Mod Distribution Page)](https://modrinth.com/mod/ifr25ku)

## Unofficial Documents

Currently known unofficial documentation:

- [MirageFairy2024-KU非公式Wiki Wiki*](https://wikiwiki.jp/mifai2024/)

## Troubleshooting

### World generation error: `Feature order cycle found, involved sources`

In some mod configurations, world generation may fail with the following error:

```text
java.lang.IllegalStateException: Feature order cycle found, involved sources
```

This can be resolved by installing [Feature Recycler](https://modrinth.com/mod/feature-recycler).

# Licensing and Attribution

## Minecraft Resources

The copyright for all texture data derived from Minecraft, including any found in `*.pdn` file layers, belongs to the original author.

## Legacy Mod Resources

The following files are part of MirageFairy2019 and are licensed under the **[CC BY-SA 3.0](https://creativecommons.org/licenses/by-sa/3.0/)** license from MirageFairy Server:

- `common/src/main/resources/assets/miragefairy2024/textures/block/apatite_block.pdn`
- `common/src/main/resources/assets/miragefairy2024/textures/block/magnetite_block.png`
- `common/src/main/resources/assets/miragefairy2024/textures/block/fluorite_block.png`
- `common/src/main/resources/assets/miragefairy2024/textures/block/nephrite_block.pdn`
- `common/src/main/resources/assets/miragefairy2024/textures/block/nephrite_block.png`
- `common/src/main/resources/assets/miragefairy2024/textures/block/nephrite_ore.pdn`
- `common/src/main/resources/assets/miragefairy2024/textures/block/nephrite_ore.png`
- `common/src/main/resources/assets/miragefairy2024/textures/block/sulfur_block.png`
- `common/src/main/resources/assets/miragefairy2024/textures/block/sulfur_ore.png`
- `common/src/main/resources/assets/miragefairy2024/textures/item/rum.png`
- `common/src/main/resources/assets/miragefairy2024/textures/item/poison.png`
- `common/src/main/resources/assets/miragefairy2024/textures/item/veropedeliquora.png`
- `common/src/main/resources/assets/miragefairy2024/textures/item/cidre.png`
- `common/src/main/resources/assets/miragefairy2024/textures/item/fairy_liqueur.png`
- `common/src/main/resources/assets/miragefairy2024/textures/item/bottle3.pdn`
- `common/src/main/resources/assets/miragefairy2024/textures/item/bottle4.pdn`
- `common/src/main/resources/assets/miragefairy2024/textures/item/topaz_gem.pdn`
- `common/src/main/resources/assets/miragefairy2024/textures/item/topaz.png`
- `common/src/main/resources/assets/miragefairy2024/textures/item/nephrite.pdn`
- `common/src/main/resources/assets/miragefairy2024/textures/item/nephrite.png`
- `common/src/main/resources/assets/miragefairy2024/textures/item/sulfur.png`
- `common/src/main/resources/assets/miragefairy2024/textures/item/fairy_pot.pdn`
- `common/src/main/resources/assets/miragefairy2024/textures/item/fairy_pot.png`
- `common/src/main/resources/assets/miragefairy2024/textures/item/solid_fuel.png`
- `common/src/main/resources/assets/miragefairy2024/textures/item/tiny_solid_fuel.png`

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

These files are provided under a multi-license scheme.

You may choose any one of the following licenses, at your option, and then only the terms of the license you selected apply to your use.

- **[Apache License 2.0](LICENSE)**
- **[CC BY 3.0](https://creativecommons.org/licenses/by/3.0/)**
- **[CC BY 4.0](https://creativecommons.org/licenses/by/4.0/)**

# Developer Guide

This project provides Maven repositories to use the mod JAR and sources.

Developers may use either Modrinth Maven or Curse Maven.

Due to technical limitations, dependencies of required mods are not resolved transitively by either repository.

## Using IFR25KU via Modrinth Maven

Adding IFR25KU as a Maven dependency is simpler with Modrinth Maven.

See the official article:

https://support.modrinth.com/en/articles/8801191-modrinth-maven

**Gradle Kotlin DSL example:**

```kotlin
repositories {
    maven("https://api.modrinth.com/maven")
}

dependencies {
    modImplementation("maven.modrinth:ifr25ku:<version>+<platform>")
}
```

**Notes:**

- Adjust the sample to fit your build setup.
- Choose `<version>` from the [Modrinth versions](https://modrinth.com/mod/ifr25ku/versions).
- Choose `<platform>` to match your mod loader: `neoforge` or `fabric`.

## Using IFR25KU via Curse Maven

Defining the dependency with Curse Maven is very unpredictable; always consult the official documentation.

https://www.cursemaven.com/

**Gradle Kotlin DSL example:**

```kotlin
repositories {
    maven("https://cursemaven.com")
}

dependencies {
    // With attached sources
    modImplementation("curse.maven:ifr25ku-1346991:<fileId>-sources-<sourceFileId>")

    // Main artifact only
    modImplementation("curse.maven:ifr25ku-1346991:<fileId>")
}
```

**Notes:**

- Adjust the sample to fit your build setup.
- Get `<fileId>` from the [CurseForge files list](https://www.curseforge.com/minecraft/mc-mods/ifr25ku/files).
- Get `<sourceFileId>` from the “Additional Files” section on the main file’s page.
- If you do not need attached sources, omit the `-sources-<sourceFileId>` suffix.
