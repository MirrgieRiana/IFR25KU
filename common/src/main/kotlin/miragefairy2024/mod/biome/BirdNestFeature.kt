package miragefairy2024.mod.biome

import com.mojang.serialization.Codec
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.materials.BlockMaterialCard
import miragefairy2024.util.Registration
import miragefairy2024.util.flower
import miragefairy2024.util.generator
import miragefairy2024.util.overworld
import miragefairy2024.util.per
import miragefairy2024.util.placeWhenVegetalDecoration
import miragefairy2024.util.register
import miragefairy2024.util.registerConfiguredFeature
import miragefairy2024.util.registerPlacedFeature
import miragefairy2024.util.square
import miragefairy2024.util.surface
import miragefairy2024.util.with
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
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
                registerPlacedFeature(placedFeatureKey) { per(24) + flower(square, surface) }.placeWhenVegetalDecoration { overworld }
            }
        }
    }
}

class BirdNestFeature(codec: Codec<NoneFeatureConfiguration>) : Feature<NoneFeatureConfiguration>(codec) {
    override fun place(context: FeaturePlaceContext<NoneFeatureConfiguration>): Boolean {
        val level = context.level()
        val origin = context.origin()
        val random = context.random()

        // 中心の真下が草ブロックでなければ生成しないのだぁ🌱
        if (!level.getBlockState(origin.below()).`is`(Blocks.GRASS_BLOCK)) return false

        // 3x3の範囲が平坦かチェックするのだぁ🌱
        (-1..1).forEach { dx ->
            (-1..1).forEach { dz ->
                val belowPos = origin.offset(dx, -1, dz)
                if (!level.getBlockState(belowPos).isSolidRender(level, belowPos)) return false
                if (!level.getBlockState(origin.offset(dx, 0, dz)).canBeReplaced()) return false
                if (!level.getBlockState(origin.offset(dx, 1, dz)).canBeReplaced()) return false
            }
        }

        val hayState = Blocks.HAY_BLOCK.defaultBlockState()
        val eggState = BlockMaterialCard.EGG_BLOCK.block().defaultBlockState()

        // 底面（y=-1）：3x3の地面を麦俵で置換するのだぁ🌱
        (-1..1).forEach { dx ->
            (-1..1).forEach { dz ->
                level.setBlock(origin.offset(dx, -1, dz), hayState, 2)
            }
        }

        // 上面（y=0）：まず全部を麦俵にするのだぁ🌱
        (-1..1).forEach { dx ->
            (-1..1).forEach { dz ->
                level.setBlock(origin.offset(dx, 0, dz), hayState, 2)
            }
        }

        // 中央を卵ブロックにするのだぁ✨
        level.setBlock(origin, eggState, 2)

        // 外周からランダムに1～2個を卵ブロックにするのだぁ🌱
        val outerPositions = mutableListOf(
            BlockPos(-1, 0, -1), BlockPos(-1, 0, 0), BlockPos(-1, 0, 1),
            BlockPos(0, 0, -1), BlockPos(0, 0, 1),
            BlockPos(1, 0, -1), BlockPos(1, 0, 0), BlockPos(1, 0, 1),
        )
        val extraEggs = random.nextIntBetweenInclusive(1, 2)
        repeat(extraEggs) {
            if (outerPositions.isEmpty()) return@repeat
            val idx = random.nextInt(outerPositions.size)
            val offset = outerPositions.removeAt(idx)
            level.setBlock(origin.offset(offset), eggState, 2)
        }

        return true
    }
}
