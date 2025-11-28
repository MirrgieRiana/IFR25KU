package miragefairy2024.mod.magicplant.contents.magicplants

import miragefairy2024.ModContext
import miragefairy2024.util.BiomeSelectorScope
import miragefairy2024.util.PlacementModifiersScope
import miragefairy2024.util.get
import miragefairy2024.util.placementModifiers
import miragefairy2024.util.registerDynamicGeneration
import miragefairy2024.util.registerFeature
import miragefairy2024.util.times
import miragefairy2024.util.with
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext
import net.minecraft.core.Holder
import net.minecraft.core.registries.Registries
import net.minecraft.data.worldgen.placement.PlacementUtils
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.levelgen.GenerationStep
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider
import net.minecraft.world.level.levelgen.placement.PlacedFeature
import net.minecraft.world.level.levelgen.placement.PlacementModifier
import java.util.function.Predicate

class FeatureGenerationScope<C : FeatureConfiguration>(val feature: Feature<C>, val identifier: ResourceLocation)
class ConfiguredFeatureGenerationScope<C : FeatureConfiguration>(val configuredFeatureKey: ResourceKey<ConfiguredFeature<*, *>>, val identifier: ResourceLocation)

context(ModContext)
fun <C : FeatureConfiguration> Feature<C>.generator(identifier: ResourceLocation, block: FeatureGenerationScope<C>.() -> Unit) {
    block(FeatureGenerationScope(this, identifier))
}

context(ModContext, FeatureGenerationScope<C>)
fun <C : FeatureConfiguration> registerConfiguredFeature(suffix: String, configurationCreator: () -> C): ResourceKey<ConfiguredFeature<*, *>> {
    return registerDynamicGeneration(Registries.CONFIGURED_FEATURE, this@FeatureGenerationScope.identifier * "_" * suffix) {
        this@FeatureGenerationScope.feature with configurationCreator()
    }
}

context(ModContext, FeatureGenerationScope<C>)
fun <C : FeatureConfiguration> ResourceKey<ConfiguredFeature<*, *>>.generator(block: ConfiguredFeatureGenerationScope<C>.() -> Unit) {
    block(ConfiguredFeatureGenerationScope(this, this@FeatureGenerationScope.identifier))
}

context(ModContext, ConfiguredFeatureGenerationScope<C>)
fun <C : FeatureConfiguration> registerPlacedFeature(suffix: String, placementModifierCreator: PlacementModifiersScope.() -> List<PlacementModifier>): ResourceKey<PlacedFeature> {
    return registerDynamicGeneration(Registries.PLACED_FEATURE, this@ConfiguredFeatureGenerationScope.identifier * "_" * suffix) {
        val placementModifiers = placementModifiers { placementModifierCreator() }
        Registries.CONFIGURED_FEATURE[this@ConfiguredFeatureGenerationScope.configuredFeatureKey] with placementModifiers
    }
}

context(ModContext)
fun ResourceKey<PlacedFeature>.place(biomePredicate: BiomeSelectorScope.() -> Predicate<BiomeSelectionContext>) {
    this.registerFeature(GenerationStep.Decoration.VEGETAL_DECORATION) { biomePredicate() }
}

val SimpleMagicPlantCard<*>.maxAgedBlockState get() = this.block().withAge(this.block().maxAge)
val SimpleMagicPlantCard<*>.placer: Holder<PlacedFeature> get() = PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK, SimpleBlockConfiguration(BlockStateProvider.simple(this.maxAgedBlockState)))
