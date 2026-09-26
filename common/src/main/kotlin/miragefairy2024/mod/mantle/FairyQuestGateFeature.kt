package miragefairy2024.mod.mantle

import com.mojang.serialization.Codec
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.Registration
import miragefairy2024.util.generator
import miragefairy2024.util.overworld
import miragefairy2024.util.per
import miragefairy2024.util.plus
import miragefairy2024.util.register
import miragefairy2024.util.registerConfiguredFeature
import miragefairy2024.util.registerFeature
import miragefairy2024.util.registerPlacedFeature
import miragefairy2024.util.uniformOre
import miragefairy2024.util.with
import net.minecraft.core.Direction
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.world.level.levelgen.GenerationStep
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration

/** フェアリークエストゲートが生成される高度の下限なのだ～🌱 地上世界の最下層なのだ～🌱 */
private const val FAIRY_QUEST_GATE_MIN_Y = -64

/** フェアリークエストゲートが生成される高度の上限なのだ～🌱 */
private const val FAIRY_QUEST_GATE_MAX_Y = -32

/** ダンジョンのスポナーと同じくらい稀に出現させるための、生成の試行の間隔なのだ～🌱 */
private const val FAIRY_QUEST_GATE_RARITY = 24

object FairyQuestGateFeatureCard {
    val identifier = MirageFairy2024.identifier("fairy_quest_gate")
    val feature = FairyQuestGateFeature(NoneFeatureConfiguration.CODEC)
    val placedFeatureKey = Registries.PLACED_FEATURE with identifier

    context(ModContext)
    fun init() {
        Registration(BuiltInRegistries.FEATURE, identifier) { feature }.register()
        feature.generator(identifier) {
            registerConfiguredFeature { NoneFeatureConfiguration.INSTANCE }.generator {
                registerPlacedFeature(placedFeatureKey) { per(FAIRY_QUEST_GATE_RARITY) + uniformOre(FAIRY_QUEST_GATE_MIN_Y, FAIRY_QUEST_GATE_MAX_Y) }
                    .registerFeature(GenerationStep.Decoration.UNDERGROUND_STRUCTURES) { overworld }
            }
        }
    }
}

/** 地中に埋もれたフェアリークエストゲートを生成するのだ～🌱 */
class FairyQuestGateFeature(codec: Codec<NoneFeatureConfiguration>) : Feature<NoneFeatureConfiguration>(codec) {
    override fun place(context: FeaturePlaceContext<NoneFeatureConfiguration>): Boolean {
        val level = context.level()
        val originBlockPos = context.origin()
        val random = context.random()

        // ゲートの周りに 1 ブロックの余白を持たせた範囲が、埋まっていなければならないのだ～🌱
        val axis = if (random.nextBoolean()) Direction.Axis.X else Direction.Axis.Z
        val widthDirection = if (axis == Direction.Axis.X) Direction.EAST else Direction.SOUTH

        if (originBlockPos.y - 1 < level.minBuildHeight) return false
        if (originBlockPos.y + FAIRY_QUEST_GATE_HEIGHT + 1 > level.maxBuildHeight - 1) return false

        val isBuried = (-2..FAIRY_QUEST_GATE_WIDTH + 1).all { dw ->
            (-2..FAIRY_QUEST_GATE_HEIGHT + 1).all { dy ->
                val blockPos = originBlockPos.relative(widthDirection, dw).above(dy)
                !level.isEmptyBlock(blockPos) && !level.getBlockState(blockPos).liquid()
            }
        }
        if (!isBuried) return false

        placeFairyQuestGate(level, originBlockPos, axis)
        return true
    }
}
