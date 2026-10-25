package miragefairy2024.mod.recipeviewer.view

import miragefairy2024.mod.recipeviewer.views.Child
import miragefairy2024.mod.recipeviewer.views.FixedView
import miragefairy2024.mod.recipeviewer.views.PagingView
import miragefairy2024.util.register
import net.minecraft.network.chat.FormattedText
import net.minecraft.util.FormattedCharSequence
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

private val TEST_RENDERING_PROXY = object : RenderingProxy {
    override fun calculateTextWidth(text: FormattedText) = throw UnsupportedOperationException()
    override fun calculateTextWidth(text: FormattedCharSequence) = throw UnsupportedOperationException()
    override fun getTextHeight() = throw UnsupportedOperationException()
    override fun wrapText(text: FormattedText, maxWidth: Int): List<FormattedCharSequence> = throw UnsupportedOperationException()
}

private val TEST_VIEW_PLACER = ViewPlacer<PlaceableView> { _, _ -> throw UnsupportedOperationException() }

private class RecordingPagingChildView(size: IntPoint, private val events: MutableList<String>) : FixedView(size) {
    val offsets = mutableListOf<IntPoint>()

    var initializationCount = 0
        private set

    override fun initSizeCalculation(renderingProxy: RenderingProxy, regionSizeXProvider: () -> Int, regionSizeYProvider: () -> Int) {
        super.initSizeCalculation(renderingProxy, regionSizeXProvider, regionSizeYProvider)
        initializationCount += 1
    }

    override fun evaluateContentSizeXImpl(): Int {
        assertSame(TEST_RENDERING_PROXY, renderingProxy)
        events += "contentX"
        return super.evaluateContentSizeXImpl()
    }

    override fun evaluateContentSizeYImpl(): Int {
        assertSame(TEST_RENDERING_PROXY, renderingProxy)
        events += "contentY"
        return super.evaluateContentSizeYImpl()
    }

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): ViewController {
        events += "attach"
        offsets += offset
        return object : ViewController {
            override fun setOffset(offset: IntPoint) {
                offsets += offset
            }

            override fun remove() {
                events += "remove"
            }

            override fun dispose() {
                events += "dispose"
            }
        }
    }
}

class PagingViewTest {

    @Test
    fun `画面破棄では子の配置を解除せず後始末しページ変更の登録を解除するのだ`() {
        val events = mutableListOf<String>()
        val firstChild = RecordingPagingChildView(IntPoint(10, 6), events)
        val secondChild = RecordingPagingChildView(IntPoint(10, 6), events)
        val view = PagingView()
        view.add(ChildrenGenerator { _, _ -> listOf(Child(Alignment.START, firstChild), Child(Alignment.END, secondChild)) })
        view.initSizeCalculation(TEST_RENDERING_PROXY, { 100 }, { 10 })
        val controller = view.attachTo(IntPoint(7, 11), TEST_VIEW_PLACER)

        controller.dispose()

        val eventsAfterDisposal = events.toList()
        assertEquals(listOf("dispose"), eventsAfterDisposal.filter { it == "remove" || it == "dispose" })
        view.pageIndex.value = 1
        assertEquals(eventsAfterDisposal, events)
        assertEquals(emptyList<IntPoint>(), secondChild.offsets)
    }

    @Test
    fun `移動後のページ切り替えは新しい位置へ配置するのだ`() {
        val events = mutableListOf<String>()
        val firstChild = RecordingPagingChildView(IntPoint(10, 6), events)
        val secondChild = RecordingPagingChildView(IntPoint(10, 6), events)
        val view = PagingView()
        view.add(ChildrenGenerator { _, _ -> listOf(Child(Alignment.START, firstChild), Child(Alignment.END, secondChild)) })
        view.initSizeCalculation(TEST_RENDERING_PROXY, { 100 }, { 10 })
        val controller = view.attachTo(IntPoint(7, 11), TEST_VIEW_PLACER)

        controller.setOffset(IntPoint(20, 30))
        view.pageIndex.value = 1
        controller.setOffset(IntPoint(-5, -9))

        assertEquals(listOf(IntPoint(7, 11), IntPoint(20, 30)), firstChild.offsets)
        assertEquals(listOf(IntPoint(110, 30), IntPoint(85, -9)), secondChild.offsets)
        assertEquals(1, firstChild.initializationCount)
        assertEquals(1, secondChild.initializationCount)
        controller.remove()
    }

    @Test
    fun `子の生成とページ分配は軸の計算順に依存せず配置時だけ実行するのだ`() {
        listOf(true, false).forEach { xFirst ->
            val events = mutableListOf<String>()
            val firstChild = RecordingPagingChildView(IntPoint(10, 6), events)
            val secondChild = RecordingPagingChildView(IntPoint(10, 6), events)
            val view = PagingView()
            val pageCounts = mutableListOf<Int>()
            view.pageCount.register { _, new ->
                pageCounts += new
            }
            view.add(ChildrenGenerator { renderingProxy, regionSize ->
                events += "generate"
                assertSame(TEST_RENDERING_PROXY, renderingProxy)
                assertEquals(IntPoint(100, 10), regionSize)
                listOf(Child(Alignment.START, firstChild), Child(Alignment.END, secondChild))
            })
            view.initSizeCalculation(TEST_RENDERING_PROXY, { 100 }, { 10 })
            view.evaluateContentSizeX()
            view.evaluateContentSizeY()

            if (xFirst) {
                view.evaluateActualSizeX()
                view.evaluateActualSizeY()
            } else {
                view.evaluateActualSizeY()
                view.evaluateActualSizeX()
            }
            assertEquals(IntPoint(100, 10), view.evaluateActualSize())
            assertEquals(emptyList<String>(), events)
            assertEquals(0, view.pageCount.value)
            assertEquals(emptyList<Int>(), pageCounts)

            val remover = view.attachTo(IntPoint(7, 11), TEST_VIEW_PLACER)

            assertEquals(listOf("generate", "contentY", "contentY", "attach"), events)
            assertEquals(2, view.pageCount.value)
            assertEquals(listOf(2), pageCounts)
            assertEquals(6, firstChild.evaluateActualSizeY())
            assertEquals(6, secondChild.evaluateActualSizeY())
            assertEquals(1, firstChild.initializationCount)
            assertEquals(1, secondChild.initializationCount)
            assertEquals(listOf(IntPoint(7, 11)), firstChild.offsets)
            assertEquals(emptyList<IntPoint>(), secondChild.offsets)

            view.pageIndex.value = 1

            assertEquals(listOf("generate", "contentY", "contentY", "attach", "remove", "contentX", "attach"), events)
            assertEquals(listOf(IntPoint(97, 11)), secondChild.offsets)
            assertEquals(1, firstChild.initializationCount)
            assertEquals(1, secondChild.initializationCount)

            remover.remove()

            val eventsAfterRemoval = events.toList()
            assertEquals(listOf("generate", "contentY", "contentY", "attach", "remove", "contentX", "attach", "remove"), eventsAfterRemoval)
            view.pageIndex.value = 0
            assertEquals(eventsAfterRemoval, events)
        }
    }

    @Test
    fun `子がない場合も配置時に空のページを作るのだ`() {
        val view = PagingView()
        view.initSizeCalculation(TEST_RENDERING_PROXY, { 100 }, { 10 })
        view.evaluateContentSizeX()
        view.evaluateContentSizeY()
        view.evaluateActualSizeX()
        view.evaluateActualSizeY()
        assertEquals(0, view.pageCount.value)

        val remover = view.attachTo(IntPoint.ZERO, TEST_VIEW_PLACER)

        assertEquals(1, view.pageCount.value)
        remover.remove()
    }

}
