package miragefairy2024.mod.recipeviewer.view

import miragefairy2024.mod.recipeviewer.views.FixedWidgetView
import miragefairy2024.mod.recipeviewer.views.MarginView
import miragefairy2024.mod.recipeviewer.views.TooltipView
import miragefairy2024.mod.recipeviewer.views.XListView
import miragefairy2024.mod.recipeviewer.views.plusAssign
import net.minecraft.network.chat.FormattedText
import net.minecraft.util.FormattedCharSequence
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

private val CONTROLLER_RENDERING_PROXY: RenderingProxy = object : RenderingProxy {
    override fun calculateTextWidth(text: FormattedText) = throw UnsupportedOperationException()
    override fun calculateTextWidth(text: FormattedCharSequence) = throw UnsupportedOperationException()
    override fun getTextHeight() = throw UnsupportedOperationException()
    override fun wrapText(text: FormattedText, maxWidth: Int): List<FormattedCharSequence> = throw UnsupportedOperationException()
}

private class RecordingPlacement(initialBounds: IntRectangle) {
    var bounds = initialBounds
    var removals = 0
    var disposals = 0
    val controller: ViewController = object : ViewController {
        override fun setOffset(offset: IntPoint) {
            bounds = offset.sized(bounds.size)
        }
        override fun remove() {
            removals += 1
        }
        override fun dispose() {
            disposals += 1
        }
    }
}

class ViewControllerTest {
    @Test
    fun `通知では登録を維持し明示的に消した登録には通知しないのだ`() {
        val placement = RecordingPlacement(IntRectangle.ZERO)
        val controllerList = ViewControllerList()
        controllerList += placement.controller

        controllerList.dispose()
        controllerList.remove()

        assertEquals(1, placement.disposals)
        assertEquals(1, placement.removals)

        controllerList.clear()
        controllerList.dispose()
        controllerList.remove()

        assertEquals(1, placement.disposals)
        assertEquals(1, placement.removals)
    }

    @Test
    fun `画面破棄の後始末は親とマージンと装飾を経由して配置を解除せず伝播するのだ`() {
        val child = TooltipView().also { it += object : FixedWidgetView(IntPoint(10, 20)) { } }
        val view = XListView().also {
            it += MarginView(2, 3, 4, 5).also { it += child }
        }
        view.initSizeCalculation(CONTROLLER_RENDERING_PROXY, { 100 }, { 100 })
        val placements = mutableListOf<RecordingPlacement>()
        val controller = view.attachTo(IntPoint(3, 4)) { _, bounds ->
            val placement = RecordingPlacement(bounds)
            placements += placement
            placement.controller
        }

        controller.dispose()

        assertEquals(listOf(0, 0), placements.map { it.removals })
        assertEquals(listOf(1, 1), placements.map { it.disposals })
    }

    @Test
    fun `親の移動は子の相対位置と寸法と登録を維持するのだ`() {
        val first: FixedWidgetView = object : FixedWidgetView(IntPoint(10, 20)) { }
        val second: FixedWidgetView = object : FixedWidgetView(IntPoint(20, 10)) { }
        val margin = MarginView(2, 3, 4, 5).also { it += first }
        val view = XListView().also {
            it += margin
            it += second
        }
        view.initSizeCalculation(CONTROLLER_RENDERING_PROXY, { 100 }, { 100 })
        val placements = mutableListOf<RecordingPlacement>()
        val controller = view.attachTo(IntPoint(100, 200)) { _, bounds ->
            val placement = RecordingPlacement(bounds)
            placements += placement
            placement.controller
        }
        assertEquals(listOf(IntRectangle(102, 204, 10, 20), IntRectangle(115, 200, 20, 10)), placements.map { it.bounds })

        controller.setOffset(IntPoint(90, 170))
        controller.setOffset(IntPoint(-10, -20))

        assertEquals(listOf(IntRectangle(-8, -16, 10, 20), IntRectangle(5, -20, 20, 10)), placements.map { it.bounds })
        assertEquals(listOf(0, 0), placements.map { it.removals })
        controller.remove()
        assertEquals(listOf(1, 1), placements.map { it.removals })
    }

    @Test
    fun `装飾する View 自身と子の描画領域を一緒に移動するのだ`() {
        val view = TooltipView().also { it += object : FixedWidgetView(IntPoint(10, 20)) { } }
        view.initSizeCalculation(CONTROLLER_RENDERING_PROXY, { 100 }, { 100 })
        val placements = mutableListOf<RecordingPlacement>()
        val controller = view.attachTo(IntPoint(3, 4)) { _, bounds ->
            val placement = RecordingPlacement(bounds)
            placements += placement
            placement.controller
        }

        controller.setOffset(IntPoint(30, 40))

        assertEquals(listOf(IntRectangle(30, 40, 10, 20), IntRectangle(30, 40, 10, 20)), placements.map { it.bounds })
        controller.remove()
        assertEquals(listOf(1, 1), placements.map { it.removals })
    }
}
