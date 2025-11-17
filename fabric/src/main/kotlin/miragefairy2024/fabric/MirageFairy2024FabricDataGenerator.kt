package miragefairy2024.fabric

import com.google.gson.JsonElement
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import miragefairy2024.DataGenerationEvents
import miragefairy2024.DataMapConsumer
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.Modules
import miragefairy2024.mixins.api.TagBuilderApi
import miragefairy2024.platformProxy
import miragefairy2024.util.TagGenerator
import miragefairy2024.util.string
import miragefairy2024.util.times
import mirrg.kotlin.gson.hydrogen.jsonArray
import mirrg.kotlin.gson.hydrogen.jsonElement
import mirrg.kotlin.gson.hydrogen.jsonObject
import mirrg.kotlin.gson.hydrogen.jsonObjectNotNull
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider
import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableProvider
import net.minecraft.advancements.AdvancementHolder
import net.minecraft.core.HolderLookup
import net.minecraft.core.Registry
import net.minecraft.core.RegistrySetBuilder
import net.minecraft.data.CachedOutput
import net.minecraft.data.DataProvider
import net.minecraft.data.PackOutput
import net.minecraft.data.advancements.AdvancementProvider
import net.minecraft.data.advancements.AdvancementSubProvider
import net.minecraft.data.models.BlockModelGenerators
import net.minecraft.data.models.ItemModelGenerators
import net.minecraft.data.recipes.RecipeOutput
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
import java.util.concurrent.CompletableFuture
import java.util.function.BiConsumer
import java.util.function.Consumer

object MirageFairy2024FabricDataGenerator : DataGeneratorEntrypoint {
    override fun onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator) {
        with(ModContext()) {
            platformProxy = FabricPlatformProxy()
            Modules.init()
            initFabricModule()
        }
        DataGenerationEvents.onInitializeDataGenerator.fire { it() }

        val pack = fabricDataGenerator.createPack()
        when (val platform = System.getProperty("miragefairy2024.datagen.platform")) {
            "common" -> common(pack)
            "neoforge" -> neoForge(pack)
            else -> throw IllegalArgumentException("Unknown platform: $platform")
        }

        TagBuilderApi.sorting = true
    }

    private fun common(pack: FabricDataGenerator.Pack) {
        pack.addProvider { output: FabricDataOutput ->
            object : FabricModelProvider(output) {
                override fun generateBlockStateModels(blockStateModelGenerator: BlockModelGenerators) = DataGenerationEvents.onGenerateBlockModel.fire { it(blockStateModelGenerator) }
                override fun generateItemModels(itemModelGenerator: ItemModelGenerators) = DataGenerationEvents.onGenerateItemModel.fire { it(itemModelGenerator) }
            }
        }
        TagGenerator.entries.forEach {
            pack.addProvider { output: FabricDataOutput, registriesFuture: CompletableFuture<HolderLookup.Provider> ->
                it.createProvider(output, registriesFuture)
            }
        }
        pack.addProvider { output: FabricDataOutput, registriesFuture: CompletableFuture<HolderLookup.Provider> ->
            val registries = registriesFuture.join()
            object : FabricBlockLootTableProvider(output, registriesFuture) {
                override fun generate() {
                    DataGenerationEvents.onGenerateBlockLootTable.fire { it(this, registries) }
                }
            }
        }
        pack.addProvider { output: FabricDataOutput, registriesFuture: CompletableFuture<HolderLookup.Provider> ->
            val registries = registriesFuture.join()
            object : SimpleFabricLootTableProvider(output, registriesFuture, LootContextParamSets.CHEST) {
                override fun generate(exporter: BiConsumer<ResourceKey<LootTable>, LootTable.Builder>) {
                    DataGenerationEvents.onGenerateChestLootTable.fire { it({ lootTableId, builder -> exporter.accept(lootTableId, builder) }, registries) }
                }
            }
        }
        pack.addProvider { output: FabricDataOutput, registriesFuture: CompletableFuture<HolderLookup.Provider> ->
            val registries = registriesFuture.join()
            object : SimpleFabricLootTableProvider(output, registriesFuture, LootContextParamSets.ARCHAEOLOGY) {
                override fun generate(exporter: BiConsumer<ResourceKey<LootTable>, LootTable.Builder>) {
                    DataGenerationEvents.onGenerateArchaeologyLootTable.fire { it({ lootTableId, builder -> exporter.accept(lootTableId, builder) }, registries) }
                }
            }
        }
        pack.addProvider { output: FabricDataOutput, registriesFuture: CompletableFuture<HolderLookup.Provider> ->
            val registries = registriesFuture.join()
            object : SimpleFabricLootTableProvider(output, registriesFuture, LootContextParamSets.ENTITY) {
                override fun generate(exporter: BiConsumer<ResourceKey<LootTable>, LootTable.Builder>) {
                    DataGenerationEvents.onGenerateEntityLootTable.fire { it({ entityType, builder -> exporter.accept(entityType.defaultLootTable, builder) }, registries) }
                }
            }
        }
        pack.addProvider { output: FabricDataOutput, registriesFuture: CompletableFuture<HolderLookup.Provider> ->
            val registries = registriesFuture.join()
            object : SimpleFabricLootTableProvider(output, registriesFuture, LootContextParamSets.ADVANCEMENT_REWARD) {
                override fun generate(exporter: BiConsumer<ResourceKey<LootTable>, LootTable.Builder>) {
                    DataGenerationEvents.onGenerateAdvancementRewardLootTable.fire { it({ lootTableId, builder -> exporter.accept(lootTableId, builder) }, registries) }
                }
            }
        }
        pack.addProvider { output: FabricDataOutput, registriesFuture: CompletableFuture<HolderLookup.Provider> ->
            object : FabricRecipeProvider(output, registriesFuture) {
                override fun buildRecipes(recipeOutput: RecipeOutput) = DataGenerationEvents.onGenerateRecipe.fire { it(recipeOutput) }
            }
        }
        pack.addProvider { output: FabricDataOutput, registriesFuture: CompletableFuture<HolderLookup.Provider> ->
            object : FabricDynamicRegistryProvider(output, registriesFuture) {
                override fun getName() = "World Gen"
                override fun configure(registries: HolderLookup.Provider, entries: Entries) {
                    DataGenerationEvents.dynamicGenerationRegistries.forEach {
                        entries.addAll(registries.lookupOrThrow(it))
                    }
                }
            }
        }
        pack.addProvider { output: FabricDataOutput, registriesFuture: CompletableFuture<HolderLookup.Provider> ->
            object : FabricLanguageProvider(output, "en_us", registriesFuture) {
                override fun generateTranslations(holderLookupProvider: HolderLookup.Provider, translationBuilder: TranslationBuilder) = DataGenerationEvents.onGenerateEnglishTranslation.fire { it(translationBuilder) }
            }
        }
        pack.addProvider { output: FabricDataOutput, registriesFuture: CompletableFuture<HolderLookup.Provider> ->
            object : FabricLanguageProvider(output, "ja_jp", registriesFuture) {
                override fun generateTranslations(holderLookupProvider: HolderLookup.Provider, translationBuilder: TranslationBuilder) = DataGenerationEvents.onGenerateJapaneseTranslation.fire { it(translationBuilder) }
            }
        }
        pack.addProvider { output: FabricDataOutput ->
            object : DataProvider {
                private val pathResolver = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "nine_patch_textures")
                override fun getName() = "Nine Patch Textures"
                override fun run(writer: CachedOutput): CompletableFuture<*> {
                    val futures = mutableListOf<CompletableFuture<*>>()
                    DataGenerationEvents.onGenerateNinePatchTexture.fire {
                        it { identifier, card ->
                            val data = jsonObject(
                                "texture" to card.texture.string.jsonElement,
                                "texture_width" to card.textureWidth.jsonElement,
                                "texture_height" to card.textureHeight.jsonElement,
                                "repeat" to card.repeat.jsonElement,
                                "patch_size" to jsonObject(
                                    "width" to card.patchWidth.jsonElement,
                                    "height" to card.patchHeight.jsonElement,
                                ),
                            )
                            futures.add(DataProvider.saveStable(writer, data, pathResolver.json(identifier)))
                        }
                    }
                    return CompletableFuture.allOf(*futures.toTypedArray())
                }
            }
        }
        pack.addProvider { output: FabricDataOutput ->
            object : DataProvider {
                private val destination = MirageFairy2024.identifier("sounds")
                override fun getName() = "Sounds"
                override fun run(writer: CachedOutput): CompletableFuture<*> {

                    val map = mutableMapOf<String, Pair<String?, List<ResourceLocation>>>()
                    DataGenerationEvents.onGenerateSound.fire {
                        it { path, subtitle, sounds ->
                            map[path] = Pair(subtitle, sounds)
                        }
                    }
                    if (map.isEmpty()) return CompletableFuture.allOf()

                    val path = output.getOutputFolder(PackOutput.Target.RESOURCE_PACK).resolve(destination.namespace).resolve(destination.path + ".json")

                    val jsonElement = map.map { (path, entry) ->
                        path to jsonObjectNotNull(
                            entry.first?.let { "subtitle" to it.jsonElement },
                            "sounds" to entry.second.map { it.string.jsonElement }.jsonArray,
                        )
                    }.jsonObject

                    return DataProvider.saveStable(writer, jsonElement, path)
                }
            }
        }
        pack.addProvider { output: FabricDataOutput ->
            object : DataProvider {
                private val pathResolver = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "particles")
                override fun getName() = "Particles"
                override fun run(writer: CachedOutput): CompletableFuture<*> {

                    val map = mutableMapOf<ResourceLocation, JsonElement>()
                    DataGenerationEvents.onGenerateParticles.fire {
                        it { identifier, jsonElement ->
                            if (identifier in map) throw Exception("Duplicate particle definition for $identifier")
                            map[identifier] = jsonElement
                        }
                    }

                    val futures = mutableListOf<CompletableFuture<*>>()
                    map.forEach { (identifier, jsonElement) ->
                        futures.add(DataProvider.saveStable(writer, jsonElement, pathResolver.json(identifier)))
                    }
                    return CompletableFuture.allOf(*futures.toTypedArray())
                }
            }
        }
        pack.addProvider { output: FabricDataOutput, registriesFuture: CompletableFuture<HolderLookup.Provider> ->
            AdvancementProvider(output, registriesFuture, listOf(object : AdvancementSubProvider {
                override fun generate(registries: HolderLookup.Provider, writer: Consumer<AdvancementHolder>) {
                    runBlocking {
                        DataGenerationEvents.onGenerateAdvancement.fire {
                            launch {
                                it(registries, writer)
                            }
                        }
                    }
                }
            }))
        }
    }

    private fun neoForge(pack: FabricDataGenerator.Pack) {
        pack.addProvider { output: FabricDataOutput, registriesFuture: CompletableFuture<HolderLookup.Provider> ->
            object : DataProvider {
                private val pathResolver = output.createPathProvider(PackOutput.Target.DATA_PACK, "data_maps")
                override fun getName() = "Data Maps"
                override fun run(writer: CachedOutput): CompletableFuture<*> {
                    val registries = registriesFuture.join()
                    val registryMap = mutableMapOf<ResourceKey<out Registry<*>>, MutableMap<ResourceLocation, MutableMap<ResourceLocation, JsonElement>>>()
                    DataGenerationEvents.onGenerateDataMap.fire {
                        it(object : DataMapConsumer {
                            override fun <T> accept(registry: ResourceKey<Registry<T>>, type: ResourceLocation, target: ResourceKey<T>, data: JsonElement) {
                                val typeMap = registryMap.getOrPut(registry) { mutableMapOf() }
                                val targetMap = typeMap.getOrPut(type) { mutableMapOf() }
                                if (target.location() in targetMap) throw IllegalArgumentException("Duplicate data map entry for ${target.location()} in registry ${registry.location()}")
                                targetMap[target.location()] = data
                            }
                        }, registries)
                    }
                    val futures = mutableListOf<CompletableFuture<*>>()
                    registryMap.forEach { (registry, typeMap) ->
                        typeMap.forEach { (type, targetMap) ->
                            val jsonElement = jsonObject(
                                "values" to targetMap.mapKeys { it.key.string }.jsonObject,
                            )
                            val registryPath = (if (registry.location().namespace == "minecraft") "" else "${registry.location().namespace}/") + registry.location().path
                            futures += DataProvider.saveStable(writer, jsonElement, pathResolver.json("$registryPath/" * type))
                        }
                    }
                    return CompletableFuture.allOf(*futures.toTypedArray())
                }
            }
        }
    }

    override fun buildRegistry(registryBuilder: RegistrySetBuilder) {
        DataGenerationEvents.onBuildRegistry.fire { it(registryBuilder) }
    }
}
