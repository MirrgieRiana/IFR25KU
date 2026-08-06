package miragefairy2024.util

import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.levelgen.Noises
import net.minecraft.world.level.levelgen.synth.NormalNoise

/**
 * 地表ルールの閾値は、ノイズの値をこの数で割ったものと比べられるのだ～🌱
 * バニラの[SurfaceRuleData.surfaceNoiseAbove]と同じ数なのだ✨
 */
const val SURFACE_NOISE_THRESHOLD_DIVISOR = 8.25

/**
 * 地表ルールで使うノイズの、値の標準偏差の実測値なのだ～🌱
 *
 * ノイズの平均は理論上厳密に0だから、標準偏差の導出に使う平均にも0を使っているのだ✨
 */
val SURFACE_NOISE_STANDARD_DEVIATIONS = mapOf(
    Noises.SURFACE to 0.3121,
    Noises.SURFACE_SECONDARY to 0.3050,
)

private fun getStandardDeviation(noiseKey: ResourceKey<NormalNoise.NoiseParameters>): Double {
    return SURFACE_NOISE_STANDARD_DEVIATIONS[noiseKey] ?: throw IllegalArgumentException("Standard deviation is not measured: ${noiseKey.location()}")
}

/**
 * 地表ルールの閾値から、ノイズがその閾値以上になる割合を求めるのだ～🌱
 *
 * 引数の[threshold]は、[SURFACE_NOISE_THRESHOLD_DIVISOR]で割る前の値なのだ✨
 * ノイズの分布を正規分布で近似しているから、閾値が0から遠いところでは1ポイントほど外れることがあるのだ🌧️
 */
fun getSurfaceNoiseMatchRatio(noiseKey: ResourceKey<NormalNoise.NoiseParameters>, threshold: Double): Double {
    return getNormalDistributionUpperProbability(threshold / SURFACE_NOISE_THRESHOLD_DIVISOR, getStandardDeviation(noiseKey))
}

/**
 * ノイズが閾値以上になる割合から、地表ルールの閾値を求めるのだ～🌱
 * [getSurfaceNoiseMatchRatio]の逆関数なのだ✨
 */
fun getSurfaceNoiseThreshold(noiseKey: ResourceKey<NormalNoise.NoiseParameters>, matchRatio: Double): Double {
    return getNormalDistributionUpperProbabilityInverse(matchRatio, getStandardDeviation(noiseKey)) * SURFACE_NOISE_THRESHOLD_DIVISOR
}
