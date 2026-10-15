package miragefairy2024.client.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class UiStackLayoutTest {

    /** 中身の寸法が決まっている、試験用のコンポーネントなのだ～🌱 */
    private class FixedContentComponent(private val contentWidth: Int, private val contentHeight: Int) : UiComponent() {
        override fun determineContentWidth() = contentWidth
        override fun determineContentHeight() = contentHeight
    }

    private fun box(width: Int, height: Int) = FixedContentComponent(width, height)

    @Test
    fun `子は同じ場所へ重なるのだ～🌱`() {
        val layout = UiStackLayout()
        val a = box(10, 20)
        val b = box(30, 40)
        layout.child(a)
        layout.child(b)
        layout.inflate(Size.of(500, 500))
        assertEquals(0 to 0, a.x to a.y)
        assertEquals(0 to 0, b.x to b.y)
        // 自分の寸法は、最も大きい子に揃うのだ～🌱
        assertEquals(30, layout.width)
        assertEquals(40, layout.height)
    }

    @Test
    fun `寄せ方は縦も横も子ごとに効くのだ～🌱`() {
        val layout = UiStackLayout()
        layout.sizing(Sizing.fixed(100), Sizing.fixed(100))
        layout.alignment(Alignment.CENTER, Alignment.CENTER)
        val a = box(10, 20)
        val b = box(40, 60)
        layout.child(a)
        layout.child(b)
        layout.inflate(Size.of(500, 500))
        assertEquals(45 to 40, a.x to a.y)
        assertEquals(30 to 20, b.x to b.y)
    }

    @Test
    fun `fillの子は親の内側いっぱいに広がるのだ～🌱`() {
        val layout = UiStackLayout()
        layout.sizing(Sizing.fixed(100), Sizing.fixed(100))
        layout.padding(Insets.of(10))
        val a = box(0, 0)
        a.sizing(Sizing.fill(), Sizing.fill())
        layout.child(a)
        layout.inflate(Size.of(500, 500))
        assertEquals(80, a.width)
        assertEquals(80, a.height)
    }

    @Test
    fun `hitTestは後から足した子を先に返すのだ～🌱`() {
        val layout = UiStackLayout()
        val a = box(20, 20)
        val b = box(20, 20)
        layout.child(a)
        layout.child(b)
        layout.inflate(Size.of(500, 500))
        assertEquals(b, layout.hitTest(5, 5))
    }
}
