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
import net.minecraft.world.level.levelgen.GenerationStep
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider
import net.minecraft.world.level.levelgen.placement.PlacedFeature
import net.minecraft.world.level.levelgen.placement.PlacementModifier
import java.util.function.Predicate

class FeatureContext<C : FeatureConfiguration>(val feature: Feature<C>, val card: SimpleMagicPlantCard<*>)
class ConfiguredFeatureContext<C : FeatureConfiguration>(val configuredFeatureKey: ResourceKey<ConfiguredFeature<*, *>>, val featureContext: FeatureContext<C>)

context(ModContext, SimpleMagicPlantCard<*>)
operator fun <C : FeatureConfiguration> Feature<C>.invoke(block: FeatureContext<C>. () -> Unit) {
    block(FeatureContext(this, this@SimpleMagicPlantCard))
}

context(ModContext, FeatureContext<C>)
fun <C : FeatureConfiguration> configuredFeature(suffix: String, configurationCreator: () -> C, block: ConfiguredFeatureContext<C> .() -> Unit) {
    val configuredFeatureKey = Registries.CONFIGURED_FEATURE with card.blockIdentifier * "_" * suffix
    registerDynamicGeneration(configuredFeatureKey) {
        this@FeatureContext.feature with configurationCreator()
    }
    block(ConfiguredFeatureContext(configuredFeatureKey, this@FeatureContext))
}

context(ModContext, ConfiguredFeatureContext<C>)
fun <C : FeatureConfiguration> placedFeature(suffix: String, placementModifierCreator: PlacementModifiersScope.() -> List<PlacementModifier>, biomePredicate: (BiomeSelectorScope.() -> Predicate<BiomeSelectionContext>)? = null) {
    val placedFeatureKey = Registries.PLACED_FEATURE with this@ConfiguredFeatureContext.featureContext.card.blockIdentifier * "_" * suffix
    registerDynamicGeneration(placedFeatureKey) {
        val placementModifiers = placementModifiers { placementModifierCreator() }
        Registries.CONFIGURED_FEATURE[this@ConfiguredFeatureContext.configuredFeatureKey] with placementModifiers
    }
    if (biomePredicate != null) placedFeatureKey.registerFeature(GenerationStep.Decoration.VEGETAL_DECORATION) { biomePredicate() }
}

val SimpleMagicPlantCard<*>.maxAgedBlockState get() = this.block().withAge(this.block().maxAge)
val SimpleMagicPlantCard<*>.placer: Holder<PlacedFeature> get() = PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK, SimpleBlockConfiguration(BlockStateProvider.simple(this.maxAgedBlockState)))
