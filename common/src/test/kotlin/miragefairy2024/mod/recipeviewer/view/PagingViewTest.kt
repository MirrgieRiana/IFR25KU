package miragefairy2024.mod.recipeviewer.view

import miragefairy2024.mod.recipeviewer.views.Child
import miragefairy2024.mod.recipeviewer.views.FixedView
import miragefairy2024.mod.recipeviewer.views.FixedWidgetView
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

            override fun detach() {
                events += "detach"
            }

            override fun dispose() {
                events += "dispose"
            }
        }
    }
}

private class RecordingPagingViewPlacer : ViewPlacer<PlaceableView> {
    val placed = mutableSetOf<PlaceableView>()
    val attached = mutableListOf<PlaceableView>()
    val disposed = mutableListOf<PlaceableView>()
    var onAttach: (PlaceableView) -> Unit = { }
    var onDispose: (PlaceableView) -> Unit = { }

    override fun place(view: PlaceableView, bounds: IntRectangle): ViewController {
        placed += view
        attached += view
        onAttach(view)
        return object : ViewController {
            override fun setOffset(offset: IntPoint) = Unit

            override fun detach() {
                placed -= view
            }

            override fun dispose() {
                disposed += view
                onDispose(view)
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
        assertEquals(listOf("dispose"), eventsAfterDisposal.filter { it == "detach" || it == "dispose" })
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
        controller.dispose()
        controller.detach()
        assertEquals(listOf("dispose", "detach", "dispose", "detach"), events.filter { it == "dispose" || it == "detach" })
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

            assertEquals(listOf("generate", "contentY", "contentY", "attach", "dispose", "detach", "contentX", "attach"), events)
            assertEquals(listOf(IntPoint(97, 11)), secondChild.offsets)
            assertEquals(1, firstChild.initializationCount)
            assertEquals(1, secondChild.initializationCount)

            remover.dispose()
            remover.detach()

            val eventsAfterRemoval = events.toList()
            assertEquals(listOf("generate", "contentY", "contentY", "attach", "dispose", "detach", "contentX", "attach", "dispose", "detach"), eventsAfterRemoval)
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
        remover.dispose()
        remover.detach()
    }

    @Test
    fun `変更通知の途中で破棄されても残りの通知からページを再配置しないのだ`() {
        val events = mutableListOf<String>()
        val firstChild = RecordingPagingChildView(IntPoint(10, 6), events)
        val secondChild = RecordingPagingChildView(IntPoint(10, 6), events)
        val view = PagingView()
        view.add(ChildrenGenerator { _, _ -> listOf(Child(Alignment.START, firstChild), Child(Alignment.END, secondChild)) })
        view.initSizeCalculation(TEST_RENDERING_PROXY, { 100 }, { 10 })
        lateinit var controller: ViewController
        val remover = view.pageIndex.register { _, _ ->
            controller.dispose()
        }
        controller = view.attachTo(IntPoint.ZERO, TEST_VIEW_PLACER)
        val notifiedIndexes = mutableListOf<Int>()
        val notificationRemover = view.pageIndex.register { _, new ->
            notifiedIndexes += new
        }
        val eventsAfterAttach = events.toList()

        view.pageIndex.value = 1

        assertEquals(eventsAfterAttach + "dispose", events)
        assertEquals(emptyList<IntPoint>(), secondChild.offsets)
        assertEquals(1, view.pageIndex.value)
        assertEquals(listOf(1), notifiedIndexes)
        remover.remove()
        notificationRemover.remove()
        controller.detach()
    }

    @Test
    fun `ページ配置中の変更は直列処理して最新のページだけを残すのだ`() {
        listOf(true, false).forEach { initiallyChanging ->
            val first = object : FixedWidgetView(IntPoint(10, 6)) { }
            val second = object : FixedWidgetView(IntPoint(20, 6)) { }
            val intermediate = object : FixedWidgetView(IntPoint(30, 6)) { }
            val latest = object : FixedWidgetView(IntPoint(40, 6)) { }
            val view = PagingView()
            view.add(ChildrenGenerator { _, _ -> listOf(first, second, intermediate, latest).map { Child(Alignment.START, it) } })
            view.initSizeCalculation(TEST_RENDERING_PROXY, { 100 }, { 10 })
            val placer = RecordingPagingViewPlacer()
            val changingChild = if (initiallyChanging) first else second
            placer.onAttach = { child ->
                if (child === changingChild) {
                    view.pageIndex.value = 2
                    view.pageIndex.value = 3
                }
            }
            val controller = view.attachTo(IntPoint(7, 11), placer)

            if (!initiallyChanging) view.pageIndex.value = 1

            val oldChildren = if (initiallyChanging) listOf(first) else listOf(first, second)
            assertEquals(3, view.pageIndex.value)
            assertEquals(setOf(latest), placer.placed)
            assertEquals(oldChildren + latest, placer.attached)
            assertEquals(oldChildren, placer.disposed)

            controller.dispose()
            controller.detach()

            assertEquals(emptySet<PlaceableView>(), placer.placed)
            assertEquals(oldChildren + latest, placer.disposed)
        }
    }

    @Test
    fun `ページ配置中に破棄された場合は戻ってきた配置も後始末するのだ`() {
        val first = object : FixedWidgetView(IntPoint(10, 6)) { }
        val second = object : FixedWidgetView(IntPoint(20, 6)) { }
        val view = PagingView()
        view.add(ChildrenGenerator { _, _ -> listOf(Child(Alignment.START, first), Child(Alignment.START, second)) })
        view.initSizeCalculation(TEST_RENDERING_PROXY, { 100 }, { 10 })
        val placer = RecordingPagingViewPlacer()
        val controller = view.attachTo(IntPoint.ZERO, placer)
        placer.onAttach = { child ->
            if (child === second) controller.dispose()
        }

        view.pageIndex.value = 1

        assertEquals(emptySet<PlaceableView>(), placer.placed)
        assertEquals(listOf(first, second), placer.disposed)
        controller.detach()
    }

    @Test
    fun `子の後始末中に破棄された場合は同じ子を再度破棄せず次のページも配置しないのだ`() {
        val first = object : FixedWidgetView(IntPoint(10, 6)) { }
        val second = object : FixedWidgetView(IntPoint(20, 6)) { }
        val view = PagingView()
        view.add(ChildrenGenerator { _, _ -> listOf(Child(Alignment.START, first), Child(Alignment.START, second)) })
        view.initSizeCalculation(TEST_RENDERING_PROXY, { 100 }, { 10 })
        val placer = RecordingPagingViewPlacer()
        val controller = view.attachTo(IntPoint.ZERO, placer)
        var disposalRequested = false
        placer.onDispose = { child ->
            if (child === first && !disposalRequested) {
                disposalRequested = true
                controller.dispose()
            }
        }

        view.pageIndex.value = 1

        assertEquals(emptySet<PlaceableView>(), placer.placed)
        assertEquals(listOf(first), placer.attached)
        assertEquals(listOf(first), placer.disposed)
        controller.detach()
    }

}
