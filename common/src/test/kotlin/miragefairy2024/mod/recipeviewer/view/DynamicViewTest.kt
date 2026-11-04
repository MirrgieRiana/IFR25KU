package miragefairy2024.mod.recipeviewer.view

import miragefairy2024.mod.recipeviewer.views.DynamicView
import miragefairy2024.mod.recipeviewer.views.FixedView
import miragefairy2024.mod.recipeviewer.views.FixedWidgetView
import miragefairy2024.mod.recipeviewer.views.StackView
import miragefairy2024.mod.recipeviewer.views.plusAssign
import miragefairy2024.util.ObservableValue
import miragefairy2024.util.register
import net.minecraft.network.chat.FormattedText
import net.minecraft.util.FormattedCharSequence
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

private val DYNAMIC_RENDERING_PROXY: RenderingProxy = object : RenderingProxy {
    override fun calculateTextWidth(text: FormattedText) = throw UnsupportedOperationException()
    override fun calculateTextWidth(text: FormattedCharSequence) = throw UnsupportedOperationException()
    override fun getTextHeight() = throw UnsupportedOperationException()
    override fun wrapText(text: FormattedText, maxWidth: Int): List<FormattedCharSequence> = throw UnsupportedOperationException()
}

private val DYNAMIC_VIEW_PLACER = ViewPlacer<PlaceableView> { _, _ -> throw UnsupportedOperationException() }

private class RecordingDynamicChildView(private val name: String, size: IntPoint, private val events: MutableList<String>) : FixedView(size) {
    val regions = mutableListOf<IntPoint>()
    val offsets = mutableListOf<IntPoint>()

    override fun initSizeCalculation(renderingProxy: RenderingProxy, regionSizeXProvider: () -> Int, regionSizeYProvider: () -> Int) {
        super.initSizeCalculation(renderingProxy, regionSizeXProvider, regionSizeYProvider)
        assertSame(DYNAMIC_RENDERING_PROXY, renderingProxy)
        events += "$name:init"
        regions += IntPoint(regionSizeXProvider(), regionSizeYProvider())
    }

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): ViewController {
        events += "$name:attach"
        offsets += offset
        return object : ViewController {
            override fun setOffset(offset: IntPoint) {
                offsets += offset
            }

            override fun detach() {
                events += "$name:detach"
            }

            override fun dispose() {
                events += "$name:dispose"
            }
        }
    }
}

class DynamicViewTest {

    @Test
    fun `サイズ計算では子を配置せず配置時点の内容を割り当て領域へ配置するのだ`() {
        listOf(true, false).forEach { xFirst ->
            val events = mutableListOf<String>()
            val first = RecordingDynamicChildView("first", IntPoint(10, 20), events)
            val second = RecordingDynamicChildView("second", IntPoint(30, 40), events)
            val content = ObservableValue<View>(first)
            val view = DynamicView(content)
            view.initSizeCalculation(DYNAMIC_RENDERING_PROXY, { 100 }, { 80 })

            assertEquals(IntPoint.ZERO, view.evaluateContentSize())
            if (xFirst) {
                view.evaluateActualSizeX()
                view.evaluateActualSizeY()
            } else {
                view.evaluateActualSizeY()
                view.evaluateActualSizeX()
            }
            assertEquals(IntPoint(100, 80), view.evaluateActualSize())
            content.value = second
            assertEquals(emptyList<String>(), events)

            val controller = view.attachTo(IntPoint(7, 11), DYNAMIC_VIEW_PLACER)

            assertEquals(listOf("second:init", "second:attach"), events)
            assertEquals(listOf(IntPoint(100, 80)), second.regions)
            assertEquals(listOf(IntPoint(7, 11)), second.offsets)
            controller.dispose()
            controller.detach()
        }
    }

    @Test
    fun `移動後の差し替えは旧配置を後始末して解除し同じ領域と新しい位置を使うのだ`() {
        val events = mutableListOf<String>()
        val first = RecordingDynamicChildView("first", IntPoint(10, 20), events)
        val second = RecordingDynamicChildView("second", IntPoint(30, 40), events)
        val content = ObservableValue<View>(first)
        val view = DynamicView(content)
        view.initSizeCalculation(DYNAMIC_RENDERING_PROXY, { 100 }, { 80 })
        val controller = view.attachTo(IntPoint(7, 11), DYNAMIC_VIEW_PLACER)

        controller.setOffset(IntPoint(20, 30))
        content.value = second
        controller.setOffset(IntPoint(-5, -9))
        content.value = first

        assertEquals(listOf("first:init", "first:attach", "first:dispose", "first:detach", "second:init", "second:attach", "second:dispose", "second:detach", "first:init", "first:attach"), events)
        assertEquals(listOf(IntPoint(7, 11), IntPoint(20, 30), IntPoint(-5, -9)), first.offsets)
        assertEquals(listOf(IntPoint(20, 30), IntPoint(-5, -9)), second.offsets)
        assertEquals(listOf(IntPoint(100, 80), IntPoint(100, 80)), first.regions)
        assertEquals(listOf(IntPoint(100, 80)), second.regions)
        assertEquals(IntPoint(100, 80), view.evaluateActualSize())
        controller.dispose()
        controller.detach()
    }

    @Test
    fun `差し替えたツリーの FILL と子の固定サイズを配置へ反映するのだ`() {
        fun contentView(size: IntPoint) = StackView().also {
            it.sizingX = Sizing.FILL
            it.sizingY = Sizing.FILL
            it += object : FixedWidgetView(size) { }
        }
        val first = contentView(IntPoint(10, 20))
        val second = contentView(IntPoint(30, 40))
        val content = ObservableValue<View>(first)
        val view = DynamicView(content)
        view.initSizeCalculation(DYNAMIC_RENDERING_PROXY, { 100 }, { 80 })
        val bounds = mutableListOf<IntRectangle>()
        val controller = view.attachTo(IntPoint(7, 11)) { _, rectangle ->
            bounds += rectangle
            ViewController.NONE
        }

        content.value = second

        assertEquals(IntPoint(100, 80), first.evaluateActualSize())
        assertEquals(IntPoint(100, 80), second.evaluateActualSize())
        assertEquals(listOf(IntRectangle(7, 11, 10, 20), IntRectangle(7, 11, 30, 40)), bounds)
        controller.dispose()
        controller.detach()
    }

    @Test
    fun `画面破棄では配置を解除せず監視を解除して再配置時には新しい領域を使うのだ`() {
        val events = mutableListOf<String>()
        val first = RecordingDynamicChildView("first", IntPoint(10, 20), events)
        val second = RecordingDynamicChildView("second", IntPoint(30, 40), events)
        val content = ObservableValue<View>(first)
        val view = DynamicView(content)
        view.initSizeCalculation(DYNAMIC_RENDERING_PROXY, { 100 }, { 80 })
        val firstController = view.attachTo(IntPoint(7, 11), DYNAMIC_VIEW_PLACER)

        firstController.dispose()
        content.value = second

        assertEquals(listOf("first:init", "first:attach", "first:dispose"), events)
        view.initSizeCalculation(DYNAMIC_RENDERING_PROXY, { 200 }, { 160 })
        val secondController = view.attachTo(IntPoint(20, 30), DYNAMIC_VIEW_PLACER)
        content.value = first

        assertEquals(listOf(IntPoint(200, 160)), second.regions)
        assertEquals(listOf(IntPoint(100, 80), IntPoint(200, 160)), first.regions)
        assertEquals(listOf(IntPoint(20, 30)), second.offsets)
        secondController.dispose()
        secondController.detach()
        val eventsAfterDisposal = events.toList()
        content.value = second
        assertEquals(eventsAfterDisposal, events)
    }

    @Test
    fun `変更通知の途中で破棄されても残りの通知から子を再配置しないのだ`() {
        val events = mutableListOf<String>()
        val first = RecordingDynamicChildView("first", IntPoint(10, 20), events)
        val second = RecordingDynamicChildView("second", IntPoint(30, 40), events)
        val content = ObservableValue<View>(first)
        lateinit var controller: ViewController
        val remover = content.register { _, _ ->
            controller.dispose()
        }
        val view = DynamicView(content)
        view.initSizeCalculation(DYNAMIC_RENDERING_PROXY, { 100 }, { 80 })
        controller = view.attachTo(IntPoint.ZERO, DYNAMIC_VIEW_PLACER)
        val notifiedValues = mutableListOf<View>()
        val notificationRemover = content.register { _, new ->
            notifiedValues += new
        }

        content.value = second

        assertEquals(listOf("first:init", "first:attach", "first:dispose"), events)
        assertEquals(emptyList<IntPoint>(), second.offsets)
        assertSame(second, content.value)
        assertEquals(listOf(second), notifiedValues)
        remover.remove()
        notificationRemover.remove()
        controller.detach()
    }

}
