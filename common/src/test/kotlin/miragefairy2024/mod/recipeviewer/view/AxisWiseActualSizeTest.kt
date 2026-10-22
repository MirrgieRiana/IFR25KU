package miragefairy2024.mod.recipeviewer.view

import miragefairy2024.mod.recipeviewer.views.AbstractView
import miragefairy2024.mod.recipeviewer.views.AbsoluteView
import miragefairy2024.mod.recipeviewer.views.Child
import miragefairy2024.mod.recipeviewer.views.ListView
import miragefairy2024.mod.recipeviewer.views.MarginView
import miragefairy2024.mod.recipeviewer.views.StackView
import miragefairy2024.mod.recipeviewer.views.WrapperView
import miragefairy2024.mod.recipeviewer.views.XListView
import miragefairy2024.mod.recipeviewer.views.YListView
import miragefairy2024.util.Remover
import net.minecraft.network.chat.FormattedText
import net.minecraft.util.FormattedCharSequence
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

private val TEST_RENDERING_PROXY = object : RenderingProxy {
    override fun calculateTextWidth(text: FormattedText) = throw UnsupportedOperationException()
    override fun calculateTextWidth(text: FormattedCharSequence) = throw UnsupportedOperationException()
    override fun getTextHeight() = throw UnsupportedOperationException()
    override fun wrapText(text: FormattedText, maxWidth: Int): List<FormattedCharSequence> = throw UnsupportedOperationException()
}

private class RecordingActualSizeView : AbstractView() {
    val events = mutableListOf<String>()

    var initializationCount = 0
        private set

    fun getStoredRenderingProxy() = renderingProxy

    override fun initSizeCalculation(renderingProxy: RenderingProxy) {
        super.initSizeCalculation(renderingProxy)
        initializationCount += 1
    }

    override var sizingX = Sizing.WRAP
        get() {
            events += "sizingX"
            return field
        }
    override var sizingY = Sizing.WRAP
        get() {
            events += "sizingY"
            return field
        }

    override fun calculateContentSizeXImpl() = 10
    override fun calculateContentSizeYImpl() = 20

    override fun calculateChildrenActualSizeX() {
        events += "childrenX:$actualSizeX"
    }

    override fun calculateChildrenActualSizeY() {
        events += "childrenY:$actualSizeY"
    }

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = Remover.NONE
}

class AxisWiseActualSizeTest {

    @Test
    fun `サイズ計算の初期化はRenderingProxyを格納するだけなのだ`() {
        val view = RecordingActualSizeView()

        view.initSizeCalculation(TEST_RENDERING_PROXY)

        assertSame(TEST_RENDERING_PROXY, view.getStoredRenderingProxy())
        assertEquals(1, view.initializationCount)
        assertEquals(emptyList<String>(), view.events)
        assertThrows(IllegalStateException::class.java) { view.contentSizeX }
        assertThrows(IllegalStateException::class.java) { view.contentSizeY }
        assertThrows(IllegalStateException::class.java) { view.actualSizeX }
        assertThrows(IllegalStateException::class.java) { view.actualSizeY }
    }

    @Test
    fun `初期化はコンテナーとラッパーの子へ伝播しサイズ計算では繰り返さないのだ`() {
        val childView = RecordingActualSizeView()
        val wrapperView = WrapperView()
        wrapperView.add(Child(Unit, childView))
        val otherChildView = RecordingActualSizeView()
        val view = StackView()
        view.add(Child(Unit, wrapperView))
        view.add(Child(Unit, otherChildView))

        view.initSizeCalculation(TEST_RENDERING_PROXY)

        listOf(childView, otherChildView).forEach {
            assertSame(TEST_RENDERING_PROXY, it.getStoredRenderingProxy())
            assertEquals(1, it.initializationCount)
            assertEquals(emptyList<String>(), it.events)
            assertThrows(IllegalStateException::class.java) { it.contentSizeX }
            assertThrows(IllegalStateException::class.java) { it.contentSizeY }
        }

        view.calculateContentSizeX()
        view.calculateContentSizeY()
        view.calculateActualSizeX(100)
        view.calculateActualSizeY(200)

        listOf(childView, otherChildView).forEach {
            assertEquals(1, it.initializationCount)
            assertEquals(IntPoint(10, 20), it.contentSize)
            assertEquals(IntPoint(10, 20), it.actualSize)
        }
    }

    @Test
    fun `X成分はY成分を計算せず同期的に確定するのだ`() {
        val view = RecordingActualSizeView()
        view.initSizeCalculation(TEST_RENDERING_PROXY)
        view.calculateContentSizeX()
        view.calculateContentSizeY()

        view.calculateActualSizeX(100)
        assertEquals(listOf("sizingX", "childrenX:10"), view.events)
        assertEquals(10, view.actualSizeX)
        assertThrows(IllegalStateException::class.java) { view.actualSizeY }
        assertEquals(listOf("sizingX", "childrenX:10"), view.events)
    }

    @Test
    fun `Y成分はX成分を計算せず同期的に確定するのだ`() {
        val view = RecordingActualSizeView()
        view.initSizeCalculation(TEST_RENDERING_PROXY)
        view.calculateContentSizeX()
        view.calculateContentSizeY()

        view.calculateActualSizeY(100)
        assertEquals(listOf("sizingY", "childrenY:20"), view.events)
        assertEquals(20, view.actualSizeY)
        assertThrows(IllegalStateException::class.java) { view.actualSizeX }
        assertEquals(listOf("sizingY", "childrenY:20"), view.events)
    }

    @Test
    fun `FILLはどちらの軸からでも渡された領域で確定するのだ`() {
        listOf(true, false).forEach { xFirst ->
            val view = RecordingActualSizeView()
            view.sizingX = Sizing.FILL
            view.sizingY = Sizing.FILL

            if (xFirst) {
                view.calculateActualSizeX(120)
                assertEquals(listOf("sizingX", "childrenX:120"), view.events)
                assertEquals(120, view.actualSizeX)
                assertThrows(IllegalStateException::class.java) { view.actualSizeY }
                view.calculateActualSizeY(500)
            } else {
                view.calculateActualSizeY(500)
                assertEquals(listOf("sizingY", "childrenY:500"), view.events)
                assertEquals(500, view.actualSizeY)
                assertThrows(IllegalStateException::class.java) { view.actualSizeX }
                view.calculateActualSizeX(120)
            }
            val expectedEvents = if (xFirst) listOf("sizingX", "childrenX:120", "sizingY", "childrenY:500") else listOf("sizingY", "childrenY:500", "sizingX", "childrenX:120")
            assertEquals(expectedEvents, view.events)
            assertEquals(120, view.actualSizeX)
            assertEquals(500, view.actualSizeY)
            assertEquals(IntPoint(120, 500), view.actualSize)
            assertEquals(expectedEvents, view.events)
        }
    }

    @Test
    fun `WRAPとFILLの組合せを保って各軸確定後に子の同じ軸を計算するのだ`() {
        Sizing.entries.forEach { sizingX ->
            Sizing.entries.forEach { sizingY ->
                val view = RecordingActualSizeView()
                view.sizingX = sizingX
                view.sizingY = sizingY
                view.initSizeCalculation(TEST_RENDERING_PROXY)
                view.calculateContentSizeX()
                view.calculateContentSizeY()
                val expectedX = if (sizingX == Sizing.FILL) 100 else 10
                val expectedY = if (sizingY == Sizing.FILL) 200 else 20

                view.calculateActualSizeX(100)
                view.calculateActualSizeY(200)

                assertEquals(listOf("sizingX", "childrenX:$expectedX", "sizingY", "childrenY:$expectedY"), view.events)
                assertEquals(expectedX, view.actualSizeX)
                assertEquals(expectedY, view.actualSizeY)
                assertEquals(listOf("sizingX", "childrenX:$expectedX", "sizingY", "childrenY:$expectedY"), view.events)
            }
        }
    }

    @Test
    fun `既存のコンテナーとラッパーは割り当て領域を子へ伝播するのだ`() {
        val childView = RecordingActualSizeView()
        childView.sizingX = Sizing.FILL
        childView.sizingY = Sizing.FILL
        val marginView = MarginView(2, 3, 5, 7)
        marginView.add(Child(Unit, childView))
        val stackView = StackView()
        stackView.sizingX = Sizing.FILL
        stackView.sizingY = Sizing.FILL
        stackView.add(Child(Unit, marginView))
        val otherChildView = RecordingActualSizeView()
        stackView.add(Child(Unit, otherChildView))
        stackView.initSizeCalculation(TEST_RENDERING_PROXY)
        stackView.calculateContentSizeX()
        stackView.calculateContentSizeY()

        stackView.calculateActualSizeX(100)
        assertEquals(95, childView.actualSizeX)
        assertThrows(IllegalStateException::class.java) { childView.actualSizeY }
        assertEquals(10, otherChildView.actualSizeX)
        assertThrows(IllegalStateException::class.java) { otherChildView.actualSizeY }
        assertEquals(listOf("sizingX", "sizingX", "childrenX:95"), childView.events)
        assertEquals(listOf("sizingX", "childrenX:10"), otherChildView.events)

        stackView.calculateActualSizeY(200)

        assertEquals(100, stackView.actualSizeX)
        assertEquals(200, stackView.actualSizeY)
        assertEquals(100, marginView.actualSizeX)
        assertEquals(200, marginView.actualSizeY)
        assertEquals(95, childView.actualSizeX)
        assertEquals(188, childView.actualSizeY)
        assertEquals(listOf("sizingX", "sizingX", "childrenX:95", "sizingY", "sizingY", "childrenY:188"), childView.events)
        assertEquals(10, otherChildView.actualSizeX)
        assertEquals(20, otherChildView.actualSizeY)
        assertEquals(listOf("sizingX", "childrenX:10", "sizingY", "childrenY:20"), otherChildView.events)
    }

    @Test
    fun `ラッパーはどちらの軸からでも子の同じ軸だけに伝播するのだ`() {
        listOf(true, false).forEach { xFirst ->
            val childView = RecordingActualSizeView()
            childView.sizingX = Sizing.FILL
            childView.sizingY = Sizing.FILL
            val view = WrapperView()
            view.add(Child(Unit, childView))
            view.initSizeCalculation(TEST_RENDERING_PROXY)
            view.calculateContentSizeX()
            view.calculateContentSizeY()

            if (xFirst) {
                view.calculateActualSizeX(101)
                assertEquals(101, childView.actualSizeX)
                assertThrows(IllegalStateException::class.java) { childView.actualSizeY }
                view.calculateActualSizeY(203)
            } else {
                view.calculateActualSizeY(203)
                assertEquals(203, childView.actualSizeY)
                assertThrows(IllegalStateException::class.java) { childView.actualSizeX }
                view.calculateActualSizeX(101)
            }
            val expectedEvents = if (xFirst) listOf("sizingX", "sizingX", "childrenX:101", "sizingY", "sizingY", "childrenY:203") else listOf("sizingY", "sizingY", "childrenY:203", "sizingX", "sizingX", "childrenX:101")
            assertEquals(expectedEvents, childView.events)
            assertEquals(IntPoint(101, 203), childView.actualSize)
        }
    }

    @Test
    fun `リストはどちらの軸からでも独立して余剰領域を分配するのだ`() {
        listOf(true, false).forEach { horizontal ->
            listOf(true, false).forEach { xFirst ->
                val view = if (horizontal) XListView() else YListView()
                view.sizingX = Sizing.FILL
                view.sizingY = Sizing.FILL
                val childViews = List(2) {
                    RecordingActualSizeView().also { childView ->
                        childView.sizingX = Sizing.FILL
                        childView.sizingY = Sizing.FILL
                        view.add(Child(ListView.Position().also { position -> position.weight = 1.0 }, childView))
                    }
                }
                view.initSizeCalculation(TEST_RENDERING_PROXY)
                view.calculateContentSizeX()
                view.calculateContentSizeY()
                val expectedSizes = if (horizontal) listOf(IntPoint(51, 203), IntPoint(50, 203)) else listOf(IntPoint(101, 102), IntPoint(101, 101))

                if (xFirst) {
                    view.calculateActualSizeX(101)
                    childViews.forEachIndexed { index, childView ->
                        assertEquals(expectedSizes[index].x, childView.actualSizeX)
                        assertThrows(IllegalStateException::class.java) { childView.actualSizeY }
                    }
                    view.calculateActualSizeY(203)
                } else {
                    view.calculateActualSizeY(203)
                    childViews.forEachIndexed { index, childView ->
                        assertEquals(expectedSizes[index].y, childView.actualSizeY)
                        assertThrows(IllegalStateException::class.java) { childView.actualSizeX }
                    }
                    view.calculateActualSizeX(101)
                }
                childViews.forEachIndexed { index, childView ->
                    val expectedSize = expectedSizes[index]
                    val expectedEvents = if (xFirst) listOf("sizingX", "childrenX:${expectedSize.x}", "sizingY", "childrenY:${expectedSize.y}") else listOf("sizingY", "childrenY:${expectedSize.y}", "sizingX", "childrenX:${expectedSize.x}")
                    assertEquals(expectedSize, childView.actualSize)
                    assertEquals(expectedEvents, childView.events)
                }
            }
        }
    }

    @Test
    fun `絶対配置はどちらの軸からでも配置指定に従って子の同じ軸だけを計算するのだ`() {
        listOf(true, false).forEach { xFirst ->
            val view = AbsoluteView(IntPoint(101, 203))
            val positions = listOf(AbsoluteView.Fill, AbsoluteView.Offset(IntPoint(3, 5)), AbsoluteView.Bounds(IntRectangle(7, 11, 29, 43)))
            val childViews = positions.map { position ->
                RecordingActualSizeView().also { childView ->
                    childView.sizingX = Sizing.FILL
                    childView.sizingY = Sizing.FILL
                    view.add(Child(position, childView))
                }
            }
            val expectedSizes = listOf(IntPoint(101, 203), IntPoint(10, 20), IntPoint(29, 43))
            view.initSizeCalculation(TEST_RENDERING_PROXY)
            view.calculateContentSizeX()
            view.calculateContentSizeY()

            if (xFirst) {
                view.calculateActualSizeX(300)
                childViews.forEachIndexed { index, childView ->
                    assertEquals(expectedSizes[index].x, childView.actualSizeX)
                    assertThrows(IllegalStateException::class.java) { childView.actualSizeY }
                }
                view.calculateActualSizeY(400)
            } else {
                view.calculateActualSizeY(400)
                childViews.forEachIndexed { index, childView ->
                    assertEquals(expectedSizes[index].y, childView.actualSizeY)
                    assertThrows(IllegalStateException::class.java) { childView.actualSizeX }
                }
                view.calculateActualSizeX(300)
            }
            childViews.forEachIndexed { index, childView ->
                assertEquals(expectedSizes[index], childView.actualSize)
                assertEquals(4, childView.events.size)
            }
        }
    }

}
