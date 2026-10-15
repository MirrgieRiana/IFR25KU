package miragefairy2024.client.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class UiFlowLayoutTest {

    /** 中身の寸法が決まっている、試験用のコンポーネントなのだ～🌱 */
    private class FixedContentComponent(private val contentWidth: Int, private val contentHeight: Int) : UiComponent() {
        override fun determineContentWidth() = contentWidth
        override fun determineContentHeight() = contentHeight
    }

    private fun box(width: Int, height: Int) = FixedContentComponent(width, height)

    @Test
    fun `縦向きは子を上から順に積むのだ～🌱`() {
        val layout = UiFlowLayout(UiFlowLayout.Direction.VERTICAL)
        val a = box(10, 20)
        val b = box(30, 40)
        layout.child(a)
        layout.child(b)
        layout.inflate(Size.of(500, 500))
        assertEquals(0 to 0, a.x to a.y)
        assertEquals(0 to 20, b.x to b.y)
        // 横は最も広い子に、縦は子の合計に揃うのだ～🌱
        assertEquals(30, layout.width)
        assertEquals(60, layout.height)
    }

    @Test
    fun `横向きは子を左から順に並べるのだ～🌱`() {
        val layout = UiFlowLayout(UiFlowLayout.Direction.HORIZONTAL)
        val a = box(10, 20)
        val b = box(30, 40)
        layout.child(a)
        layout.child(b)
        layout.inflate(Size.of(500, 500))
        assertEquals(0 to 0, a.x to a.y)
        assertEquals(10 to 0, b.x to b.y)
        assertEquals(40, layout.width)
        assertEquals(40, layout.height)
    }

    @Test
    fun `隙間は子と子の間にだけ入るのだ～🌱`() {
        val layout = UiFlowLayout(UiFlowLayout.Direction.VERTICAL)
        layout.gap(5)
        val a = box(10, 20)
        val b = box(10, 20)
        val c = box(10, 20)
        listOf(a, b, c).forEach { layout.child(it) }
        layout.inflate(Size.of(500, 500))
        assertEquals(listOf(0, 25, 50), listOf(a.y, b.y, c.y))
        assertEquals(70, layout.height)
    }

    @Test
    fun `内側の余白は子の位置と自分の寸法の両方へ効くのだ～🌱`() {
        val layout = UiFlowLayout(UiFlowLayout.Direction.VERTICAL)
        layout.padding(Insets.of(7))
        val a = box(10, 20)
        layout.child(a)
        layout.inflate(Size.of(500, 500))
        assertEquals(7 to 7, a.x to a.y)
        assertEquals(24, layout.width)
        assertEquals(34, layout.height)
    }

    @Test
    fun `expandの子は他の子が取った後の残りを受け取るのだ～🌱`() {
        val layout = UiFlowLayout(UiFlowLayout.Direction.VERTICAL)
        layout.sizing(Sizing.fixed(100), Sizing.fixed(100))
        val a = box(10, 0)
        a.sizing(Sizing.content(), Sizing.fixed(30))
        val b = box(10, 0)
        b.sizing(Sizing.content(), Sizing.expand())
        layout.child(a)
        layout.child(b)
        layout.inflate(Size.of(500, 500))
        assertEquals(30, a.height)
        assertEquals(70, b.height)
        assertEquals(30, b.y)
    }

    @Test
    fun `並べる方向と垂直な方向のfillは親の内側いっぱいに広がるのだ～🌱`() {
        val layout = UiFlowLayout(UiFlowLayout.Direction.VERTICAL)
        layout.sizing(Sizing.fixed(100), Sizing.fixed(100))
        layout.padding(Insets.of(10))
        val a = box(0, 20)
        a.sizing(Sizing.fill(), Sizing.content())
        layout.child(a)
        layout.inflate(Size.of(500, 500))
        assertEquals(80, a.width)
    }

    @Test
    fun `子のマージンは位置と占める長さの両方へ効くのだ～🌱`() {
        val layout = UiFlowLayout(UiFlowLayout.Direction.VERTICAL)
        val a = box(10, 20)
        a.margins(Insets.of(3))
        val b = box(10, 20)
        layout.child(a)
        layout.child(b)
        layout.inflate(Size.of(500, 500))
        assertEquals(3 to 3, a.x to a.y)
        assertEquals(26, b.y)
        assertEquals(46, layout.height)
    }

    @Test
    fun `並べる方向の寄せ方は子の塊ごと動かすのだ～🌱`() {
        val layout = UiFlowLayout(UiFlowLayout.Direction.VERTICAL)
        layout.sizing(Sizing.fixed(100), Sizing.fixed(100))
        layout.alignment(Alignment.START, Alignment.END)
        val a = box(10, 20)
        val b = box(10, 20)
        layout.child(a)
        layout.child(b)
        layout.inflate(Size.of(500, 500))
        assertEquals(60, a.y)
        assertEquals(80, b.y)
    }

    @Test
    fun `並べる方向と垂直な寄せ方は子ごとに効くのだ～🌱`() {
        val layout = UiFlowLayout(UiFlowLayout.Direction.VERTICAL)
        layout.sizing(Sizing.fixed(100), Sizing.fixed(100))
        layout.alignment(Alignment.CENTER, Alignment.START)
        val a = box(10, 20)
        val b = box(40, 20)
        layout.child(a)
        layout.child(b)
        layout.inflate(Size.of(500, 500))
        // 幅の違う子が、それぞれ中央へ寄るのだ～🌱
        assertEquals(45, a.x)
        assertEquals(30, b.x)
    }

    @Test
    fun `横向きでも隙間は子と子の間にだけ入るのだ～🌱`() {
        val layout = UiFlowLayout(UiFlowLayout.Direction.HORIZONTAL)
        layout.gap(5)
        val a = box(10, 20)
        val b = box(10, 20)
        val c = box(10, 20)
        listOf(a, b, c).forEach { layout.child(it) }
        layout.inflate(Size.of(500, 500))
        assertEquals(listOf(0, 15, 30), listOf(a.x, b.x, c.x))
        assertEquals(40, layout.width)
    }

    @Test
    fun `横向きでもexpandの子は残りを受け取るのだ～🌱`() {
        val layout = UiFlowLayout(UiFlowLayout.Direction.HORIZONTAL)
        layout.sizing(Sizing.fixed(100), Sizing.fixed(100))
        val a = box(0, 10)
        a.sizing(Sizing.fixed(30), Sizing.content())
        val b = box(0, 10)
        b.sizing(Sizing.expand(), Sizing.content())
        layout.child(a)
        layout.child(b)
        layout.inflate(Size.of(500, 500))
        assertEquals(30, a.width)
        assertEquals(70, b.width)
        assertEquals(30, b.x)
    }

    @Test
    fun `横向きでも並べる方向の寄せ方は子の塊ごと動かすのだ～🌱`() {
        val layout = UiFlowLayout(UiFlowLayout.Direction.HORIZONTAL)
        layout.sizing(Sizing.fixed(100), Sizing.fixed(100))
        layout.alignment(Alignment.END, Alignment.START)
        val a = box(10, 20)
        val b = box(10, 20)
        layout.child(a)
        layout.child(b)
        layout.inflate(Size.of(500, 500))
        assertEquals(80, a.x)
        assertEquals(90, b.x)
    }

    @Test
    fun `横向きでも内側の余白は子の位置と自分の寸法の両方へ効くのだ～🌱`() {
        val layout = UiFlowLayout(UiFlowLayout.Direction.HORIZONTAL)
        layout.padding(Insets.of(7))
        val a = box(10, 20)
        layout.child(a)
        layout.inflate(Size.of(500, 500))
        assertEquals(7 to 7, a.x to a.y)
        assertEquals(24, layout.width)
        assertEquals(34, layout.height)
    }

    @Test
    fun `絶対座標は親を遡って足した位置なのだ～🌱`() {
        val outer = UiFlowLayout(UiFlowLayout.Direction.VERTICAL)
        outer.padding(Insets.of(10))
        val inner = UiFlowLayout(UiFlowLayout.Direction.VERTICAL)
        inner.padding(Insets.of(5))
        val leaf = box(10, 10)
        inner.child(leaf)
        outer.child(inner)
        outer.inflate(Size.of(500, 500))
        outer.moveTo(100, 200)
        assertEquals(115, leaf.absoluteX)
        assertEquals(215, leaf.absoluteY)
    }

    @Test
    fun `containsPointはコンポーネントの右端と下端を含まないのだ～🌱`() {
        val layout = UiFlowLayout(UiFlowLayout.Direction.VERTICAL)
        val a = box(10, 20)
        layout.child(a)
        layout.inflate(Size.of(500, 500))
        layout.moveTo(100, 200)
        assertEquals(true, a.containsPoint(100, 200))
        assertEquals(true, a.containsPoint(109, 219))
        assertEquals(false, a.containsPoint(110, 200))
        assertEquals(false, a.containsPoint(100, 220))
        assertEquals(false, a.containsPoint(99, 200))
    }

    @Test
    fun `hitTestは後から足した子を先に返すのだ～🌱`() {
        val layout = UiFlowLayout(UiFlowLayout.Direction.VERTICAL)
        val a = box(10, 10)
        val b = box(10, 10)
        layout.child(a)
        layout.child(b)
        layout.inflate(Size.of(500, 500))
        assertEquals(a, layout.hitTest(5, 5))
        assertEquals(b, layout.hitTest(5, 15))
        assertEquals(null, layout.hitTest(50, 50))
    }

    @Test
    fun `hitTestは入れ子の奥の子まで辿るのだ～🌱`() {
        val outer = UiFlowLayout(UiFlowLayout.Direction.VERTICAL)
        val inner = UiFlowLayout(UiFlowLayout.Direction.VERTICAL)
        val leaf = box(10, 10)
        inner.child(leaf)
        outer.child(inner)
        outer.inflate(Size.of(500, 500))
        assertEquals(leaf, outer.hitTest(5, 5))
    }

    @Test
    fun `childは親への参照を繋いで、clearChildrenは外すのだ～🌱`() {
        val layout = UiFlowLayout(UiFlowLayout.Direction.VERTICAL)
        val a = box(10, 10)
        layout.child(a)
        assertEquals(layout, a.parent)
        layout.clearChildren()
        assertEquals(null, a.parent)
    }

    @Test
    fun `子が無いときは中身の寸法が0になるのだ～🌱`() {
        val layout = UiFlowLayout(UiFlowLayout.Direction.VERTICAL)
        layout.inflate(Size.of(500, 500))
        assertEquals(0, layout.width)
        assertEquals(0, layout.height)
    }

    @Test
    fun `入れ子にした親も中身に合わせて縮むのだ～🌱`() {
        val outer = UiFlowLayout(UiFlowLayout.Direction.HORIZONTAL)
        val inner = UiFlowLayout(UiFlowLayout.Direction.VERTICAL)
        inner.child(box(10, 20))
        inner.child(box(10, 20))
        outer.child(inner)
        outer.child(box(5, 5))
        outer.inflate(Size.of(500, 500))
        assertEquals(10, inner.width)
        assertEquals(40, inner.height)
        assertEquals(15, outer.width)
        assertEquals(40, outer.height)
    }
}
