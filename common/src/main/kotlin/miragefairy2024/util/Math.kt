package miragefairy2024.util

import net.minecraft.util.RandomSource
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.sqrt

/** 期待値がdになるように整数の乱数を生成します。 */
fun RandomSource.randomInt(d: Double): Int {
    val i = floor(d).toInt()
    val mod = d - i
    return if (this.nextDouble() < mod) i + 1 else i
}

fun RandomSource.randomBoolean(maxRate: Int, rate: Int): Boolean {
    if (rate >= maxRate) return true
    if (rate <= 0) return false
    return this.nextInt(maxRate) < rate
}

val Int.bitCount: Int
    get() {
        var b = 0
        var a = this
        while (a != 0) {
            if (a and 0x1 != 0) b++
            a = a ushr 1
        }
        return b
    }

/** 平均0、標準偏差[standardDeviation]の正規分布において、[x]より大きい値が得られる確率を求めるのだ～🌱 */
fun getNormalDistributionUpperProbability(x: Double, standardDeviation: Double): Double {
    // 誤差関数の近似式はAbramowitz and Stegun 7.1.26によるもので、絶対誤差は1.5e-7以下なのだ～🌱
    val z = x / (standardDeviation * sqrt(2.0))
    val t = 1.0 / (1.0 + 0.3275911 * abs(z))
    val erfOfAbsoluteZ = 1.0 - (((((1.061405429 * t - 1.453152027) * t + 1.421413741) * t - 0.284496736) * t + 0.254829592) * t) * exp(-z * z)
    return 0.5 * (1.0 - if (z < 0) -erfOfAbsoluteZ else erfOfAbsoluteZ)
}

/** [getNormalDistributionUpperProbability]の逆関数なのだ～🌱 二分探索で求めるのだ✨ */
fun getNormalDistributionUpperProbabilityInverse(probability: Double, standardDeviation: Double): Double {
    var low = -100.0 * standardDeviation
    var high = 100.0 * standardDeviation
    repeat(80) {
        val middle = (low + high) / 2.0
        if (getNormalDistributionUpperProbability(middle, standardDeviation) > probability) low = middle else high = middle
    }
    return (low + high) / 2.0
}
