package miragefairy2024.client.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class SizingTest {

    /** 中身の長さを数えた回数を記録しながら、その長さを返すのだ～🌱 */
    private class ContentLengthCounter(private val length: Int) : () -> Int {
        var count = 0
            private set

        override fun invoke(): Int {
            count++
            return length
        }
    }

    @Test
    fun `fixedは空間に関わらず指定の長さを返すのだ～🌱`() {
        listOf(0, 1, 100, 1000).forEach { space ->
            assertEquals(42, Sizing.fixed(42).inflate(space) { 7 }, "space=$space")
        }
    }

    @Test
    fun `contentは中身の長さに余白を両端それぞれへ足すのだ～🌱`() {
        assertEquals(30, Sizing.content().inflate(1000) { 30 })
        assertEquals(44, Sizing.content(7).inflate(1000) { 30 })
    }

    @Test
    fun `fillは空間の割合を四捨五入して返すのだ～🌱`() {
        assertEquals(100, Sizing.fill().inflate(100) { 0 })
        assertEquals(50, Sizing.fill(50).inflate(100) { 0 })
        // 2 で割り切れない空間は、大きい側へ丸まるのだ～🌱
        assertEquals(51, Sizing.fill(50).inflate(101) { 0 })
    }

    @Test
    fun `expandはfillと同じ式で長さを決めるのだ～🌱`() {
        listOf(0, 50, 100).forEach { percent ->
            listOf(0, 1, 100, 101, 999).forEach { space ->
                val fill = Sizing.fill(percent).inflate(space) { 0 }
                val expand = Sizing.expand(percent).inflate(space) { 0 }
                assertEquals(fill, expand, "percent=$percent space=$space")
            }
        }
    }

    @Test
    fun `中身の長さを数えるのはcontentのときだけなのだ～🌱`() {
        listOf(Sizing.fixed(10), Sizing.fill(), Sizing.expand()).forEach { sizing ->
            val counter = ContentLengthCounter(30)
            sizing.inflate(100, counter)
            assertEquals(0, counter.count, "sizing=$sizing")
        }
        val counter = ContentLengthCounter(30)
        Sizing.content().inflate(100, counter)
        assertEquals(1, counter.count)
    }

    @Test
    fun `isContentとisExpandはそれぞれの種類だけで立つのだ～🌱`() {
        assertEquals(listOf(false, true, false, false), listOf(Sizing.fixed(10), Sizing.content(), Sizing.fill(), Sizing.expand()).map { it.isContent })
        assertEquals(listOf(false, false, false, true), listOf(Sizing.fixed(10), Sizing.content(), Sizing.fill(), Sizing.expand()).map { it.isExpand })
    }

    @Test
    fun `inflateWithExpandはexpandの子へ残りの空間を渡すのだ～🌱`() {
        val children = listOf(Sizing.fixed(30), Sizing.expand(), Sizing.fixed(20))
        val lengths = inflateWithExpand(children, 100, 0, { it }, { sizing, space -> sizing.inflate(space) { 0 } })
        assertEquals(listOf(30, 50, 20), lengths)
    }

    @Test
    fun `inflateWithExpandは子の間の隙間も残りの空間から引くのだ～🌱`() {
        val children = listOf(Sizing.fixed(30), Sizing.expand(), Sizing.fixed(20))
        val lengths = inflateWithExpand(children, 100, 4, { it }, { sizing, space -> sizing.inflate(space) { 0 } })
        // 隙間は子の個数より 1 個少ないのだ～🌱
        assertEquals(listOf(30, 42, 20), lengths)
    }

    @Test
    fun `inflateWithExpandは残りの空間が足りなくても負の長さを渡さないのだ～🌱`() {
        val children = listOf(Sizing.fixed(80), Sizing.expand(), Sizing.fixed(80))
        val lengths = inflateWithExpand(children, 100, 0, { it }, { sizing, space -> sizing.inflate(space) { 0 } })
        assertEquals(listOf(80, 0, 80), lengths)
    }

    @Test
    fun `inflateWithExpandは複数のexpandへ残りの空間をそれぞれ渡すのだ～🌱`() {
        val children = listOf(Sizing.expand(50), Sizing.expand(50))
        val lengths = inflateWithExpand(children, 100, 0, { it }, { sizing, space -> sizing.inflate(space) { 0 } })
        assertEquals(listOf(50, 50), lengths)
    }

    @Test
    fun `childSpaceはcontentの親のときだけ上の親の空間を渡すのだ～🌱`() {
        assertEquals(200, childSpace(Sizing.content(), 50, 200, 0))
        assertEquals(50, childSpace(Sizing.fill(), 50, 200, 0))
        assertEquals(50, childSpace(Sizing.fixed(50), 50, 200, 0))
        assertEquals(50, childSpace(Sizing.expand(), 50, 200, 0))
    }

    @Test
    fun `childSpaceは親の内側の余白を引くのだ～🌱`() {
        assertEquals(186, childSpace(Sizing.content(), 50, 200, 14))
        assertEquals(36, childSpace(Sizing.fill(), 50, 200, 14))
    }

    @Test
    fun `Insetsは四方の余白を縦と横の合計にまとめるのだ～🌱`() {
        val insets = Insets.of(1, 2, 3, 4)
        assertEquals(7, insets.horizontal)
        assertEquals(3, insets.vertical)
        assertEquals(Insets(5, 5, 5, 5), Insets.of(5))
        assertEquals(Insets(5, 5, 0, 0), Insets.vertical(5))
    }
}
