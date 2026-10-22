package miragefairy2024.mod.recipeviewer.view

import miragefairy2024.mod.recipeviewer.views.Child
import miragefairy2024.mod.recipeviewer.views.FixedView
import miragefairy2024.mod.recipeviewer.views.PagingView
import miragefairy2024.util.Remover
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

    override fun initSizeCalculation(renderingProxy: RenderingProxy) {
        super.initSizeCalculation(renderingProxy)
        initializationCount += 1
    }

    override fun calculateContentSizeXImpl(): Int {
        assertSame(TEST_RENDERING_PROXY, renderingProxy)
        events += "contentX"
        return super.calculateContentSizeXImpl()
    }

    override fun calculateContentSizeYImpl(): Int {
        assertSame(TEST_RENDERING_PROXY, renderingProxy)
        events += "contentY"
        return super.calculateContentSizeYImpl()
    }

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): Remover {
        events += "attach"
        offsets += offset
        return Remover {
            events += "remove"
        }
    }
}

class PagingViewTest {

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
            view.initSizeCalculation(TEST_RENDERING_PROXY)
            view.calculateContentSizeX()
            view.calculateContentSizeY()

            if (xFirst) {
                view.calculateActualSizeX(100)
                view.calculateActualSizeY(10)
            } else {
                view.calculateActualSizeY(10)
                view.calculateActualSizeX(100)
            }
            assertEquals(IntPoint(100, 10), view.actualSize)
            assertEquals(emptyList<String>(), events)
            assertEquals(0, view.pageCount.value)
            assertEquals(emptyList<Int>(), pageCounts)

            val remover = view.attachTo(IntPoint(7, 11), TEST_VIEW_PLACER)

            assertEquals(listOf("generate", "contentX", "contentX", "contentY", "contentY", "attach"), events)
            assertEquals(2, view.pageCount.value)
            assertEquals(listOf(2), pageCounts)
            assertEquals(IntPoint(10, 6), firstChild.actualSize)
            assertEquals(IntPoint(10, 6), secondChild.actualSize)
            assertEquals(1, firstChild.initializationCount)
            assertEquals(1, secondChild.initializationCount)
            assertEquals(listOf(IntPoint(7, 11)), firstChild.offsets)
            assertEquals(emptyList<IntPoint>(), secondChild.offsets)

            view.pageIndex.value = 1

            assertEquals(listOf("generate", "contentX", "contentX", "contentY", "contentY", "attach", "remove", "attach"), events)
            assertEquals(listOf(IntPoint(97, 11)), secondChild.offsets)
            assertEquals(1, firstChild.initializationCount)
            assertEquals(1, secondChild.initializationCount)

            remover.remove()

            val eventsAfterRemoval = events.toList()
            assertEquals(listOf("generate", "contentX", "contentX", "contentY", "contentY", "attach", "remove", "attach", "remove"), eventsAfterRemoval)
            view.pageIndex.value = 0
            assertEquals(eventsAfterRemoval, events)
        }
    }

    @Test
    fun `子がない場合も配置時に空のページを作るのだ`() {
        val view = PagingView()
        view.initSizeCalculation(TEST_RENDERING_PROXY)
        view.calculateContentSizeX()
        view.calculateContentSizeY()
        view.calculateActualSizeX(100)
        view.calculateActualSizeY(10)
        assertEquals(0, view.pageCount.value)

        val remover = view.attachTo(IntPoint.ZERO, TEST_VIEW_PLACER)

        assertEquals(1, view.pageCount.value)
        remover.remove()
    }

}
