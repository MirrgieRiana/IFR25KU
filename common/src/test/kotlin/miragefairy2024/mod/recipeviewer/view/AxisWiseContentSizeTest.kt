package miragefairy2024.mod.recipeviewer.view

import miragefairy2024.mod.recipeviewer.views.AbstractView
import miragefairy2024.mod.recipeviewer.views.Child
import miragefairy2024.mod.recipeviewer.views.MarginView
import miragefairy2024.mod.recipeviewer.views.MinContentSizeXView
import miragefairy2024.mod.recipeviewer.views.SpaceView
import miragefairy2024.mod.recipeviewer.views.StackView
import miragefairy2024.mod.recipeviewer.views.XListView
import miragefairy2024.mod.recipeviewer.views.YListView
import miragefairy2024.mod.recipeviewer.views.plusAssign
import miragefairy2024.util.Remover
import net.minecraft.network.chat.FormattedText
import net.minecraft.util.FormattedCharSequence
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** 文字の寸法を測らないViewだけを扱うため、どの関数も呼ばれないのだ～🌱 */
private val TEST_RENDERING_PROXY = object : RenderingProxy {
    override fun calculateTextWidth(text: FormattedText) = throw UnsupportedOperationException()
    override fun calculateTextWidth(text: FormattedCharSequence) = throw UnsupportedOperationException()
    override fun getTextHeight() = throw UnsupportedOperationException()
    override fun wrapText(text: FormattedText, maxWidth: Int): List<FormattedCharSequence> = throw UnsupportedOperationException()
}

/** 逆側の軸の利用可能なサイズを、指定した軸の計算の中で要求するViewなのだ～🌱 */
private class RequirementView(private val requiresRegionSizeX: Boolean, private val requiresRegionSizeY: Boolean) : AbstractView() {
    var observedRegionSizeX: Int? = null
    var observedRegionSizeY: Int? = null
    override val sizingX = Sizing.WRAP
    override val sizingY = Sizing.WRAP
    override fun calculateContentSizeX(): Int {
        if (requiresRegionSizeY) observedRegionSizeY = requireRegionSizeY()
        return 10
    }

    override fun calculateContentSizeY(): Int {
        if (requiresRegionSizeX) observedRegionSizeX = requireRegionSizeX()
        return 20
    }

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = Remover {}
}

class AxisWiseContentSizeTest {

    @Test
    fun `横に並べるViewは、X成分を足してY成分の最大を取るのだ`() {
        val view = XListView()
        view += SpaceView(IntPoint(10, 20))
        view += SpaceView(IntPoint(30, 5))
        view.calculateSize(TEST_RENDERING_PROXY, IntPoint(100, 100))
        assertEquals(40, view.contentSizeX)
        assertEquals(20, view.contentSizeY)
    }

    @Test
    fun `縦に並べるViewは、Y成分を足してX成分の最大を取るのだ`() {
        val view = YListView()
        view += SpaceView(IntPoint(10, 20))
        view += SpaceView(IntPoint(30, 5))
        view.calculateSize(TEST_RENDERING_PROXY, IntPoint(100, 100))
        assertEquals(30, view.contentSizeX)
        assertEquals(25, view.contentSizeY)
    }

    @Test
    fun `重ねるViewは、両方の軸で最大を取るのだ`() {
        val view = StackView()
        view += SpaceView(IntPoint(10, 20))
        view += SpaceView(IntPoint(30, 5))
        view.calculateSize(TEST_RENDERING_PROXY, IntPoint(100, 100))
        assertEquals(30, view.contentSizeX)
        assertEquals(20, view.contentSizeY)
    }

    @Test
    fun `余白のViewは、軸ごとに独立して余白を足すのだ`() {
        val view = MarginView(1, 2, 3, 4)
        view += SpaceView(IntPoint(10, 20))
        view.calculateSize(TEST_RENDERING_PROXY, IntPoint(100, 100))
        assertEquals(13, view.contentSizeX)
        assertEquals(27, view.contentSizeY)
    }

    @Test
    fun `最小サイズのViewは、指定した軸だけを広げるのだ`() {
        val view = MinContentSizeXView(100)
        view += SpaceView(IntPoint(10, 20))
        view.calculateSize(TEST_RENDERING_PROXY, IntPoint(100, 100))
        assertEquals(100, view.contentSizeX)
        assertEquals(20, view.contentSizeY)
    }

    @Test
    fun `Y成分の計算では、X方向の利用可能なサイズを参照できるのだ`() {
        val view = RequirementView(requiresRegionSizeX = true, requiresRegionSizeY = false)
        view.calculateSize(TEST_RENDERING_PROXY, IntPoint(100, 200))
        assertEquals(100, view.observedRegionSizeX)
        assertEquals(20, view.contentSizeY)
    }

    @Test
    fun `X成分の計算では、Y方向の利用可能なサイズを参照できるのだ`() {
        val view = RequirementView(requiresRegionSizeX = false, requiresRegionSizeY = true)
        view.calculateSize(TEST_RENDERING_PROXY, IntPoint(100, 200))
        assertEquals(200, view.observedRegionSizeY)
        assertEquals(10, view.contentSizeX)
    }

    @Test
    fun `計算より前にX成分を読むと、Viewのクラス名を伴う例外が出るのだ`() {
        val view = SpaceView(IntPoint(10, 20))
        val exception = assertThrows(IllegalStateException::class.java) {
            view.contentSizeX
        }
        assertEquals("The content size of the X axis is not calculated yet: ${SpaceView::class.java.name}", exception.message)
    }

    @Test
    fun `実サイズのX成分は、Y成分を準備せずに計算できるのだ`() {
        val view = SpaceView(IntPoint(10, 20))
        view.setRenderingProxy(TEST_RENDERING_PROXY)
        view.calculateContentSizeX { throw AssertionError() }
        view.calculateActualSizeX { 100 }
        assertEquals(10, view.actualSizeX)
        assertThrows(IllegalStateException::class.java) { view.actualSizeY }
    }

    @Test
    fun `実サイズのY成分は、X成分を準備せずに計算できるのだ`() {
        val view = SpaceView(IntPoint(10, 20))
        view.setRenderingProxy(TEST_RENDERING_PROXY)
        view.calculateContentSizeY { throw AssertionError() }
        view.calculateActualSizeY { 100 }
        assertEquals(20, view.actualSizeY)
        assertThrows(IllegalStateException::class.java) { view.actualSizeX }
    }

    @Test
    fun `両成分が逆側の利用可能なサイズを要求しても、外部で確定していれば循環しないのだ`() {
        val view = RequirementView(requiresRegionSizeX = true, requiresRegionSizeY = true)
        view.calculateSize(TEST_RENDERING_PROXY, IntPoint(100, 200))
        assertEquals(100, view.observedRegionSizeX)
        assertEquals(200, view.observedRegionSizeY)
    }

    @Test
    fun `子が参照する幅は、WRAPで選んだ実サイズではなく配分された領域なのだ`() {
        val view = XListView()
        view.sizingX = Sizing.FILL
        val child = RequirementView(requiresRegionSizeX = true, requiresRegionSizeY = false)
        view += Child(view.createDefaultPosition().also {
            it.weight = 1.0
        }, child)
        view += SpaceView(IntPoint(10, 20))
        view.calculateSize(TEST_RENDERING_PROXY, IntPoint(120, 200))
        assertEquals(110, child.observedRegionSizeX)
        assertEquals(10, child.actualSizeX)
    }

    @Test
    fun `子が参照する高さは、WRAPで選んだ実サイズではなく配分された領域なのだ`() {
        val view = YListView()
        view.sizingY = Sizing.FILL
        val child = RequirementView(requiresRegionSizeX = false, requiresRegionSizeY = true)
        view += Child(view.createDefaultPosition().also {
            it.weight = 1.0
        }, child)
        view += SpaceView(IntPoint(10, 20))
        view.calculateSize(TEST_RENDERING_PROXY, IntPoint(200, 120))
        assertEquals(100, child.observedRegionSizeY)
        assertEquals(20, child.actualSizeY)
    }

    @Test
    fun `WRAPの親を介して両成分が循環した場合は、クラス名を伴う例外が出るのだ`() {
        val view = StackView()
        view += RequirementView(requiresRegionSizeX = true, requiresRegionSizeY = true)
        val exception = assertThrows(IllegalStateException::class.java) {
            view.calculateSize(TEST_RENDERING_PROXY, IntPoint(100, 200))
        }
        assertTrue(exception.message!!.startsWith("Circular reference while calculating"))
        assertTrue(exception.message!!.contains(StackView::class.java.name))
    }

}
