package miragefairy2024.mod.biome

import com.mojang.serialization.Codec
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.materials.BlockMaterialCard
import miragefairy2024.util.Registration
import miragefairy2024.util.flower
import miragefairy2024.util.generator
import miragefairy2024.util.isIn
import miragefairy2024.util.isNotIn
import miragefairy2024.util.per
import miragefairy2024.util.placeWhenVegetalDecoration
import miragefairy2024.util.plus
import miragefairy2024.util.register
import miragefairy2024.util.registerConfiguredFeature
import miragefairy2024.util.registerPlacedFeature
import miragefairy2024.util.square
import miragefairy2024.util.surface
import miragefairy2024.util.unaryPlus
import miragefairy2024.util.with
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.tags.BlockTags
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration

object BirdNestFeatureCard {
    val identifier = MirageFairy2024.identifier("bird_nest")
    val feature = BirdNestFeature(NoneFeatureConfiguration.CODEC)
    val placedFeatureKey = Registries.PLACED_FEATURE with identifier

    context(ModContext)
    fun init() {
        Registration(BuiltInRegistries.FEATURE, identifier) { feature }.register()
        feature.generator(identifier) {
            registerConfiguredFeature { NoneFeatureConfiguration.INSTANCE }.generator {
                registerPlacedFeature(placedFeatureKey) { per(24) + flower(square, surface) }.placeWhenVegetalDecoration { +ConventionalBiomeTags.IS_PLAINS + +ConventionalBiomeTags.IS_FOREST }
            }
        }
    }
}

class BirdNestFeature(codec: Codec<NoneFeatureConfiguration>) : Feature<NoneFeatureConfiguration>(codec) {
    override fun place(context: FeaturePlaceContext<NoneFeatureConfiguration>): Boolean {
        val level = context.level()
        val originBlockPos = context.origin()
        val random = context.random()

        // 中心の真下が草ブロックでなければ生成しないのだぁ🌱
        if (level.getBlockState(originBlockPos.below()) isNotIn Blocks.GRASS_BLOCK) return false

        // この時点で生成は確定なのだぁ🌱

        val hayBlockBlockState = Blocks.HAY_BLOCK.defaultBlockState()
        val eggBlockBlockState = BlockMaterialCard.EGG_BLOCK.block().defaultBlockState()

        // 置換可能ブロック、もしくは土や草系ブロックの場合に麦俵を配置するのだぁ🌱
        fun tryPlaceHayBlock(blockPos: BlockPos) {
            val blockState = level.getBlockState(blockPos)
            if (blockState.canBeReplaced() || blockState isIn BlockTags.DIRT) {
                level.setBlock(blockPos, hayBlockBlockState, 2)
            }
        }

        // 置換可能ブロックの場合に卵ブロックを配置するのだぁ✨
        fun tryPlaceEggBlock(blockPos: BlockPos) {
            val blockState = level.getBlockState(blockPos)
            if (blockState.canBeReplaced()) {
                level.setBlock(blockPos, eggBlockBlockState, 2)
            }
        }

        // 底面（Y-1）：3x3に麦俵を配置するのだぁ🌱
        (-1..1).forEach { dx ->
            (-1..1).forEach { dz ->
                tryPlaceHayBlock(originBlockPos.offset(dx, -1, dz))
            }
        }

        // 上面（Y+0）：3x3を各座標ごとに25%の確率で卵ブロックに置換するのだぁ✨
        (-1..1).forEach { dx ->
            (-1..1).forEach { dz ->
                if (random.nextFloat() < 0.25f) {
                    tryPlaceEggBlock(originBlockPos.offset(dx, 0, dz))
                }
            }
        }

        // 壁（Y+0）：X:-2,2; Z:-1..1 の6座標に75%の確率で麦俵を設置するのだぁ🌱
        listOf(-2, 2).forEach { dx ->
            (-1..1).forEach { dz ->
                if (random.nextFloat() < 0.75f) {
                    tryPlaceHayBlock(originBlockPos.offset(dx, 0, dz))
                }
            }
        }

        // 壁（Y+0）：Z:-2,2; X:-1..1 の6座標に75%の確率で麦俵を設置するのだぁ🌱
        listOf(-2, 2).forEach { dz ->
            (-1..1).forEach { dx ->
                if (random.nextFloat() < 0.75f) {
                    tryPlaceHayBlock(originBlockPos.offset(dx, 0, dz))
                }
            }
        }

        return true
    }
}
