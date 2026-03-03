package miragefairy2024.mod.magicplant.contents.magicplants

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import miragefairy2024.lib.placePlacedItem
import miragefairy2024.mod.materials.MaterialCard
import miragefairy2024.util.createItemStack
import net.minecraft.core.BlockPos
import net.minecraft.core.Holder
import net.minecraft.util.Mth
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration
import net.minecraft.world.level.levelgen.placement.PlacedFeature
import kotlin.math.roundToInt

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

        if (config.tries == 0) return false

        var count = 0
        val minRadius = config.minRadius
        val radiusRange = config.maxRadius - minRadius
        val y1 = config.ySpread + 1
        val mutableBlockPos = BlockPos.MutableBlockPos()

        repeat(config.tries) {
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

        repeat((4.0 * count / config.tries).roundToInt()) {
            val r = random.nextFloat() * config.maxRadius
            val theta = random.nextFloat() * Mth.TWO_PI
            val x = Mth.floor(Mth.cos(theta) * r)
            val y = random.nextInt(y1) - random.nextInt(y1)
            val z = Mth.floor(Mth.sin(theta) * r)

            mutableBlockPos.setWithOffset(originBlockPos, x, y, z)
            if (placePlacedItem(world, mutableBlockPos, MaterialCard.FAIRY_SCALES.item().createItemStack(), random)) {
                count++
            }
        }

        return count > 0
    }
}
