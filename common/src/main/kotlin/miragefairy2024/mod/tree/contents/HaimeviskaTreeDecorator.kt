package miragefairy2024.mod.tree.contents

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import miragefairy2024.MirageFairy2024
import miragefairy2024.util.with
import mirrg.kotlin.java.hydrogen.orNull
import mirrg.kotlin.java.hydrogen.toOptional
import net.minecraft.core.Direction
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.HorizontalDirectionalBlock
import net.minecraft.world.level.block.RotatedPillarBlock
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType
import java.util.Optional

object HaimeviskaTreeDecoratorCard {
    val identifier = MirageFairy2024.identifier("haimeviska")
    val type: TreeDecoratorType<HaimeviskaTreeDecorator> = TreeDecoratorType(HaimeviskaTreeDecorator.CODEC)
}

class HaimeviskaTreeDecorator(
    private val log: Block,
    private val drippingLogReplacement: Replacement?,
    private val hollowLogReplacement: Replacement?,
) : TreeDecorator() {
    companion object {
        val CODEC: MapCodec<HaimeviskaTreeDecorator> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                BuiltInRegistries.BLOCK.byNameCodec().fieldOf("log").forGetter { it.log },
                Replacement.CODEC.optionalFieldOf("dripping_log_replacement").forGetter { it.drippingLogReplacement.toOptional() },
                Replacement.CODEC.optionalFieldOf("hollow_log_replacement").forGetter { it.hollowLogReplacement.toOptional() },
            ).apply(instance, ::HaimeviskaTreeDecorator)
        }
    }

    constructor(log: Block, drippingLogReplacement: Optional<Replacement>, hollowLogReplacement: Optional<Replacement>) : this(log, drippingLogReplacement.orNull, hollowLogReplacement.orNull)

    /** 置き換え先の原木のブロックと、それが選ばれる100分率の確率なのだ～🌱 */
    class Replacement(val block: Block, val percentage: Int) {
        companion object {
            val CODEC: Codec<Replacement> = RecordCodecBuilder.create { instance ->
                instance.group(
                    BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").forGetter { it.block },
                    Codec.intRange(0, 100).fieldOf("percentage").forGetter { it.percentage },
                ).apply(instance, ::Replacement)
            }
        }
    }

    override fun type() = HaimeviskaTreeDecoratorCard.type

    override fun place(generator: Context) {
        generator.logs().forEach { blockPos ->
            if (!generator.level().isStateAtPosition(blockPos) { it == log.defaultBlockState().with(RotatedPillarBlock.AXIS, Direction.Axis.Y) }) return@forEach // 垂直の幹のみ
            val direction = Direction.from2DDataValue(generator.random().nextInt(4))
            if (!generator.isAir(blockPos.relative(direction))) return@forEach // 正面が空気の場合のみ
            val random = generator.random().nextInt(100)
            var threshold = 0
            if (drippingLogReplacement != null) {
                threshold += drippingLogReplacement.percentage
                if (random < threshold) {
                    generator.setBlock(blockPos, drippingLogReplacement.block.defaultBlockState().with(HorizontalDirectionalBlock.FACING, direction))
                    return@forEach
                }
            }
            if (hollowLogReplacement != null) {
                threshold += hollowLogReplacement.percentage
                if (random < threshold) {
                    generator.setBlock(blockPos, hollowLogReplacement.block.defaultBlockState().with(HorizontalDirectionalBlock.FACING, direction))
                    return@forEach
                }
            }
        }
    }
}
