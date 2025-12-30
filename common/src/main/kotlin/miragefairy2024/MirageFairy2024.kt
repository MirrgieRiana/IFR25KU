package miragefairy2024

import com.google.gson.JsonElement
import miragefairy2024.mod.NinePatchTextureCard
import miragefairy2024.util.ResourceLocation
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider
import net.minecraft.advancements.AdvancementHolder
import net.minecraft.core.HolderLookup
import net.minecraft.core.Registry
import net.minecraft.core.RegistrySetBuilder
import net.minecraft.data.models.BlockModelGenerators
import net.minecraft.data.models.ItemModelGenerators
import net.minecraft.data.recipes.RecipeOutput
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.storage.loot.LootTable
import java.util.function.Consumer

object ModEvents {
    val onConstruction = InitializationEventRegistry<() -> Unit>()
    val onInitialize = InitializationEventRegistry<() -> Unit>()
    val onClientInit = InitializationEventRegistry<() -> Unit>()
    val onTerraBlenderInitialized = InitializationEventRegistry<() -> Unit>()
}

object DataGenerationEvents {
    val onInitializeDataGenerator = InitializationEventRegistry<() -> Unit>()

    val onGenerateBlockModel = InitializationEventRegistry<(BlockModelGenerators) -> Unit>()
    val onGenerateItemModel = InitializationEventRegistry<(ItemModelGenerators) -> Unit>()
    val onGenerateBlockLootTable = InitializationEventRegistry<(FabricBlockLootTableProvider, HolderLookup.Provider) -> Unit>()
    val onGenerateChestLootTable = InitializationEventRegistry<((ResourceKey<LootTable>, LootTable.Builder) -> Unit, HolderLookup.Provider) -> Unit>()
    val onGenerateArchaeologyLootTable = InitializationEventRegistry<((ResourceKey<LootTable>, LootTable.Builder) -> Unit, HolderLookup.Provider) -> Unit>()
    val onGenerateEntityLootTable = InitializationEventRegistry<((EntityType<*>, LootTable.Builder) -> Unit, HolderLookup.Provider) -> Unit>()
    val onGenerateAdvancementRewardLootTable = InitializationEventRegistry<((ResourceKey<LootTable>, LootTable.Builder) -> Unit, HolderLookup.Provider) -> Unit>()
    val onGenerateRecipe = InitializationEventRegistry<(RecipeOutput) -> Unit>()
    val onGenerateEnglishTranslation = InitializationEventRegistry<(FabricLanguageProvider.TranslationBuilder) -> Unit>()
    val onGenerateJapaneseTranslation = InitializationEventRegistry<(FabricLanguageProvider.TranslationBuilder) -> Unit>()
    val onGenerateNinePatchTexture = InitializationEventRegistry<((ResourceLocation, NinePatchTextureCard) -> Unit) -> Unit>()
    val onGenerateSound = InitializationEventRegistry<((path: String, subtitle: String?, sounds: List<ResourceLocation>) -> Unit) -> Unit>()
    val onGenerateParticles = InitializationEventRegistry<((identifier: ResourceLocation, jsonElement: JsonElement) -> Unit) -> Unit>()
    val onGenerateAdvancement = InitializationEventRegistry<suspend (HolderLookup.Provider, Consumer<AdvancementHolder>) -> Unit>()
    val onGenerateDataMap = InitializationEventRegistry<(DataMapConsumer, HolderLookup.Provider) -> Unit>()

    val onBuildRegistry = InitializationEventRegistry<(RegistrySetBuilder) -> Unit>()

    val dynamicGenerationRegistries = mutableSetOf<ResourceKey<out Registry<*>>>()
}

interface DataMapConsumer {
    fun <T> accept(registry: ResourceKey<Registry<T>>, type: ResourceLocation, target: ResourceKey<T>, data: JsonElement)
}

object MirageFairy2024 {
    const val MOD_ID = "miragefairy2024"
    fun identifier(path: String) = ResourceLocation(MOD_ID, path)
}
