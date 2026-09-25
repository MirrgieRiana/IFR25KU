package miragefairy2024.mod.biome

import com.mojang.serialization.Codec
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.materials.BlockMaterialCard
import miragefairy2024.mod.tree.contents.plastictree.onResinCementedDirt
import miragefairy2024.util.Registration
import miragefairy2024.util.center
import miragefairy2024.util.flower
import miragefairy2024.util.generator
import miragefairy2024.util.per
import miragefairy2024.util.register
import miragefairy2024.util.registerConfiguredFeature
import miragefairy2024.util.registerPlacedFeature
import miragefairy2024.util.surface
import miragefairy2024.util.with
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration

object ResinCementedDirtSapFeatureCard {
    val identifier = MirageFairy2024.identifier("resin_cemented_dirt_sap")
    val feature = ResinCementedDirtSapFeature(NoneFeatureConfiguration.CODEC)
    val placedFeatureKey = Registries.PLACED_FEATURE with identifier

    context(ModContext)
    fun init() {
        Registration(BuiltInRegistries.FEATURE, identifier) { feature }.register()
        feature.generator(identifier) {
            registerConfiguredFeature { NoneFeatureConfiguration.INSTANCE }.generator {
                registerPlacedFeature(placedFeatureKey) { per(4) + flower(center, surface) + onResinCementedDirt }
            }
        }
    }
}

class ResinCementedDirtSapFeature(codec: Codec<NoneFeatureConfiguration>) : Feature<NoneFeatureConfiguration>(codec) {
    override fun place(context: FeaturePlaceContext<NoneFeatureConfiguration>): Boolean {
        val level = context.level()
        val originBlockPos = context.origin()
        val random = context.random()

        // 空気や草や流体のように既存のブロックを押しのけずに済む位置にのみ、樹液を置けるのだ～🌱
        fun canPlaceSap(blockPos: BlockPos): Boolean {
            return level.getBlockState(blockPos).canBeReplaced()
        }

        // 樹液が宙に浮かないように、直下が完全な立方体であることを確かめるのだ～🌱
        fun isSupported(blockPos: BlockPos): Boolean {
            val belowBlockPos = blockPos.below()
            return level.getBlockState(belowBlockPos).isSolidRender(level, belowBlockPos)
        }

        val blockState = BlockMaterialCard.PLASTIC_TREE_SAP_BLOCK.block().defaultBlockState()

        fun placePillar(blockPos: BlockPos, height: Int): Boolean {
            if (!isSupported(blockPos)) return false
            val blockPosList = (0 until height).map { blockPos.above(it) }
            if (!blockPosList.all { canPlaceSap(it) }) return false
            blockPosList.forEach { level.setBlock(it, blockState, 2) }
            return true
        }

        val originHeight = random.nextIntBetweenInclusive(2, 4)
        if (!placePillar(originBlockPos, originHeight)) return false

        // 辺で接する4マス
        listOf(Pair(-1, 0), Pair(1, 0), Pair(0, -1), Pair(0, 1)).forEach { (dx, dz) ->
            if (random.nextFloat() < 0.5F) {
                placePillar(originBlockPos.offset(dx, 0, dz), random.nextIntBetweenInclusive(1, originHeight))
            }
        }

        // 角で接する4マス
        // 乱数2個の最小値を採ることで、辺で接するマスよりも低い柱が出やすくなって、裾が外側へ向かって下がるのだ～🌱
        listOf(Pair(-1, -1), Pair(-1, 1), Pair(1, -1), Pair(1, 1)).forEach { (dx, dz) ->
            if (random.nextFloat() < 0.25F) {
                val height = minOf(random.nextIntBetweenInclusive(1, originHeight), random.nextIntBetweenInclusive(1, originHeight))
                placePillar(originBlockPos.offset(dx, 0, dz), height)
            }
        }

        return true
    }
}
