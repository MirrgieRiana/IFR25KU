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
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

/** 文字の寸法を測らないViewだけを扱うため、どの関数も呼ばれないのだ～🌱 */
private val TEST_RENDERING_PROXY = object : RenderingProxy {
    override fun calculateTextWidth(text: FormattedText) = throw UnsupportedOperationException()
    override fun calculateTextWidth(text: FormattedCharSequence) = throw UnsupportedOperationException()
    override fun getTextHeight() = throw UnsupportedOperationException()
    override fun wrapText(text: FormattedText, maxWidth: Int): List<FormattedCharSequence> = throw UnsupportedOperationException()
}

class AxisWiseContentSizeTest {

    @Test
    fun `初期化はコンテナーとラッパーの子へ一度伝播し両軸の計算で再実行されないのだ`() {
        val events = mutableListOf<String>()
        val child = object : AbstractView() {
            override val sizingX = Sizing.WRAP
            override val sizingY = Sizing.WRAP
            override fun initChildrenSizeCalculation() {
                assertSame(TEST_RENDERING_PROXY, renderingProxy)
                events += "init"
            }

            override fun calculateContentSizeXImpl(): Int {
                assertSame(TEST_RENDERING_PROXY, renderingProxy)
                events += "x"
                return 10
            }

            override fun calculateContentSizeYImpl(): Int {
                assertSame(TEST_RENDERING_PROXY, renderingProxy)
                events += "y"
                return 20
            }

            override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = Remover.NONE
        }
        val wrapper = MarginView(1, 2, 3, 4)
        wrapper += child
        val view = YListView()
        view += wrapper
        view.initSizeCalculation(TEST_RENDERING_PROXY)
        assertEquals(listOf("init"), events)
        view.calculateContentSizeY()
        view.calculateContentSizeX()
        assertEquals(listOf("init", "y", "x"), events)
        assertEquals(IntPoint(13, 27), view.contentSize)
    }

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
    fun `X成分だけを計算してもラッパーの子のY成分は計算されないのだ`() {
        val child = SpaceView(IntPoint(10, 20))
        val view = MarginView(1, 2, 3, 4)
        view += child
        view.initSizeCalculation(TEST_RENDERING_PROXY)
        view.calculateContentSizeX()
        assertEquals(13, view.contentSizeX)
        assertEquals(10, child.contentSizeX)
        assertThrows(IllegalStateException::class.java) { view.contentSizeY }
        assertThrows(IllegalStateException::class.java) { child.contentSizeY }
    }

    @Test
    fun `Y成分を先に計算してもコンテナーの子のX成分は計算されないのだ`() {
        val child = SpaceView(IntPoint(10, 20))
        val view = YListView()
        view += child
        view.initSizeCalculation(TEST_RENDERING_PROXY)
        view.calculateContentSizeY()
        assertEquals(20, view.contentSizeY)
        assertEquals(20, child.contentSizeY)
        assertThrows(IllegalStateException::class.java) { view.contentSizeX }
        assertThrows(IllegalStateException::class.java) { child.contentSizeX }
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
