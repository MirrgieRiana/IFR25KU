package miragefairy2024.mod.recipeviewer.view

import miragefairy2024.mod.recipeviewer.views.AbstractView
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
    override val sizingX = Sizing.WRAP
    override val sizingY = Sizing.WRAP
    override fun calculateContentSizeX(): Int {
        if (requiresRegionSizeY) requireRegionSizeY()
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
    fun `Y成分の計算では、確定済みのX成分を参照できるのだ`() {
        val view = RequirementView(requiresRegionSizeX = true, requiresRegionSizeY = false)
        view.calculateSize(TEST_RENDERING_PROXY, IntPoint(100, 100))
        assertEquals(10, view.observedRegionSizeX)
        assertEquals(20, view.contentSizeY)
    }

    @Test
    fun `X成分の計算でY方向を要求すると、根のViewでは例外が出るのだ`() {
        val view = RequirementView(requiresRegionSizeX = false, requiresRegionSizeY = true)
        val exception = assertThrows(IllegalStateException::class.java) {
            view.calculateSize(TEST_RENDERING_PROXY, IntPoint(100, 100))
        }
        assertEquals("The root view cannot refer to the available size of the Y axis.", exception.message)
    }

    @Test
    fun `計算より前にX成分を読むと、Viewのクラス名を伴う例外が出るのだ`() {
        val view = SpaceView(IntPoint(10, 20))
        val exception = assertThrows(IllegalStateException::class.java) {
            view.contentSizeX
        }
        assertEquals("The content size of the X axis is not calculated yet: ${SpaceView::class.java.name}", exception.message)
    }

}
