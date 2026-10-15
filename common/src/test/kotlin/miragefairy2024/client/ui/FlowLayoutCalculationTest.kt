package miragefairy2024.client.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class FlowLayoutCalculationTest {

    @Test
    fun `flowAlongは子を隙間を空けて順に並べるのだ～🌱`() {
        val result = flowAlong(listOf(10, 20, 30), 4)
        assertEquals(listOf(0, 14, 38), result.offsets)
        assertEquals(68, result.length)
    }

    @Test
    fun `flowAlongは隙間が無いときに子を詰めて並べるのだ～🌱`() {
        val result = flowAlong(listOf(10, 20, 30), 0)
        assertEquals(listOf(0, 10, 30), result.offsets)
        assertEquals(60, result.length)
    }

    @Test
    fun `flowAlongは子が1個のときに隙間を数えないのだ～🌱`() {
        val result = flowAlong(listOf(10), 4)
        assertEquals(listOf(0), result.offsets)
        assertEquals(10, result.length)
    }

    @Test
    fun `flowAlongは子が無いときに長さを0にするのだ～🌱`() {
        val result = flowAlong(listOf(), 4)
        assertEquals(listOf(), result.offsets)
        assertEquals(0, result.length)
    }

    @Test
    fun `alignは寄せる先に応じて始点を返すのだ～🌱`() {
        assertEquals(0, Alignment.START.align(4, 10))
        assertEquals(3, Alignment.CENTER.align(4, 10))
        assertEquals(6, Alignment.END.align(4, 10))
    }

    @Test
    fun `alignAlongは寄せる先に応じて補正の量を返すのだ～🌱`() {
        assertEquals(0, alignAlong(Alignment.START, 4, 10))
        assertEquals(3, alignAlong(Alignment.CENTER, 4, 10))
        assertEquals(6, alignAlong(Alignment.END, 4, 10))
    }

    @Test
    fun `alignとalignAlongは偶奇が異なる場合に1だけ異なるのだ～🌱`() {
        // 空間の長さが偶数で、コンポーネントの長さが奇数なのだ～🌱
        assertEquals(4, Alignment.CENTER.align(3, 10))
        assertEquals(3, alignAlong(Alignment.CENTER, 3, 10))
    }

    @Test
    fun `alignとalignAlongは偶奇が揃っている場合に一致するのだ～🌱`() {
        listOf(10 to 4, 11 to 5, 10 to 2, 100 to 40).forEach { (space, length) ->
            assertEquals(Alignment.CENTER.align(length, space), alignAlong(Alignment.CENTER, length, space), "space=$space length=$length")
        }
    }

    @Test
    fun `STARTとENDはalignとalignAlongで常に一致するのだ～🌱`() {
        (0..30).forEach { space ->
            (0..30).forEach { length ->
                assertEquals(Alignment.START.align(length, space), alignAlong(Alignment.START, length, space), "space=$space length=$length")
                assertEquals(Alignment.END.align(length, space), alignAlong(Alignment.END, length, space), "space=$space length=$length")
            }
        }
    }
}
