package miragefairy2024.mod.recipeviewer.view

import miragefairy2024.mod.recipeviewer.views.AbsoluteView
import miragefairy2024.mod.recipeviewer.views.AbstractView
import miragefairy2024.mod.recipeviewer.views.Child
import miragefairy2024.mod.recipeviewer.views.MarginView
import miragefairy2024.mod.recipeviewer.views.PagingView
import miragefairy2024.mod.recipeviewer.views.StackView
import miragefairy2024.mod.recipeviewer.views.XListView
import miragefairy2024.mod.recipeviewer.views.YListView
import miragefairy2024.mod.recipeviewer.views.plusAssign
import miragefairy2024.util.Remover
import net.minecraft.network.chat.FormattedText
import net.minecraft.util.FormattedCharSequence
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

private val TEST_RENDERING_PROXY = object : RenderingProxy {
    override fun calculateTextWidth(text: FormattedText) = throw UnsupportedOperationException()
    override fun calculateTextWidth(text: FormattedCharSequence) = throw UnsupportedOperationException()
    override fun getTextHeight() = throw UnsupportedOperationException()
    override fun wrapText(text: FormattedText, maxWidth: Int): List<FormattedCharSequence> = throw UnsupportedOperationException()
}

private class RegionDependentView(private val horizontal: Boolean) : AbstractView(), PlaceableView {
    override val sizingX = if (horizontal) Sizing.FILL else Sizing.WRAP
    override val sizingY = if (horizontal) Sizing.WRAP else Sizing.FILL
    var calculationsX = 0
    var calculationsY = 0

    override fun calculateContentSizeX(): Int {
        calculationsX++
        return if (horizontal) 10 else requireRegionSizeY() * 2
    }

    override fun calculateContentSizeY(): Int {
        calculationsY++
        return if (horizontal) requireRegionSizeX() * 2 else 10
    }

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = viewPlacer.place(this, offset.sized(actualSize))
}

class AxisWiseActualSizeTest {

    @Test
    fun `横方向の配分済み領域を高さの計算に使うのだ`() {
        val view = XListView()
        view.sizingX = Sizing.FILL
        val first = RegionDependentView(true)
        val second = RegionDependentView(true)
        view += Child(view.createDefaultPosition().also {
            it.weight = 1.0
        }, first)
        view += Child(view.createDefaultPosition().also {
            it.weight = 1.0
        }, second)
        view.calculateSize(TEST_RENDERING_PROXY, IntPoint(120, 200))
        assertEquals(IntPoint(60, 120), first.actualSize)
        assertEquals(IntPoint(60, 120), second.actualSize)
        assertEquals(IntPoint(120, 120), view.actualSize)
        assertEquals(1, first.calculationsX)
        assertEquals(1, first.calculationsY)
    }

    @Test
    fun `縦方向の配分済み領域を幅の計算に使うのだ`() {
        val view = YListView()
        view.sizingY = Sizing.FILL
        val first = RegionDependentView(false)
        val second = RegionDependentView(false)
        view += Child(view.createDefaultPosition().also {
            it.weight = 1.0
        }, first)
        view += Child(view.createDefaultPosition().also {
            it.weight = 1.0
        }, second)
        view.calculateSize(TEST_RENDERING_PROXY, IntPoint(200, 120))
        assertEquals(IntPoint(120, 60), first.actualSize)
        assertEquals(IntPoint(120, 60), second.actualSize)
        assertEquals(IntPoint(120, 120), view.actualSize)
        assertEquals(1, first.calculationsX)
        assertEquals(1, first.calculationsY)
    }

    @Test
    fun `余白を除いた幅で高さを計算するのだ`() {
        val view = StackView()
        view.sizingX = Sizing.FILL
        val child = RegionDependentView(true)
        val margin = MarginView(5, 7, 11, 13)
        margin += child
        view += margin
        view.calculateSize(TEST_RENDERING_PROXY, IntPoint(120, 500))
        assertEquals(IntPoint(108, 216), child.actualSize)
        assertEquals(IntPoint(120, 240), view.actualSize)
    }

    @Test
    fun `余白を除いた高さで幅を計算するのだ`() {
        val view = StackView()
        view.sizingY = Sizing.FILL
        val child = RegionDependentView(false)
        val margin = MarginView(11, 13, 5, 7)
        margin += child
        view += margin
        view.calculateSize(TEST_RENDERING_PROXY, IntPoint(500, 120))
        assertEquals(IntPoint(216, 108), child.actualSize)
        assertEquals(IntPoint(240, 120), view.actualSize)
    }

    @Test
    fun `固定された両軸の領域で逆方向の依存を持つ子を共存させるのだ`() {
        val view = StackView()
        view.sizingX = Sizing.FILL
        view.sizingY = Sizing.FILL
        val horizontal = RegionDependentView(true)
        val vertical = RegionDependentView(false)
        view += horizontal
        view += vertical
        view.calculateSize(TEST_RENDERING_PROXY, IntPoint(80, 60))
        assertEquals(IntPoint(80, 160), horizontal.actualSize)
        assertEquals(IntPoint(120, 60), vertical.actualSize)
        assertEquals(IntPoint(120, 160), view.contentSize)
        assertEquals(IntPoint(80, 60), view.actualSize)
    }

    @Test
    fun `Y軸から準備しても横方向の配分を参照できるのだ`() {
        val view = XListView()
        view.sizingX = Sizing.FILL
        val child = RegionDependentView(true)
        view += Child(view.createDefaultPosition().also {
            it.weight = 1.0
        }, child)
        view.setRenderingProxy(TEST_RENDERING_PROXY)
        view.calculateContentSizeY { 120 }
        view.calculateContentSizeX { 500 }
        view.calculateActualSizeY { 500 }
        view.calculateActualSizeX { 120 }
        assertEquals(240, view.actualSizeY)
        assertEquals(120, view.actualSizeX)
        view.finishSizeCalculation()
        assertEquals(IntPoint(120, 240), child.actualSize)
    }

    @Test
    fun `重み付き領域の丸めと配置を維持するのだ`() {
        val view = XListView()
        view.sizingX = Sizing.FILL
        val children = List(3) { RegionDependentView(true) }
        children.forEach {
            view += Child(view.createDefaultPosition().also {
                it.weight = 1.0
            }, it)
        }
        view.calculateSize(TEST_RENDERING_PROXY, IntPoint(101, 500))
        assertEquals(listOf(34, 33, 34), children.map { it.actualSizeX })
        val bounds = mutableListOf<IntRectangle>()
        view.attachTo(IntPoint(5, 7)) { _, region ->
            bounds += region
            Remover {}
        }
        assertEquals(listOf(IntRectangle(5, 7, 34, 68), IntRectangle(39, 7, 33, 66), IntRectangle(72, 7, 34, 68)), bounds)
    }

    @Test
    fun `縦方向も重み付き領域の丸めと配置を維持するのだ`() {
        val view = YListView()
        view.sizingY = Sizing.FILL
        val children = List(3) { RegionDependentView(false) }
        children.forEach {
            view += Child(view.createDefaultPosition().also {
                it.weight = 1.0
            }, it)
        }
        view.calculateSize(TEST_RENDERING_PROXY, IntPoint(500, 101))
        assertEquals(listOf(34, 33, 34), children.map { it.actualSizeY })
        val bounds = mutableListOf<IntRectangle>()
        view.attachTo(IntPoint(7, 5)) { _, region ->
            bounds += region
            Remover {}
        }
        assertEquals(listOf(IntRectangle(7, 5, 68, 34), IntRectangle(7, 39, 66, 33), IntRectangle(7, 72, 68, 34)), bounds)
    }

    @Test
    fun `横方向の予約サイズを無視する指定を維持するのだ`() {
        val view = XListView()
        view.sizingX = Sizing.FILL
        val first = RegionDependentView(true)
        val second = RegionDependentView(true)
        view += Child(view.createDefaultPosition().also {
            it.weight = 1.0
            it.ignoreLayoutX = true
        }, first)
        view += Child(view.createDefaultPosition().also {
            it.weight = 1.0
        }, second)
        view.calculateSize(TEST_RENDERING_PROXY, IntPoint(120, 200))
        assertEquals(IntPoint(55, 110), first.actualSize)
        assertEquals(IntPoint(65, 130), second.actualSize)
    }

    @Test
    fun `縦方向の予約サイズを無視する指定を維持するのだ`() {
        val view = YListView()
        view.sizingY = Sizing.FILL
        val first = RegionDependentView(false)
        val second = RegionDependentView(false)
        view += Child(view.createDefaultPosition().also {
            it.weight = 1.0
            it.ignoreLayoutY = true
        }, first)
        view += Child(view.createDefaultPosition().also {
            it.weight = 1.0
        }, second)
        view.calculateSize(TEST_RENDERING_PROXY, IntPoint(200, 120))
        assertEquals(IntPoint(110, 55), first.actualSize)
        assertEquals(IntPoint(130, 65), second.actualSize)
    }

    @Test
    fun `絶対配置では指定された領域の軸だけを参照するのだ`() {
        val view = AbsoluteView(IntPoint(500, 500))
        val horizontal = RegionDependentView(true)
        val vertical = RegionDependentView(false)
        view += Child(AbsoluteView.Bounds(IntRectangle(0, 0, 40, 30)), horizontal)
        view += Child(AbsoluteView.Bounds(IntRectangle(0, 0, 40, 30)), vertical)
        view.calculateSize(TEST_RENDERING_PROXY, IntPoint(1000, 1000))
        assertEquals(IntPoint(40, 80), horizontal.actualSize)
        assertEquals(IntPoint(60, 30), vertical.actualSize)
    }

    @Test
    fun `絶対配置のFillでは両方向の依存を解決できるのだ`() {
        val view = AbsoluteView(IntPoint(80, 60))
        val horizontal = RegionDependentView(true)
        val vertical = RegionDependentView(false)
        view += horizontal
        view += vertical
        view.calculateSize(TEST_RENDERING_PROXY, IntPoint(1000, 1000))
        assertEquals(IntPoint(80, 160), horizontal.actualSize)
        assertEquals(IntPoint(120, 60), vertical.actualSize)
    }

    @Test
    fun `絶対配置のOffsetでは自分の最小サイズから領域を取得するのだ`() {
        val view = AbsoluteView(IntPoint(80, 60))
        val horizontal = RegionDependentView(true)
        val vertical = RegionDependentView(false)
        view += Child(AbsoluteView.Offset(IntPoint(5, 7)), horizontal)
        view += Child(AbsoluteView.Offset(IntPoint(7, 5)), vertical)
        view.calculateSize(TEST_RENDERING_PROXY, IntPoint(1000, 1000))
        assertEquals(IntPoint(10, 20), horizontal.actualSize)
        assertEquals(IntPoint(20, 10), vertical.actualSize)
    }

    @Test
    fun `ページ生成後の子も配分された領域からサイズを計算するのだ`() {
        val view = PagingView()
        val children = List(2) { RegionDependentView(true) }
        view += ChildrenGenerator { _, regionSize ->
            assertEquals(IntPoint(40, 100), regionSize)
            children.map { Child(Alignment.START, it) }
        }
        view.calculateSize(TEST_RENDERING_PROXY, IntPoint(40, 100))
        assertEquals(2, view.pageCount.value)
        children.forEach {
            assertEquals(IntPoint(40, 80), it.actualSize)
        }
    }

}
