package miragefairy2024.mod.mantle

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.EnJa
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.get
import miragefairy2024.util.registerDimensionGeneration
import miragefairy2024.util.registerDynamicGeneration
import miragefairy2024.util.with
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.BlockTags
import net.minecraft.util.valueproviders.UniformInt
import net.minecraft.world.level.Level
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.biome.BiomeSpecialEffects
import net.minecraft.world.level.biome.FixedBiomeSource
import net.minecraft.world.level.biome.MobSpawnSettings
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.dimension.BuiltinDimensionTypes
import net.minecraft.world.level.dimension.DimensionType
import net.minecraft.world.level.dimension.LevelStem
import net.minecraft.world.level.levelgen.DensityFunctions
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings
import net.minecraft.world.level.levelgen.NoiseRouter
import net.minecraft.world.level.levelgen.NoiseSettings
import net.minecraft.world.level.levelgen.SurfaceRules
import net.minecraft.world.level.levelgen.VerticalAnchor
import net.minecraft.world.level.levelgen.synth.NormalNoise
import java.util.OptionalLong

private val identifier = MirageFairy2024.identifier("mantle")

/** マントルディメンションの [Level] としての鍵なのだ～🌱 */
val MANTLE_DIMENSION_KEY: ResourceKey<Level> = Registries.DIMENSION with identifier

val mantleDimensionStemKey: ResourceKey<LevelStem> = Registries.LEVEL_STEM with identifier
val mantleDimensionTypeKey: ResourceKey<DimensionType> = Registries.DIMENSION_TYPE with identifier
val mantleNoiseGeneratorSettingsKey: ResourceKey<NoiseGeneratorSettings> = Registries.NOISE_SETTINGS with identifier
val mantleBiomeKey: ResourceKey<Biome> = Registries.BIOME with identifier

/** マントルディメンションの、岩盤の下限なのだ～🌱 */
const val MANTLE_DIMENSION_MIN_Y = -64

/** マントルディメンションの、岩盤の上限なのだ～🌱 */
const val MANTLE_DIMENSION_CEILING_Y = 256

/** マントルディメンションへ地上世界からやってきたときに、到達する高度なのだ～🌱 */
const val MANTLE_DIMENSION_ARRIVAL_Y = 64

/** 地上世界の距離に対する、マントルディメンションの距離の係数なのだ～🌱 */
const val MANTLE_DIMENSION_COORDINATE_SCALE = 4.0

/** ノイズの高さの単位に合わせるため、天井の 1 つ上まで含めた高さを、16 の倍数へ切り上げるのだ～🌱 */
private const val MANTLE_DIMENSION_HEIGHT = 336

private val mantleBiomeTranslation = Translation({ identifier.toLanguageKey("biome") }, EnJa("Mantle", "マントル"))

context(ModContext)
fun initMantleDimension() {

    mantleBiomeTranslation.enJa()

    registerDynamicGeneration(mantleBiomeKey) {
        Biome.BiomeBuilder()
            .hasPrecipitation(false)
            .temperature(2.0F)
            .downfall(0.0F)
            .specialEffects(
                BiomeSpecialEffects.Builder()
                    .waterColor(0x3F0F0F)
                    .waterFogColor(0x1F0707)
                    .fogColor(0x1A0A08)
                    .skyColor(0x1A0A08)
                    .build()
            )
            .mobSpawnSettings(MobSpawnSettings.Builder().build())
            .generationSettings(net.minecraft.world.level.biome.BiomeGenerationSettings.Builder(lookup(Registries.PLACED_FEATURE), lookup(Registries.CONFIGURED_CARVER)).build())
            .build()
    }

    registerDynamicGeneration(mantleDimensionTypeKey) {
        DimensionType(
            OptionalLong.of(18000L),
            false,
            true,
            true,
            false,
            MANTLE_DIMENSION_COORDINATE_SCALE,
            false,
            false,
            MANTLE_DIMENSION_MIN_Y,
            MANTLE_DIMENSION_HEIGHT,
            MANTLE_DIMENSION_HEIGHT,
            BlockTags.INFINIBURN_OVERWORLD,
            BuiltinDimensionTypes.NETHER_EFFECTS,
            0.05F,
            DimensionType.MonsterSettings(false, false, UniformInt.of(0, 7), 15),
        )
    }

    registerDynamicGeneration(mantleNoiseGeneratorSettingsKey) {
        NoiseGeneratorSettings(
            NoiseSettings.create(MANTLE_DIMENSION_MIN_Y, MANTLE_DIMENSION_HEIGHT, 1, 2),
            MantleBlockCard.BRIDGMANITE.block().defaultBlockState(),
            Blocks.LAVA.defaultBlockState(),
            createMantleNoiseRouter(),
            createMantleSurfaceRule(),
            listOf(),
            MANTLE_DIMENSION_MIN_Y,
            false,
            false,
            false,
            false,
        )
    }

    registerDimensionGeneration(mantleDimensionStemKey) {
        LevelStem(
            Registries.DIMENSION_TYPE[mantleDimensionTypeKey],
            NoiseBasedChunkGenerator(
                FixedBiomeSource(Registries.BIOME[mantleBiomeKey]),
                Registries.NOISE_SETTINGS[mantleNoiseGeneratorSettingsKey],
            ),
        )
    }

}

/**
 * 天井の高度までを岩石で満たし、その上を空にする密度関数を持った、ノイズルーターなのだ～🌱
 *
 * 洞窟も帯水層も鉱脈も作らないから、密度以外の要素は、すべて 0 なのだ～🌱
 */
private fun createMantleNoiseRouter(): NoiseRouter {
    val zero = DensityFunctions.zero()
    val density = DensityFunctions.yClampedGradient(MANTLE_DIMENSION_CEILING_Y, MANTLE_DIMENSION_CEILING_Y + 1, 1.0, -1.0)
    return NoiseRouter(zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, density, zero, zero, zero)
}

/**
 * 上下を岩盤で塞ぎ、その間をノイズによって選ばれた岩石で満たす、サーフェスルールなのだ～🌱
 *
 * サーフェスルールは地表付近だけでなく、既定のブロックで埋まっている全ての高度に適用されるから、
 * 岩石の切り替えにも使えるのだ～🌱
 */
private fun createMantleSurfaceRule(): SurfaceRules.RuleSource {
    val bedrock = SurfaceRules.state(Blocks.BEDROCK.defaultBlockState())

    fun rock(card: MantleBlockCard) = SurfaceRules.state(card.block().defaultBlockState())

    /** ノイズの値の範囲を等分して、岩石を選び分けるのだ～🌱 */
    fun byNoise(noise: ResourceKey<NormalNoise.NoiseParameters>, vararg cards: MantleBlockCard): SurfaceRules.RuleSource {
        val step = 2.0 / cards.size
        val rules = cards.dropLast(1).mapIndexed { index, card ->
            SurfaceRules.ifTrue(SurfaceRules.noiseCondition(noise, -1.0 + step * index, -1.0 + step * (index + 1)), rock(card))
        }
        return SurfaceRules.sequence(*rules.toTypedArray(), rock(cards.last()))
    }

    return SurfaceRules.sequence(
        SurfaceRules.ifTrue(SurfaceRules.verticalGradient("mantle_bedrock_floor", VerticalAnchor.bottom(), VerticalAnchor.aboveBottom(5)), bedrock),
        SurfaceRules.ifTrue(SurfaceRules.yBlockCheck(VerticalAnchor.absolute(MANTLE_DIMENSION_CEILING_Y), 0), bedrock),
        byNoise(
            net.minecraft.world.level.levelgen.Noises.NETHER_STATE_SELECTOR,
            MantleBlockCard.BRIDGMANITE,
            MantleBlockCard.FERROPERICLASE,
            MantleBlockCard.MAJORITE,
            MantleBlockCard.AKIMOTOITE,
            MantleBlockCard.POST_PEROVSKITE,
        ),
    )
}
