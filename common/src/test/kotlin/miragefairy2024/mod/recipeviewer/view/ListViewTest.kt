package miragefairy2024.mod.recipeviewer.view

import miragefairy2024.mod.recipeviewer.views.Child
import miragefairy2024.mod.recipeviewer.views.FilledRectangleView
import miragefairy2024.mod.recipeviewer.views.FixedWidgetView
import miragefairy2024.mod.recipeviewer.views.ListView
import miragefairy2024.mod.recipeviewer.views.XListView
import miragefairy2024.mod.recipeviewer.views.YListView
import net.minecraft.network.chat.FormattedText
import net.minecraft.util.FormattedCharSequence
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

private val LIST_VIEW_RENDERING_PROXY: RenderingProxy = object : RenderingProxy {
    override fun calculateTextWidth(text: FormattedText) = throw UnsupportedOperationException()
    override fun calculateTextWidth(text: FormattedCharSequence) = throw UnsupportedOperationException()
    override fun getTextHeight() = throw UnsupportedOperationException()
    override fun wrapText(text: FormattedText, maxWidth: Int): List<FormattedCharSequence> = throw UnsupportedOperationException()
}

class ListViewTest {

    @Test
    fun `重み付きの子が寸法ゼロを下回らない配分では縮小と累積丸めを保つのだ`() {
        listOf(true, false).forEach { horizontal ->
            listOf(Pair(30, listOf(5, 25)), Pair(31, listOf(6, 25)), Pair(41, listOf(11, 30))).forEach { (regionLength, expectedLengths) ->
                val view = if (horizontal) XListView() else YListView()
                view.sizingX = Sizing.FILL
                view.sizingY = Sizing.FILL
                val childViews = listOf(10, 30).map { length ->
                    val childView: FixedWidgetView = object : FixedWidgetView(if (horizontal) IntPoint(length, 7) else IntPoint(7, length)) {
                        override val sizingX = Sizing.FILL
                        override val sizingY = Sizing.FILL
                    }
                    childView
                }
                childViews.forEach {
                    view.add(Child(ListView.Position().also { it.weight = 1.0 }, it))
                }
                view.initSizeCalculation(LIST_VIEW_RENDERING_PROXY, { if (horizontal) regionLength else 7 }, { if (horizontal) 7 else regionLength })

                assertEquals(expectedLengths.map { if (horizontal) IntPoint(it, 7) else IntPoint(7, it) }, childViews.map { it.evaluateActualSize() })
            }
        }
    }

    @Test
    fun `固定部分が領域を埋めると重み付きの子を寸法ゼロにし後続の配置を戻さないのだ`() {
        listOf(true, false).forEach { horizontal ->
            listOf(0, 10, 25).forEach { regionLength ->
                val view = if (horizontal) XListView() else YListView()
                view.sizingX = Sizing.FILL
                view.sizingY = Sizing.FILL
                val firstChild: FixedWidgetView = object : FixedWidgetView(if (horizontal) IntPoint(20, 7) else IntPoint(7, 20)) {}
                val weightedChild = FilledRectangleView()
                val lastChild: FixedWidgetView = object : FixedWidgetView(IntPoint(5, 5)) {}
                view.add(Child(ListView.Position(), firstChild))
                view.add(Child(ListView.Position().also { it.weight = 1.0 }, weightedChild))
                view.add(Child(ListView.Position(), lastChild))
                view.initSizeCalculation(LIST_VIEW_RENDERING_PROXY, { if (horizontal) regionLength else 7 }, { if (horizontal) 7 else regionLength })

                assertEquals(if (horizontal) IntPoint(0, 7) else IntPoint(7, 0), weightedChild.evaluateActualSize())
                val bounds = mutableListOf<IntRectangle>()
                val controller = view.attachTo(IntPoint(7, 11)) { _, rectangle ->
                    bounds += rectangle
                    ViewController.NONE
                }
                assertEquals(
                    if (horizontal) listOf(IntRectangle(7, 11, 20, 7), IntRectangle(27, 11, 0, 7), IntRectangle(27, 11, 5, 5)) else listOf(IntRectangle(7, 11, 7, 20), IntRectangle(7, 31, 7, 0), IntRectangle(7, 31, 5, 5)),
                    bounds,
                )
                controller.dispose()
                controller.detach()
            }
        }
    }

}
