package miragefairy2024.mod.magicplant.contents.magicplants

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import miragefairy2024.mod.materials.MaterialCard
import miragefairy2024.mod.placeditem.PlacedItemBlockEntity
import miragefairy2024.mod.placeditem.PlacedItemCard
import miragefairy2024.util.createItemStack
import net.minecraft.core.BlockPos
import net.minecraft.core.Holder
import net.minecraft.util.Mth
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration
import net.minecraft.world.level.levelgen.placement.PlacedFeature

class FairyRingFeatureConfig(val tries: Int, val minRadius: Float, val maxRadius: Float, val ySpread: Int, val feature: Holder<PlacedFeature>) : FeatureConfiguration {
    companion object {
        val CODEC: Codec<FairyRingFeatureConfig> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.INT.fieldOf("tries").forGetter(FairyRingFeatureConfig::tries),
                Codec.FLOAT.fieldOf("min_radius").forGetter(FairyRingFeatureConfig::minRadius),
                Codec.FLOAT.fieldOf("max_radius").forGetter(FairyRingFeatureConfig::maxRadius),
                Codec.INT.fieldOf("y_spread").forGetter(FairyRingFeatureConfig::ySpread),
                PlacedFeature.CODEC.fieldOf("feature").forGetter(FairyRingFeatureConfig::feature),
            ).apply(instance, ::FairyRingFeatureConfig)
        }
    }

    init {
        require(tries >= 0)
        require(minRadius >= 0F)
        require(maxRadius >= 0F)
        require(maxRadius >= minRadius)
        require(ySpread >= 0)
    }
}

class FairyRingFeature(codec: Codec<FairyRingFeatureConfig>) : Feature<FairyRingFeatureConfig>(codec) {
    override fun place(context: FeaturePlaceContext<FairyRingFeatureConfig>): Boolean {
        val config = context.config()
        val random = context.random()
        val originBlockPos = context.origin()
        val world = context.level()

        var count = 0
        val minRadius = config.minRadius
        val radiusRange = config.maxRadius - minRadius
        val y1 = config.ySpread + 1
        val mutableBlockPos = BlockPos.MutableBlockPos()
        for (l in 0 until config.tries) {
            val r = random.nextFloat() * radiusRange + minRadius
            val theta = random.nextFloat() * Mth.TWO_PI
            val x = Mth.floor(Mth.cos(theta) * r)
            val y = random.nextInt(y1) - random.nextInt(y1)
            val z = Mth.floor(Mth.sin(theta) * r)

            mutableBlockPos.setWithOffset(originBlockPos, x, y, z)
            if (config.feature.value().place(world, context.chunkGenerator(), random, mutableBlockPos)) {
                count++
            }
        }

        repeat(16) {
            val r = random.nextFloat() * config.maxRadius
            val theta = random.nextFloat() * Mth.TWO_PI
            val x = Mth.floor(Mth.cos(theta) * r)
            val z = Mth.floor(Mth.sin(theta) * r)

            mutableBlockPos.setWithOffset(originBlockPos, x, 0, z)
            val actualBlockPos = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, mutableBlockPos)
            if (!world.getBlockState(actualBlockPos).canBeReplaced()) return@repeat
            if (!world.getBlockState(actualBlockPos.below()).isSolidRender(world, actualBlockPos.below())) return@repeat

            world.setBlock(actualBlockPos, PlacedItemCard.block().defaultBlockState(), Block.UPDATE_CLIENTS)
            val blockEntity = world.getBlockEntity(actualBlockPos) as? PlacedItemBlockEntity ?: return@repeat
            blockEntity.itemStack = MaterialCard.FAIRY_SCALES.item().createItemStack()
            blockEntity.itemX = (4.0 + 8.0 * random.nextDouble()) / 16.0
            blockEntity.itemY = 0.5 / 16.0
            blockEntity.itemZ = (4.0 + 8.0 * random.nextDouble()) / 16.0
            blockEntity.itemRotateY = Mth.TWO_PI * random.nextDouble()
            blockEntity.updateShapeCache()
            blockEntity.setChanged()
        }

        return count > 0
    }
}
