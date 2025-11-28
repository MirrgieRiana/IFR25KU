package miragefairy2024.mod.biome

import com.mojang.serialization.Codec
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.materials.BlockMaterialCard
import miragefairy2024.mod.materials.contents.MiragidianLampBlock
import miragefairy2024.util.Registration
import miragefairy2024.util.flower
import miragefairy2024.util.generator
import miragefairy2024.util.per
import miragefairy2024.util.register
import miragefairy2024.util.registerConfiguredFeature
import miragefairy2024.util.registerPlacedFeature
import miragefairy2024.util.square
import miragefairy2024.util.surface
import miragefairy2024.util.with
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration

object MiragidianLampFeatureCard {
    val identifier = MirageFairy2024.identifier("miragidian_lamp")
    val feature = MiragidianLampFeature(NoneFeatureConfiguration.CODEC)
    val placedFeatureKey = Registries.PLACED_FEATURE with identifier

    context(ModContext)
    fun init() {
        Registration(BuiltInRegistries.FEATURE, identifier) { feature }.register()
        feature.generator(identifier) {
            registerConfiguredFeature { NoneFeatureConfiguration.INSTANCE }.generator {
                registerPlacedFeature(placedFeatureKey) { per(8) + flower(square, surface) }
            }
        }
    }
}

class MiragidianLampFeature(codec: Codec<NoneFeatureConfiguration>) : Feature<NoneFeatureConfiguration>(codec) {
    override fun place(context: FeaturePlaceContext<NoneFeatureConfiguration>): Boolean {
        val level = context.level()
        val blockPos = context.origin()
        val height = context.random().nextIntBetweenInclusive(3, 8)

        repeat(height) { i ->
            val targetBlockPos = blockPos.above(i)
            if (targetBlockPos.y >= level.maxBuildHeight) return false
            if (!level.isEmptyBlock(targetBlockPos)) return false
        }
        if (!level.getBlockState(blockPos.below()).isSolidRender(level, blockPos.below())) return false

        repeat(height) { i ->
            val targetBlockPos = blockPos.above(i)
            val part = when (i) {
                0 -> MiragidianLampBlock.Part.FOOT
                height - 1 -> MiragidianLampBlock.Part.HEAD
                else -> MiragidianLampBlock.Part.POLE
            }
            val blockState = BlockMaterialCard.MIRAGIDIAN_LAMP.block().defaultBlockState().with(MiragidianLampBlock.PART, part)
            level.setBlock(targetBlockPos, blockState, 2)
        }

        return true
    }
}
