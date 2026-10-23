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
    fun getStoredRegionSizeXProvider() = regionSizeXProvider
    fun getStoredRegionSizeYProvider() = regionSizeYProvider

    override fun initSizeCalculation(renderingProxy: RenderingProxy, regionSizeXProvider: () -> Int, regionSizeYProvider: () -> Int) {
        super.initSizeCalculation(renderingProxy, regionSizeXProvider, regionSizeYProvider)
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

    override fun getOwnContentSizeX() = 10
    override fun getOwnContentSizeY() = 20

    override fun calculateChildrenActualSizeX() {
        events += "childrenX:${getActualSizeX()}"
    }

    override fun calculateChildrenActualSizeY() {
        events += "childrenY:${getActualSizeY()}"
    }

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = Remover.NONE
}

class AxisWiseActualSizeTest {

    @Test
    fun `サイズ計算の初期化はRenderingProxyと領域の供給元を格納するだけなのだ`() {
        val view = RecordingActualSizeView()
        val regionSizeXProvider = { throw UnsupportedOperationException() }
        val regionSizeYProvider = { throw UnsupportedOperationException() }

        view.initSizeCalculation(TEST_RENDERING_PROXY, regionSizeXProvider, regionSizeYProvider)

        assertSame(TEST_RENDERING_PROXY, view.getStoredRenderingProxy())
        assertSame(regionSizeXProvider, view.getStoredRegionSizeXProvider())
        assertSame(regionSizeYProvider, view.getStoredRegionSizeYProvider())
        assertEquals(1, view.initializationCount)
        assertEquals(emptyList<String>(), view.events)
        assertThrows(IllegalStateException::class.java) { view.getContentSizeX() }
        assertThrows(IllegalStateException::class.java) { view.getContentSizeY() }
        assertThrows(IllegalStateException::class.java) { view.getActualSizeX() }
        assertThrows(IllegalStateException::class.java) { view.getActualSizeY() }
    }

    @Test
    fun `領域の供給元はFILLの実寸計算時だけ呼びWRAPでは呼ばないのだ`() {
        Sizing.entries.forEach { sizing ->
            listOf(true, false).forEach { xFirst ->
                val calls = mutableListOf<String>()
                val view = RecordingActualSizeView()
                view.sizingX = sizing
                view.sizingY = sizing
                view.initSizeCalculation(TEST_RENDERING_PROXY, {
                    calls += "regionX"
                    101
                }, {
                    calls += "regionY"
                    203
                })
                assertEquals(emptyList<String>(), calls)

                view.evaluateContentSizeX()
                view.evaluateContentSizeY()
                assertEquals(emptyList<String>(), calls)

                if (xFirst) {
                    view.calculateActualSizeX()
                    assertEquals(if (sizing == Sizing.FILL) listOf("regionX") else emptyList<String>(), calls)
                    view.calculateActualSizeY()
                } else {
                    view.calculateActualSizeY()
                    assertEquals(if (sizing == Sizing.FILL) listOf("regionY") else emptyList<String>(), calls)
                    view.calculateActualSizeX()
                }

                assertEquals(if (sizing == Sizing.FILL) IntPoint(101, 203) else IntPoint(10, 20), view.actualSize)
                assertEquals(if (sizing == Sizing.WRAP) emptyList<String>() else if (xFirst) listOf("regionX", "regionY") else listOf("regionY", "regionX"), calls)
            }
        }
    }

    @Test
    fun `子の領域の供給元は親の実寸確定前には参照できなくても初期化できるのだ`() {
        listOf(true, false).forEach { xFirst ->
            val childView = RecordingActualSizeView()
            childView.sizingX = Sizing.FILL
            childView.sizingY = Sizing.FILL
            val marginView = MarginView(2, 3, 5, 7)
            marginView.add(Child(Unit, childView))
            val view = StackView()
            view.sizingX = Sizing.FILL
            view.sizingY = Sizing.FILL
            view.add(Child(Unit, marginView))

            view.initSizeCalculation(TEST_RENDERING_PROXY, { 101 }, { 203 })
            assertThrows(IllegalStateException::class.java) { childView.getStoredRegionSizeXProvider()() }
            assertThrows(IllegalStateException::class.java) { childView.getStoredRegionSizeYProvider()() }
            view.evaluateContentSizeX()
            view.evaluateContentSizeY()
            assertThrows(IllegalStateException::class.java) { childView.getStoredRegionSizeXProvider()() }
            assertThrows(IllegalStateException::class.java) { childView.getStoredRegionSizeYProvider()() }

            if (xFirst) {
                view.calculateActualSizeX()
                assertEquals(96, childView.getActualSizeX())
                assertThrows(IllegalStateException::class.java) { childView.getStoredRegionSizeYProvider()() }
                view.calculateActualSizeY()
            } else {
                view.calculateActualSizeY()
                assertEquals(191, childView.getActualSizeY())
                assertThrows(IllegalStateException::class.java) { childView.getStoredRegionSizeXProvider()() }
                view.calculateActualSizeX()
            }

            assertEquals(IntPoint(96, 191), childView.actualSize)
        }
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

        view.initSizeCalculation(TEST_RENDERING_PROXY, { 100 }, { 200 })

        listOf(childView, otherChildView).forEach {
            assertSame(TEST_RENDERING_PROXY, it.getStoredRenderingProxy())
            assertEquals(1, it.initializationCount)
            assertEquals(emptyList<String>(), it.events)
            assertThrows(IllegalStateException::class.java) { it.getContentSizeX() }
            assertThrows(IllegalStateException::class.java) { it.getContentSizeY() }
        }

        view.evaluateContentSizeX()
        view.evaluateContentSizeY()
        view.calculateActualSizeX()
        view.calculateActualSizeY()

        listOf(childView, otherChildView).forEach {
            assertEquals(1, it.initializationCount)
            assertEquals(IntPoint(10, 20), it.contentSize)
            assertEquals(IntPoint(10, 20), it.actualSize)
        }
    }

    @Test
    fun `X成分はY成分を計算せず同期的に確定するのだ`() {
        val view = RecordingActualSizeView()
        view.initSizeCalculation(TEST_RENDERING_PROXY, { 100 }, { 100 })
        view.evaluateContentSizeX()
        view.evaluateContentSizeY()

        view.calculateActualSizeX()
        assertEquals(listOf("sizingX", "childrenX:10"), view.events)
        assertEquals(10, view.getActualSizeX())
        assertThrows(IllegalStateException::class.java) { view.getActualSizeY() }
        assertEquals(listOf("sizingX", "childrenX:10"), view.events)
    }

    @Test
    fun `Y成分はX成分を計算せず同期的に確定するのだ`() {
        val view = RecordingActualSizeView()
        view.initSizeCalculation(TEST_RENDERING_PROXY, { 100 }, { 100 })
        view.evaluateContentSizeX()
        view.evaluateContentSizeY()

        view.calculateActualSizeY()
        assertEquals(listOf("sizingY", "childrenY:20"), view.events)
        assertEquals(20, view.getActualSizeY())
        assertThrows(IllegalStateException::class.java) { view.getActualSizeX() }
        assertEquals(listOf("sizingY", "childrenY:20"), view.events)
    }

    @Test
    fun `FILLはどちらの軸からでも渡された領域で確定するのだ`() {
        listOf(true, false).forEach { xFirst ->
            val view = RecordingActualSizeView()
            view.sizingX = Sizing.FILL
            view.sizingY = Sizing.FILL
            view.initSizeCalculation(TEST_RENDERING_PROXY, { 120 }, { 500 })

            if (xFirst) {
                view.calculateActualSizeX()
                assertEquals(listOf("sizingX", "childrenX:120"), view.events)
                assertEquals(120, view.getActualSizeX())
                assertThrows(IllegalStateException::class.java) { view.getActualSizeY() }
                view.calculateActualSizeY()
            } else {
                view.calculateActualSizeY()
                assertEquals(listOf("sizingY", "childrenY:500"), view.events)
                assertEquals(500, view.getActualSizeY())
                assertThrows(IllegalStateException::class.java) { view.getActualSizeX() }
                view.calculateActualSizeX()
            }
            val expectedEvents = if (xFirst) listOf("sizingX", "childrenX:120", "sizingY", "childrenY:500") else listOf("sizingY", "childrenY:500", "sizingX", "childrenX:120")
            assertEquals(expectedEvents, view.events)
            assertEquals(120, view.getActualSizeX())
            assertEquals(500, view.getActualSizeY())
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
                view.initSizeCalculation(TEST_RENDERING_PROXY, { 100 }, { 200 })
                view.evaluateContentSizeX()
                view.evaluateContentSizeY()
                val expectedX = if (sizingX == Sizing.FILL) 100 else 10
                val expectedY = if (sizingY == Sizing.FILL) 200 else 20

                view.calculateActualSizeX()
                view.calculateActualSizeY()

                assertEquals(listOf("sizingX", "childrenX:$expectedX", "sizingY", "childrenY:$expectedY"), view.events)
                assertEquals(expectedX, view.getActualSizeX())
                assertEquals(expectedY, view.getActualSizeY())
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
        stackView.initSizeCalculation(TEST_RENDERING_PROXY, { 100 }, { 200 })
        stackView.evaluateContentSizeX()
        stackView.evaluateContentSizeY()

        stackView.calculateActualSizeX()
        assertEquals(95, childView.getActualSizeX())
        assertThrows(IllegalStateException::class.java) { childView.getActualSizeY() }
        assertEquals(10, otherChildView.getActualSizeX())
        assertThrows(IllegalStateException::class.java) { otherChildView.getActualSizeY() }
        assertEquals(listOf("sizingX", "sizingX", "childrenX:95"), childView.events)
        assertEquals(listOf("sizingX", "childrenX:10"), otherChildView.events)

        stackView.calculateActualSizeY()

        assertEquals(100, stackView.getActualSizeX())
        assertEquals(200, stackView.getActualSizeY())
        assertEquals(100, marginView.getActualSizeX())
        assertEquals(200, marginView.getActualSizeY())
        assertEquals(95, childView.getActualSizeX())
        assertEquals(188, childView.getActualSizeY())
        assertEquals(listOf("sizingX", "sizingX", "childrenX:95", "sizingY", "sizingY", "childrenY:188"), childView.events)
        assertEquals(10, otherChildView.getActualSizeX())
        assertEquals(20, otherChildView.getActualSizeY())
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
            view.initSizeCalculation(TEST_RENDERING_PROXY, { 101 }, { 203 })
            view.evaluateContentSizeX()
            view.evaluateContentSizeY()

            if (xFirst) {
                view.calculateActualSizeX()
                assertEquals(101, childView.getActualSizeX())
                assertThrows(IllegalStateException::class.java) { childView.getActualSizeY() }
                view.calculateActualSizeY()
            } else {
                view.calculateActualSizeY()
                assertEquals(203, childView.getActualSizeY())
                assertThrows(IllegalStateException::class.java) { childView.getActualSizeX() }
                view.calculateActualSizeX()
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
                view.initSizeCalculation(TEST_RENDERING_PROXY, { 101 }, { 203 })
                view.evaluateContentSizeX()
                view.evaluateContentSizeY()
                val expectedSizes = if (horizontal) listOf(IntPoint(51, 203), IntPoint(50, 203)) else listOf(IntPoint(101, 102), IntPoint(101, 101))

                if (xFirst) {
                    view.calculateActualSizeX()
                    childViews.forEachIndexed { index, childView ->
                        assertEquals(expectedSizes[index].x, childView.getActualSizeX())
                        assertThrows(IllegalStateException::class.java) { childView.getActualSizeY() }
                    }
                    view.calculateActualSizeY()
                } else {
                    view.calculateActualSizeY()
                    childViews.forEachIndexed { index, childView ->
                        assertEquals(expectedSizes[index].y, childView.getActualSizeY())
                        assertThrows(IllegalStateException::class.java) { childView.getActualSizeX() }
                    }
                    view.calculateActualSizeX()
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
    fun `WRAPとFILLが混在するリストも既存の累積丸めと分配順序を維持するのだ`() {
        listOf(true, false).forEach { horizontal ->
            listOf(true, false).forEach { xFirst ->
                val view = if (horizontal) XListView() else YListView()
                view.sizingX = Sizing.FILL
                view.sizingY = Sizing.FILL
                val childViews = List(4) { index ->
                    val fill = index % 2 == 1
                    RecordingActualSizeView().also { childView ->
                        childView.sizingX = if (fill) Sizing.FILL else Sizing.WRAP
                        childView.sizingY = if (fill) Sizing.FILL else Sizing.WRAP
                        view.add(Child(ListView.Position().also { it.weight = 1.0 }, childView))
                    }
                }

                repeat(2) {
                    view.initSizeCalculation(TEST_RENDERING_PROXY, { 101 }, { 203 })
                    view.evaluateContentSizeX()
                    view.evaluateContentSizeY()
                    if (xFirst) {
                        view.calculateActualSizeX()
                        view.calculateActualSizeY()
                    } else {
                        view.calculateActualSizeY()
                        view.calculateActualSizeX()
                    }

                    val expectedSizes = if (horizontal) listOf(IntPoint(10, 20), IntPoint(26, 203), IntPoint(10, 20), IntPoint(25, 203)) else listOf(IntPoint(10, 20), IntPoint(101, 51), IntPoint(10, 20), IntPoint(101, 51))
                    assertEquals(expectedSizes, childViews.map { it.actualSize })
                }
            }
        }
    }

    @Test
    fun `リストの領域の供給元は任意の順序で繰り返し参照しても同じ割り当てを返すのだ`() {
        listOf(true, false).forEach { horizontal ->
            val view = if (horizontal) XListView() else YListView()
            view.sizingX = Sizing.FILL
            view.sizingY = Sizing.FILL
            val firstChildView = RecordingActualSizeView()
            val secondChildView = RecordingActualSizeView()
            secondChildView.sizingX = Sizing.FILL
            secondChildView.sizingY = Sizing.FILL
            listOf(firstChildView, secondChildView).forEach {
                view.add(Child(ListView.Position().also { position -> position.weight = 1.0 }, it))
            }
            view.initSizeCalculation(TEST_RENDERING_PROXY, { 101 }, { 203 })
            view.evaluateContentSizeX()
            view.evaluateContentSizeY()
            view.calculateActualSizeX()
            view.calculateActualSizeY()

            val providers = if (horizontal) listOf(firstChildView.getStoredRegionSizeXProvider(), secondChildView.getStoredRegionSizeXProvider()) else listOf(firstChildView.getStoredRegionSizeYProvider(), secondChildView.getStoredRegionSizeYProvider())
            val expectedRegions = if (horizontal) listOf(50, 51, 50, 51) else listOf(101, 102, 101, 102)
            assertEquals(expectedRegions, listOf(1, 0, 1, 0).map { providers[it]() })
            assertEquals(IntPoint(10, 20), firstChildView.actualSize)
            assertEquals(if (horizontal) IntPoint(50, 203) else IntPoint(101, 101), secondChildView.actualSize)
        }
    }

    @Test
    fun `子を追加して再初期化すると登録位置に対応する領域を再計算するのだ`() {
        listOf(true, false).forEach { horizontal ->
            val view = if (horizontal) XListView() else YListView()
            view.sizingX = Sizing.FILL
            view.sizingY = Sizing.FILL
            fun addChild(): RecordingActualSizeView {
                val childView = RecordingActualSizeView()
                childView.sizingX = Sizing.FILL
                childView.sizingY = Sizing.FILL
                view.add(Child(ListView.Position().also { it.weight = 1.0 }, childView))
                return childView
            }
            fun calculate() {
                view.initSizeCalculation(TEST_RENDERING_PROXY, { 101 }, { 203 })
                view.evaluateContentSizeX()
                view.evaluateContentSizeY()
                view.calculateActualSizeX()
                view.calculateActualSizeY()
            }
            val childViews = mutableListOf(addChild(), addChild())
            calculate()
            val expectedInitialSizes = if (horizontal) listOf(IntPoint(51, 203), IntPoint(50, 203)) else listOf(IntPoint(101, 102), IntPoint(101, 101))
            assertEquals(expectedInitialSizes, childViews.map { it.actualSize })

            childViews += addChild()
            calculate()

            val expectedSizes = if (horizontal) listOf(IntPoint(34, 203), IntPoint(33, 203), IntPoint(34, 203)) else listOf(IntPoint(101, 68), IntPoint(101, 67), IntPoint(101, 68))
            assertEquals(expectedSizes, childViews.map { it.actualSize })
            val providers = childViews.map { if (horizontal) it.getStoredRegionSizeXProvider() else it.getStoredRegionSizeYProvider() }
            assertEquals(expectedSizes.map { if (horizontal) it.x else it.y }, providers.map { it() })
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
            view.initSizeCalculation(TEST_RENDERING_PROXY, { 300 }, { 400 })
            view.evaluateContentSizeX()
            view.evaluateContentSizeY()

            if (xFirst) {
                view.calculateActualSizeX()
                childViews.forEachIndexed { index, childView ->
                    assertEquals(expectedSizes[index].x, childView.getActualSizeX())
                    assertThrows(IllegalStateException::class.java) { childView.getActualSizeY() }
                }
                view.calculateActualSizeY()
            } else {
                view.calculateActualSizeY()
                childViews.forEachIndexed { index, childView ->
                    assertEquals(expectedSizes[index].y, childView.getActualSizeY())
                    assertThrows(IllegalStateException::class.java) { childView.getActualSizeX() }
                }
                view.calculateActualSizeX()
            }
            childViews.forEachIndexed { index, childView ->
                assertEquals(expectedSizes[index], childView.actualSize)
                assertEquals(4, childView.events.size)
            }
        }
    }

}
