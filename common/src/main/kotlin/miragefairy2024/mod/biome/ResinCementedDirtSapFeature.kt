package miragefairy2024.mod.biome

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.tree.contents.plastictree.onResinCementedDirt
import miragefairy2024.util.center
import miragefairy2024.util.flower
import miragefairy2024.util.generator
import miragefairy2024.util.per
import miragefairy2024.util.registerConfiguredFeature
import miragefairy2024.util.registerPlacedFeature
import miragefairy2024.util.surface
import miragefairy2024.util.with
import net.minecraft.core.registries.Registries
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider

object ResinCementedDirtSapFeatureCard {
    val identifier = MirageFairy2024.identifier("resin_cemented_dirt_sap")
    val placedFeatureKey = Registries.PLACED_FEATURE with identifier

    context(ModContext)
    fun init() {
        Feature.SIMPLE_BLOCK.generator(identifier) {
            registerConfiguredFeature {
                // TODO 樹脂土から滲み出た樹液のブロック
                SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.BEE_NEST))
            }.generator {
                registerPlacedFeature(placedFeatureKey) { per(4) + flower(center, surface) + onResinCementedDirt }
            }
        }
    }
}
