package miragefairy2024.mod.tree.contents

import com.mojang.serialization.MapCodec
import miragefairy2024.MirageFairy2024
import net.minecraft.util.RandomSource
import net.minecraft.util.valueproviders.ConstantInt
import net.minecraft.world.level.LevelSimulatedReader
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType

object GiantHaimeviskaFoliagePlacerCard {
    val identifier = MirageFairy2024.identifier("giant_haimeviska")
    private val codec: MapCodec<GiantTreeFoliagePlacer> = MapCodec.unit { GiantHaimeviskaFoliagePlacer }
    val type: FoliagePlacerType<GiantTreeFoliagePlacer> = FoliagePlacerType(codec)
}

object GiantPlasticTreeFoliagePlacerCard {
    val identifier = MirageFairy2024.identifier("giant_plastic_tree")
    private val codec: MapCodec<GiantTreeFoliagePlacer> = MapCodec.unit { GiantPlasticTreeFoliagePlacer }
    val type: FoliagePlacerType<GiantTreeFoliagePlacer> = FoliagePlacerType(codec)
}

object GiantHaimeviskaFoliagePlacer : GiantTreeFoliagePlacer(0.0F) {
    override fun type() = GiantHaimeviskaFoliagePlacerCard.type
}

// プラノキは葉が少なくて、空が結構開けているのだ～🌱
object GiantPlasticTreeFoliagePlacer : GiantTreeFoliagePlacer(0.4F) {
    override fun type() = GiantPlasticTreeFoliagePlacerCard.type
}

/**
 * 枝先に葉の塊を作る葉の配置子なのだ～🌱
 *
 * [thinningRate] の割合で葉を間引くのだ～🌱 樹冠の透け具合が樹種によって変わるのだ～🌱
 */
abstract class GiantTreeFoliagePlacer(private val thinningRate: Float) : FoliagePlacer(ConstantInt.of(3), ConstantInt.of(0)) {
    private companion object {
        const val FOLIAGE_LAYER_COUNT = 2
    }

    override fun createFoliage(
        level: LevelSimulatedReader,
        blockSetter: FoliageSetter,
        random: RandomSource,
        config: TreeConfiguration,
        maxFreeTreeHeight: Int,
        attachment: FoliageAttachment,
        foliageHeight: Int,
        foliageRadius: Int,
        offset: Int,
    ) {
        (0..<FOLIAGE_LAYER_COUNT).forEach { localY ->
            // 上の段ほど半径が小さい円盤を積むのだ～🌱
            val range = foliageRadius + attachment.radiusOffset() - localY
            if (range > 0) {
                // 幹に対する方角の情報が一切渡らないので、回転対称な形しか作れないのだぁ…🌧️
                placeLeavesRow(
                    level,
                    blockSetter,
                    random,
                    config,
                    attachment.pos(),
                    range,
                    offset + localY,
                    attachment.doubleTrunk(),
                )
            }
        }
    }

    override fun foliageHeight(random: RandomSource, height: Int, config: TreeConfiguration) = FOLIAGE_LAYER_COUNT - 1

    override fun shouldSkipLocation(random: RandomSource, localX: Int, localY: Int, localZ: Int, range: Int, large: Boolean): Boolean {
        if (localX * localX + localZ * localZ > range * range) return true
        return random.nextFloat() < thinningRate
    }
}
