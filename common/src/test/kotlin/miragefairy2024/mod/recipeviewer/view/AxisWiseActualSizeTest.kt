package miragefairy2024.mod.recipeviewer.view

import miragefairy2024.mod.recipeviewer.views.AbstractView
import miragefairy2024.mod.recipeviewer.views.Child
import miragefairy2024.mod.recipeviewer.views.PagingView
import miragefairy2024.util.Remover
import net.minecraft.network.chat.FormattedText
import net.minecraft.util.FormattedCharSequence
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

private val TEST_RENDERING_PROXY: RenderingProxy = object : RenderingProxy {
    override fun calculateTextWidth(text: FormattedText) = throw UnsupportedOperationException()
    override fun calculateTextWidth(text: FormattedCharSequence) = throw UnsupportedOperationException()
    override fun getTextHeight() = throw UnsupportedOperationException()
    override fun wrapText(text: FormattedText, maxWidth: Int): List<FormattedCharSequence> = throw UnsupportedOperationException()
}

private class RecordingSizeView(private val events: MutableList<String>) : AbstractView() {
    var sizingXReadCount = 0
        private set
    var sizingYReadCount = 0
        private set

    override var sizingX = Sizing.WRAP
        get() {
            sizingXReadCount++
            return field
        }
    override var sizingY = Sizing.WRAP
        get() {
            sizingYReadCount++
            return field
        }

    override fun calculateContentSizeX(): Int {
        events += "contentX"
        return 10
    }

    override fun calculateContentSizeY(): Int {
        events += "contentY"
        return 20
    }

    override fun calculateChildrenActualSize() {
        events += "children:${actualSize.x}x${actualSize.y}"
    }

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = Remover.NONE
}

class AxisWiseActualSizeTest {

    @Test
    fun `X成分はY成分を計算せず同期的に確定するのだ`() {
        val events = mutableListOf<String>()
        val view = RecordingSizeView(events)
        view.initSizeCalculation(TEST_RENDERING_PROXY)
        view.calculateContentSizeX { throw UnsupportedOperationException() }
        assertEquals(listOf("contentX"), events)

        view.calculateActualSizeX(100)
        assertEquals(1, view.sizingXReadCount)
        assertEquals(0, view.sizingYReadCount)
        assertEquals(10, view.actualSizeX)
        assertEquals(1, view.sizingXReadCount)
        assertFailsWith<IllegalStateException> { view.actualSizeY }
        assertEquals(listOf("contentX"), events)
    }

    @Test
    fun `Y成分はX成分を計算せず同期的に確定するのだ`() {
        val events = mutableListOf<String>()
        val view = RecordingSizeView(events)
        view.initSizeCalculation(TEST_RENDERING_PROXY)
        view.calculateContentSizeY { throw UnsupportedOperationException() }
        assertEquals(listOf("contentY"), events)

        view.calculateActualSizeY(100)
        assertEquals(1, view.sizingYReadCount)
        assertEquals(0, view.sizingXReadCount)
        assertEquals(20, view.actualSizeY)
        assertEquals(1, view.sizingYReadCount)
        assertFailsWith<IllegalStateException> { view.actualSizeX }
        assertEquals(listOf("contentY"), events)
    }

    @Test
    fun `FILLはどちらの軸からでも渡された領域で確定するのだ`() {
        listOf(true, false).forEach { xFirst ->
            val events = mutableListOf<String>()
            val view = RecordingSizeView(events)
            view.sizingX = Sizing.FILL
            view.sizingY = Sizing.FILL
            view.initSizeCalculation(TEST_RENDERING_PROXY)

            if (xFirst) {
                view.calculateActualSizeX(120)
                assertEquals(1, view.sizingXReadCount)
                assertEquals(120, view.actualSizeX)
                assertFailsWith<IllegalStateException> { view.actualSizeY }
                view.calculateActualSizeY(500)
            } else {
                view.calculateActualSizeY(500)
                assertEquals(1, view.sizingYReadCount)
                assertEquals(500, view.actualSizeY)
                assertFailsWith<IllegalStateException> { view.actualSizeX }
                view.calculateActualSizeX(120)
            }
            assertEquals(IntPoint(120, 500), view.actualSize)
            assertEquals(1, view.sizingXReadCount)
            assertEquals(1, view.sizingYReadCount)
            assertEquals(emptyList(), events)
        }
    }

    @Test
    fun `既存の入口は両軸を確定してから子の計算を呼ぶのだ`() {
        val events = mutableListOf<String>()
        val view = RecordingSizeView(events)
        view.calculateSize(TEST_RENDERING_PROXY, IntPoint(100, 200))

        assertEquals(listOf("contentX", "contentY", "children:10x20"), events)
        assertEquals(IntPoint(10, 20), view.actualSize)
        assertEquals(listOf("contentX", "contentY", "children:10x20"), events)
    }

    @Test
    fun `ページの子は親の両軸確定後に生成され同期的に計算されるのだ`() {
        val events = mutableListOf<String>()
        val view = PagingView()
        view.childrenGenerators += ChildrenGenerator { _, regionSize ->
            assertEquals(IntPoint(100, 35), regionSize)
            assertEquals(regionSize, view.actualSize)
            events += "generate"
            List(3) { Child(Alignment.START, RecordingSizeView(events)) }
        }

        view.calculateSize(TEST_RENDERING_PROXY, IntPoint(100, 35))

        assertEquals(listOf("generate", "contentX", "contentX", "contentX", "contentY", "contentY", "contentY", "children:10x20", "children:10x20", "children:10x20"), events)
        assertEquals(3, view.pageCount.value)
    }

}
