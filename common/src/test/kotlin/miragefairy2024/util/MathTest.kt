package miragefairy2024.util

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

class MathTest {

    /** [getNormalDistributionUpperProbability]が使っている近似式の、誤差の上限なのだ～🌱 */
    private val probabilityTolerance = 1.0E-7

    /** 標準正規分布の上側確率の、既知の値なのだ～🌱 */
    private val standardNormalDistributionUpperProbabilities = listOf(
        -2.0 to 0.977249868051821,
        -1.0 to 0.841344746068543,
        0.0 to 0.5,
        0.5 to 0.308537538725987,
        1.0 to 0.158655253931457,
        1.5 to 0.066807201268858,
        2.0 to 0.022750131948179,
        2.5 to 0.006209665325776,
        3.0 to 0.001349898031630,
        4.0 to 0.000031671241833,
    )

    @Test
    fun `getNormalDistributionUpperProbabilityは標準正規分布の既知の値を返すのだ～🌱`() {
        standardNormalDistributionUpperProbabilities.forEach { (x, expected) ->
            val actual = getNormalDistributionUpperProbability(x, 1.0)
            assertTrue(abs(actual - expected) <= probabilityTolerance, "x=$x expected=$expected actual=$actual")
        }
    }

    @Test
    fun `getNormalDistributionUpperProbabilityはxと標準偏差の比だけで決まるのだ～🌱`() {
        // 上側確率は、xと標準偏差の比だけで決まるのだ～🌱
        listOf(0.3050, 0.3121, 2.0).forEach { standardDeviation ->
            standardNormalDistributionUpperProbabilities.forEach { (x, expected) ->
                val actual = getNormalDistributionUpperProbability(x * standardDeviation, standardDeviation)
                assertTrue(abs(actual - expected) <= probabilityTolerance, "x=$x standardDeviation=$standardDeviation expected=$expected actual=$actual")
            }
        }
    }

    @Test
    fun `getNormalDistributionUpperProbabilityは0を中心に対称なのだ～🌱`() {
        listOf(0.0, 0.25, 0.5, 1.0, 2.0, 3.0, 5.0).forEach { x ->
            val sum = getNormalDistributionUpperProbability(x, 0.3121) + getNormalDistributionUpperProbability(-x, 0.3121)
            assertTrue(abs(sum - 1.0) <= probabilityTolerance * 2.0, "x=$x sum=$sum")
        }
    }

    @Test
    fun `getNormalDistributionUpperProbabilityは飽和しない範囲で単調減少するのだ～🌱`() {
        // 標準偏差の8倍より遠いところでは、返る値が0か1に飽和するから、厳密な単調減少はその内側でのみ成り立つのだ～🌱
        val standardDeviation = 0.3050
        var previous = Double.MAX_VALUE
        (-80..80).forEach { i ->
            val actual = getNormalDistributionUpperProbability(i / 10.0 * standardDeviation, standardDeviation)
            assertTrue(actual < previous, "i=$i previous=$previous actual=$actual")
            previous = actual
        }
    }

    @Test
    fun `getNormalDistributionUpperProbabilityは決して増加しないのだ～🌱`() {
        var previous = Double.MAX_VALUE
        (-1000..1000).forEach { i ->
            val actual = getNormalDistributionUpperProbability(i / 10.0, 0.3050)
            assertTrue(actual <= previous, "i=$i previous=$previous actual=$actual")
            previous = actual
        }
    }

    @Test
    fun `getNormalDistributionUpperProbabilityは0以上1以下に収まるのだ～🌱`() {
        (-1000..1000).forEach { i ->
            val actual = getNormalDistributionUpperProbability(i / 10.0, 0.3121)
            assertTrue(actual in 0.0..1.0, "i=$i actual=$actual")
        }
    }

    @Test
    fun `getXFromNormalDistributionUpperProbabilityは元のxを復元するのだ～🌱`() {
        // 二分探索の範囲は標準偏差の100倍までだから、その内側で確かめるのだ～🌱
        listOf(0.3050, 0.3121, 1.0).forEach { standardDeviation ->
            (-30..30).forEach { i ->
                val x = i / 10.0 * standardDeviation
                val probability = getNormalDistributionUpperProbability(x, standardDeviation)
                val actual = getXFromNormalDistributionUpperProbability(probability, standardDeviation)
                // 上側確率の傾きは端に行くほど緩やかになるから、許容する誤差も確率の誤差を傾きで割った大きさになるのだ🌱
                val tolerance = 0.01 * standardDeviation
                assertTrue(abs(actual - x) <= tolerance, "x=$x standardDeviation=$standardDeviation actual=$actual")
            }
        }
    }

    @Test
    fun `getXFromNormalDistributionUpperProbabilityは確率が2分の1のとき0を返すのだ～🌱`() {
        listOf(0.3050, 0.3121, 1.0).forEach { standardDeviation ->
            val actual = getXFromNormalDistributionUpperProbability(0.5, standardDeviation)
            assertTrue(abs(actual) <= 1.0E-9, "standardDeviation=$standardDeviation actual=$actual")
        }
    }

    @Test
    fun `getXFromNormalDistributionUpperProbabilityは単調減少するのだ～🌱`() {
        var previous = Double.MAX_VALUE
        (1..99).forEach { i ->
            val actual = getXFromNormalDistributionUpperProbability(i / 100.0, 0.3121)
            assertTrue(actual < previous, "i=$i previous=$previous actual=$actual")
            previous = actual
        }
    }

}
