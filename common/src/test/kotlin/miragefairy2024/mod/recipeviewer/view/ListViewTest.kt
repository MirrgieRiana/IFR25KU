package miragefairy2024.mod.recipeviewer.view

import miragefairy2024.mod.recipeviewer.views.Child
import miragefairy2024.mod.recipeviewer.views.FixedWidgetView
import miragefairy2024.mod.recipeviewer.views.ListView
import miragefairy2024.mod.recipeviewer.views.MarginView
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
    fun `不足する領域では予約寸法を維持し余裕があれば重みに応じて配るのだ`() {
        listOf(true, false).forEach { horizontal ->
            listOf(10, 30, 40, 41).forEach { regionLength ->
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

                val firstLength = if (regionLength == 41) 11 else 10
                val expectedBounds = if (horizontal) listOf(IntRectangle(0, 0, firstLength, 7), IntRectangle(firstLength, 0, 30, 7)) else listOf(IntRectangle(0, 0, 7, firstLength), IntRectangle(0, firstLength, 7, 30))
                val bounds = mutableListOf<IntRectangle>()
                val controller = view.attachTo(IntPoint.ZERO) { _, rectangle ->
                    bounds += rectangle
                    ViewController.NONE
                }
                assertEquals(expectedBounds, bounds)
                controller.dispose()
                controller.detach()
            }
        }
    }

    @Test
    fun `負の余白で予約寸法が0になると次の子は原点に配置されるのだ`() {
        listOf(true, false).forEach { horizontal ->
            val view = if (horizontal) XListView() else YListView()
            val marginView = MarginView(-6, -6, -6, -6)
            marginView.add(Child(Unit, object : FixedWidgetView(IntPoint(10, 10)) {}))
            view.add(Child(view.createDefaultPosition(), marginView))
            view.add(Child(view.createDefaultPosition(), object : FixedWidgetView(IntPoint(30, 7)) {}))
            view.initSizeCalculation(LIST_VIEW_RENDERING_PROXY, { 100 }, { 100 })

            assertEquals(IntPoint.ZERO, marginView.evaluateContentSize())
            assertEquals(IntPoint(30, 7), view.evaluateContentSize())

            val bounds = mutableListOf<IntRectangle>()
            val controller = view.attachTo(IntPoint.ZERO) { _, rectangle ->
                bounds += rectangle
                ViewController.NONE
            }
            assertEquals(listOf(IntRectangle(-6, -6, 10, 10), IntRectangle(0, 0, 30, 7)), bounds)
            controller.dispose()
            controller.detach()
        }
    }

}
