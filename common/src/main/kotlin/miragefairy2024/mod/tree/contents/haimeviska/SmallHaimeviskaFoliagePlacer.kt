package miragefairy2024.mod.tree.contents.haimeviska

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import miragefairy2024.MirageFairy2024
import net.minecraft.util.RandomSource
import net.minecraft.util.valueproviders.IntProvider
import net.minecraft.world.level.LevelSimulatedReader
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType

object SmallHaimeviskaFoliagePlacerCard {
    val identifier = MirageFairy2024.identifier("small_haimeviska")
    private val codec: MapCodec<SmallHaimeviskaFoliagePlacer> = RecordCodecBuilder.mapCodec { instance ->
        instance.group(
            IntProvider.codec(0, 16).fieldOf("radius").forGetter { it.radiusProvider },
            IntProvider.codec(0, 16).fieldOf("offset").forGetter { it.offsetProvider },
            Codec.intRange(0, 16).fieldOf("height").forGetter { it.height },
        ).apply(instance, ::SmallHaimeviskaFoliagePlacer)
    }
    val type: FoliagePlacerType<SmallHaimeviskaFoliagePlacer> = FoliagePlacerType(codec)
}

class SmallHaimeviskaFoliagePlacer(radius: IntProvider, offset: IntProvider, val height: Int) : FoliagePlacer(radius, offset) {
    val radiusProvider: IntProvider get() = this.radius
    val offsetProvider: IntProvider get() = this.offset

    override fun type() = SmallHaimeviskaFoliagePlacerCard.type

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
        (0..foliageHeight).forEach { localY ->
            placeLeavesRow(
                level,
                blockSetter,
                random,
                config,
                attachment.pos(),
                foliageRadius + attachment.radiusOffset(),
                offset + localY,
                attachment.doubleTrunk(),
            )
        }
    }

    override fun foliageHeight(random: RandomSource, height: Int, config: TreeConfiguration) = this.height

    override fun shouldSkipLocation(random: RandomSource, localX: Int, localY: Int, localZ: Int, range: Int, large: Boolean) = false
}
