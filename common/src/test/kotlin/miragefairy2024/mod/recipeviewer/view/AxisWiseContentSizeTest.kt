package miragefairy2024.mod.recipeviewer.view

import miragefairy2024.mod.recipeviewer.views.AbstractView
import miragefairy2024.mod.recipeviewer.views.AbsoluteView
import miragefairy2024.mod.recipeviewer.views.Child
import miragefairy2024.mod.recipeviewer.views.ContainerView
import miragefairy2024.mod.recipeviewer.views.FilledRectangleView
import miragefairy2024.mod.recipeviewer.views.ListView
import miragefairy2024.mod.recipeviewer.views.MarginView
import miragefairy2024.mod.recipeviewer.views.MaxContentSizeXView
import miragefairy2024.mod.recipeviewer.views.MaxContentSizeYView
import miragefairy2024.mod.recipeviewer.views.MinContentSizeXView
import miragefairy2024.mod.recipeviewer.views.MinContentSizeYView
import miragefairy2024.mod.recipeviewer.views.PagingView
import miragefairy2024.mod.recipeviewer.views.SlotView
import miragefairy2024.mod.recipeviewer.views.SpaceView
import miragefairy2024.mod.recipeviewer.views.StackView
import miragefairy2024.mod.recipeviewer.views.TextView
import miragefairy2024.mod.recipeviewer.views.WrapperView
import miragefairy2024.mod.recipeviewer.views.XListView
import miragefairy2024.mod.recipeviewer.views.YListView
import miragefairy2024.util.Remover
import net.minecraft.network.chat.FormattedText
import net.minecraft.util.FormattedCharSequence
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

private val TEST_RENDERING_PROXY = object : RenderingProxy {
    override fun calculateTextWidth(text: FormattedText) = throw UnsupportedOperationException()
    override fun calculateTextWidth(text: FormattedCharSequence) = throw UnsupportedOperationException()
    override fun getTextHeight() = throw UnsupportedOperationException()
    override fun wrapText(text: FormattedText, maxWidth: Int): List<FormattedCharSequence> = throw UnsupportedOperationException()
}

private class RecordingContentSizeView(private val size: IntPoint, private val events: MutableList<String>) : AbstractView() {
    override val sizingX = Sizing.WRAP
    override val sizingY = Sizing.WRAP

    override fun getOwnContentSizeX(): Int {
        events += "contentX"
        return size.x
    }

    override fun getOwnContentSizeY(): Int {
        events += "contentY"
        return size.y
    }

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = Remover.NONE
}

class AxisWiseContentSizeTest {

    @Test
    fun `各軸の最小サイズと実寸は逆軸が未計算でも確定するのだ`() {
        listOf(true, false).forEach { xFirst ->
            val events = mutableListOf<String>()
            val view = RecordingContentSizeView(IntPoint(10, 20), events)
            view.initSizeCalculation(TEST_RENDERING_PROXY, { 100 }, { 200 })
            assertThrows(IllegalStateException::class.java) { view.getContentSizeX() }
            assertThrows(IllegalStateException::class.java) { view.getContentSizeY() }

            if (xFirst) {
                view.evaluateContentSizeX()
                assertEquals(listOf("contentX"), events)
                assertEquals(10, view.getContentSizeX())
                assertThrows(IllegalStateException::class.java) { view.getContentSizeY() }
                view.calculateActualSizeX()
                assertEquals(10, view.getActualSizeX())
                assertThrows(IllegalStateException::class.java) { view.getActualSizeY() }
                assertEquals(listOf("contentX"), events)
                view.evaluateContentSizeY()
                view.calculateActualSizeY()
            } else {
                view.evaluateContentSizeY()
                assertEquals(listOf("contentY"), events)
                assertEquals(20, view.getContentSizeY())
                assertThrows(IllegalStateException::class.java) { view.getContentSizeX() }
                view.calculateActualSizeY()
                assertEquals(20, view.getActualSizeY())
                assertThrows(IllegalStateException::class.java) { view.getActualSizeX() }
                assertEquals(listOf("contentY"), events)
                view.evaluateContentSizeX()
                view.calculateActualSizeX()
            }

            assertEquals(IntPoint(10, 20), view.contentSize)
            assertEquals(IntPoint(10, 20), view.actualSize)
            assertEquals(if (xFirst) listOf("contentX", "contentY") else listOf("contentY", "contentX"), events)
        }
    }

    @Test
    fun `コンテナーとマージンは子の同じ軸だけを計算してから最小サイズを集計するのだ`() {
        listOf(true, false).forEach { xFirst ->
            fun <P> check(view: ContainerView<P>, expectedSize: IntPoint) {
                val events = mutableListOf<String>()
                val childView = RecordingContentSizeView(IntPoint(10, 20), events)
                val marginView = MarginView(2, 3, 5, 7)
                marginView.add(Child(Unit, childView))
                val otherChildView = RecordingContentSizeView(IntPoint(30, 7), events)
                view.add(Child(view.createDefaultPosition(), marginView))
                view.add(Child(view.createDefaultPosition(), otherChildView))
                view.initSizeCalculation(TEST_RENDERING_PROXY, { 100 }, { 200 })

                if (xFirst) {
                    view.evaluateContentSizeX()
                    assertEquals(expectedSize.x, view.getContentSizeX())
                    assertEquals(15, marginView.getContentSizeX())
                    assertEquals(10, childView.getContentSizeX())
                    assertEquals(30, otherChildView.getContentSizeX())
                    listOf(view, marginView, childView, otherChildView).forEach {
                        assertThrows(IllegalStateException::class.java) { it.getContentSizeY() }
                    }
                    assertEquals(listOf("contentX", "contentX"), events)
                    view.calculateActualSizeX()
                    view.evaluateContentSizeY()
                    view.calculateActualSizeY()
                } else {
                    view.evaluateContentSizeY()
                    assertEquals(expectedSize.y, view.getContentSizeY())
                    assertEquals(32, marginView.getContentSizeY())
                    assertEquals(20, childView.getContentSizeY())
                    assertEquals(7, otherChildView.getContentSizeY())
                    listOf(view, marginView, childView, otherChildView).forEach {
                        assertThrows(IllegalStateException::class.java) { it.getContentSizeX() }
                    }
                    assertEquals(listOf("contentY", "contentY"), events)
                    view.calculateActualSizeY()
                    view.evaluateContentSizeX()
                    view.calculateActualSizeX()
                }

                assertEquals(expectedSize, view.contentSize)
                assertEquals(IntPoint(15, 32), marginView.contentSize)
                assertEquals(IntPoint(10, 20), childView.contentSize)
                assertEquals(IntPoint(30, 7), otherChildView.contentSize)
                assertEquals(if (xFirst) listOf("contentX", "contentX", "contentY", "contentY") else listOf("contentY", "contentY", "contentX", "contentX"), events)
            }

            check(StackView(), IntPoint(30, 32))
            check(XListView(), IntPoint(45, 32))
            check(YListView(), IntPoint(30, 39))
            check(AbsoluteView(IntPoint(101, 203)), IntPoint(101, 203))
        }
    }

    @Test
    fun `最小サイズの上限と下限は指定された軸だけに適用するのだ`() {
        listOf<Pair<() -> WrapperView, IntPoint>>(
            Pair({ MaxContentSizeXView(6) }, IntPoint(6, 20)),
            Pair({ MaxContentSizeXView(30) }, IntPoint(10, 20)),
            Pair({ MaxContentSizeYView(6) }, IntPoint(10, 6)),
            Pair({ MaxContentSizeYView(30) }, IntPoint(10, 20)),
            Pair({ MinContentSizeXView(6) }, IntPoint(10, 20)),
            Pair({ MinContentSizeXView(30) }, IntPoint(30, 20)),
            Pair({ MinContentSizeYView(6) }, IntPoint(10, 20)),
            Pair({ MinContentSizeYView(30) }, IntPoint(10, 30)),
        ).forEach { (createView, expectedSize) ->
            listOf(true, false).forEach { xFirst ->
                val events = mutableListOf<String>()
                val childView = RecordingContentSizeView(IntPoint(10, 20), events)
                val view = createView()
                view.add(Child(Unit, childView))
                view.initSizeCalculation(TEST_RENDERING_PROXY, { throw UnsupportedOperationException() }, { throw UnsupportedOperationException() })

                if (xFirst) {
                    view.evaluateContentSizeX()
                    assertEquals(expectedSize.x, view.getContentSizeX())
                    assertThrows(IllegalStateException::class.java) { view.getContentSizeY() }
                    assertThrows(IllegalStateException::class.java) { childView.getContentSizeY() }
                    assertEquals(listOf("contentX"), events)
                    view.evaluateContentSizeY()
                } else {
                    view.evaluateContentSizeY()
                    assertEquals(expectedSize.y, view.getContentSizeY())
                    assertThrows(IllegalStateException::class.java) { view.getContentSizeX() }
                    assertThrows(IllegalStateException::class.java) { childView.getContentSizeX() }
                    assertEquals(listOf("contentY"), events)
                    view.evaluateContentSizeX()
                }

                assertEquals(expectedSize, view.contentSize)
                assertEquals(IntPoint(10, 20), childView.contentSize)
                assertEquals(if (xFirst) listOf("contentX", "contentY") else listOf("contentY", "contentX"), events)
            }
        }
    }

    @Test
    fun `リストのレイアウト除外は各軸で独立して最小サイズの集計に適用するのだ`() {
        listOf(true, false).forEach { horizontal ->
            listOf(true, false).forEach { xFirst ->
                val events = mutableListOf<String>()
                val view = if (horizontal) XListView() else YListView()
                view.add(Child(ListView.Position().also { it.ignoreLayoutX = true }, RecordingContentSizeView(IntPoint(10, 20), events)))
                view.add(Child(ListView.Position().also { it.ignoreLayoutY = true }, RecordingContentSizeView(IntPoint(30, 7), events)))
                view.initSizeCalculation(TEST_RENDERING_PROXY, { throw UnsupportedOperationException() }, { throw UnsupportedOperationException() })

                if (xFirst) {
                    view.evaluateContentSizeX()
                    assertEquals(30, view.getContentSizeX())
                    assertThrows(IllegalStateException::class.java) { view.getContentSizeY() }
                    assertEquals(listOf("contentX", "contentX"), events)
                    view.evaluateContentSizeY()
                } else {
                    view.evaluateContentSizeY()
                    assertEquals(20, view.getContentSizeY())
                    assertThrows(IllegalStateException::class.java) { view.getContentSizeX() }
                    assertEquals(listOf("contentY", "contentY"), events)
                    view.evaluateContentSizeX()
                }

                assertEquals(IntPoint(30, 20), view.contentSize)
                assertEquals(if (xFirst) listOf("contentX", "contentX", "contentY", "contentY") else listOf("contentY", "contentY", "contentX", "contentX"), events)
            }
        }
    }

    @Test
    fun `空のコンテナーと固定サイズのViewはどちらの軸からでも計算できるのだ`() {
        listOf(true, false).forEach { xFirst ->
            listOf(
                Pair(StackView(), IntPoint.ZERO),
                Pair(XListView(), IntPoint.ZERO),
                Pair(YListView(), IntPoint.ZERO),
                Pair(SpaceView(IntPoint(10, 20)), IntPoint(10, 20)),
                Pair(FilledRectangleView(), IntPoint.ZERO),
                Pair(PagingView(), IntPoint.ZERO),
                Pair(object : SlotView() {}.also { it.margin = 3 }, IntPoint(22, 22)),
            ).forEach { (view, expectedSize) ->
                view.initSizeCalculation(TEST_RENDERING_PROXY, { throw UnsupportedOperationException() }, { throw UnsupportedOperationException() })

                if (xFirst) {
                    view.evaluateContentSizeX()
                    assertEquals(expectedSize.x, view.getContentSizeX())
                    assertThrows(IllegalStateException::class.java) { view.getContentSizeY() }
                    view.evaluateContentSizeY()
                } else {
                    view.evaluateContentSizeY()
                    assertEquals(expectedSize.y, view.getContentSizeY())
                    assertThrows(IllegalStateException::class.java) { view.getContentSizeX() }
                    view.evaluateContentSizeX()
                }

                assertEquals(expectedSize, view.contentSize)
            }
        }
    }

    @Test
    fun `テキストは要求された軸の寸法だけをRenderingProxyから取得するのだ`() {
        listOf(true, false).forEach { xFirst ->
            val events = mutableListOf<String>()
            val renderingProxy = object : RenderingProxy {
                override fun calculateTextWidth(text: FormattedText) = throw UnsupportedOperationException()
                override fun calculateTextWidth(text: FormattedCharSequence): Int {
                    events += "width"
                    return 37
                }
                override fun getTextHeight(): Int {
                    events += "height"
                    return 9
                }
                override fun wrapText(text: FormattedText, maxWidth: Int): List<FormattedCharSequence> = throw UnsupportedOperationException()
            }
            val view = TextView()
            view.initSizeCalculation(renderingProxy, { throw UnsupportedOperationException() }, { throw UnsupportedOperationException() })
            assertEquals(emptyList<String>(), events)

            if (xFirst) {
                view.evaluateContentSizeX()
                assertEquals(37, view.getContentSizeX())
                assertThrows(IllegalStateException::class.java) { view.getContentSizeY() }
                assertEquals(listOf("width"), events)
                view.evaluateContentSizeY()
            } else {
                view.evaluateContentSizeY()
                assertEquals(9, view.getContentSizeY())
                assertThrows(IllegalStateException::class.java) { view.getContentSizeX() }
                assertEquals(listOf("height"), events)
                view.evaluateContentSizeX()
            }

            assertEquals(IntPoint(37, 9), view.contentSize)
            assertEquals(if (xFirst) listOf("width", "height") else listOf("height", "width"), events)
        }
    }

}
