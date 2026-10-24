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

    var ownContentSize = IntPoint(10, 20)

    override fun evaluateContentSizeXImpl() = ownContentSize.x
    override fun evaluateContentSizeYImpl() = ownContentSize.y

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = ViewController.NONE
}

class AxisWiseActualSizeTest {

    @Test
    fun `サイズ計算の初期化はRenderingProxyと領域の供給元を格納しサイズを評価しないのだ`() {
        val view = RecordingActualSizeView()
        val regionSizeXProvider = { throw UnsupportedOperationException() }
        val regionSizeYProvider = { throw UnsupportedOperationException() }

        view.initSizeCalculation(TEST_RENDERING_PROXY, regionSizeXProvider, regionSizeYProvider)

        assertSame(TEST_RENDERING_PROXY, view.getStoredRenderingProxy())
        assertSame(regionSizeXProvider, view.getStoredRegionSizeXProvider())
        assertSame(regionSizeYProvider, view.getStoredRegionSizeYProvider())
        assertEquals(1, view.initializationCount)
        assertEquals(emptyList<String>(), view.events)
    }

    @Test
    fun `再初期化は計算済みの内容サイズと実サイズを破棄するのだ`() {
        Sizing.entries.forEach { sizing ->
            val view = RecordingActualSizeView()
            view.sizingX = sizing
            view.sizingY = sizing
            view.initSizeCalculation(TEST_RENDERING_PROXY, { 100 }, { 200 })
            assertEquals(IntPoint(10, 20), view.evaluateContentSize())
            assertEquals(if (sizing == Sizing.FILL) IntPoint(100, 200) else IntPoint(10, 20), view.evaluateActualSize())

            view.ownContentSize = IntPoint(30, 40)
            assertEquals(IntPoint(10, 20), view.evaluateContentSize())
            assertEquals(if (sizing == Sizing.FILL) IntPoint(100, 200) else IntPoint(10, 20), view.evaluateActualSize())
            val events = view.events.toList()
            view.initSizeCalculation(TEST_RENDERING_PROXY, { 300 }, { 400 })
            assertEquals(events, view.events)
            assertEquals(IntPoint(30, 40), view.evaluateContentSize())
            assertEquals(if (sizing == Sizing.FILL) IntPoint(300, 400) else IntPoint(30, 40), view.evaluateActualSize())
        }
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
                    view.evaluateActualSizeX()
                    assertEquals(if (sizing == Sizing.FILL) listOf("regionX") else emptyList<String>(), calls)
                    view.evaluateActualSizeY()
                } else {
                    view.evaluateActualSizeY()
                    assertEquals(if (sizing == Sizing.FILL) listOf("regionY") else emptyList<String>(), calls)
                    view.evaluateActualSizeX()
                }

                assertEquals(if (sizing == Sizing.FILL) IntPoint(101, 203) else IntPoint(10, 20), view.evaluateActualSize())
                assertEquals(if (sizing == Sizing.WRAP) emptyList<String>() else if (xFirst) listOf("regionX", "regionY") else listOf("regionY", "regionX"), calls)
            }
        }
    }

    @Test
    fun `子の領域の供給元は必要な軸の親の実寸を評価するのだ`() {
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
            view.evaluateContentSizeX()
            view.evaluateContentSizeY()

            assertEquals(emptyList<String>(), childView.events)

            if (xFirst) {
                assertEquals(96, childView.getStoredRegionSizeXProvider()())
                assertEquals(listOf("sizingX"), childView.events)
                view.evaluateActualSizeX()
                assertEquals(96, childView.evaluateActualSizeX())
                view.evaluateActualSizeY()
            } else {
                assertEquals(191, childView.getStoredRegionSizeYProvider()())
                assertEquals(listOf("sizingY"), childView.events)
                view.evaluateActualSizeY()
                assertEquals(191, childView.evaluateActualSizeY())
                view.evaluateActualSizeX()
            }

            assertEquals(IntPoint(96, 191), childView.evaluateActualSize())
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
        }

        view.evaluateContentSizeX()
        view.evaluateContentSizeY()
        view.evaluateActualSizeX()
        view.evaluateActualSizeY()

        listOf(childView, otherChildView).forEach {
            assertEquals(1, it.initializationCount)
            assertEquals(IntPoint(10, 20), it.evaluateContentSize())
            assertEquals(IntPoint(10, 20), it.evaluateActualSize())
        }
    }

    @Test
    fun `X成分はY成分を計算せず同期的に確定するのだ`() {
        val view = RecordingActualSizeView()
        view.initSizeCalculation(TEST_RENDERING_PROXY, { 100 }, { 100 })
        view.evaluateContentSizeX()
        view.evaluateContentSizeY()

        view.evaluateActualSizeX()
        assertEquals(listOf("sizingX"), view.events)
        assertEquals(10, view.evaluateActualSizeX())
        assertEquals(listOf("sizingX"), view.events)
    }

    @Test
    fun `Y成分はX成分を計算せず同期的に確定するのだ`() {
        val view = RecordingActualSizeView()
        view.initSizeCalculation(TEST_RENDERING_PROXY, { 100 }, { 100 })
        view.evaluateContentSizeX()
        view.evaluateContentSizeY()

        view.evaluateActualSizeY()
        assertEquals(listOf("sizingY"), view.events)
        assertEquals(20, view.evaluateActualSizeY())
        assertEquals(listOf("sizingY"), view.events)
    }

    @Test
    fun `FILLはどちらの軸からでも渡された領域で確定するのだ`() {
        listOf(true, false).forEach { xFirst ->
            val view = RecordingActualSizeView()
            view.sizingX = Sizing.FILL
            view.sizingY = Sizing.FILL
            view.initSizeCalculation(TEST_RENDERING_PROXY, { 120 }, { 500 })

            if (xFirst) {
                view.evaluateActualSizeX()
                assertEquals(listOf("sizingX"), view.events)
                assertEquals(120, view.evaluateActualSizeX())
                view.evaluateActualSizeY()
            } else {
                view.evaluateActualSizeY()
                assertEquals(listOf("sizingY"), view.events)
                assertEquals(500, view.evaluateActualSizeY())
                view.evaluateActualSizeX()
            }
            val expectedEvents = if (xFirst) listOf("sizingX", "sizingY") else listOf("sizingY", "sizingX")
            assertEquals(expectedEvents, view.events)
            assertEquals(120, view.evaluateActualSizeX())
            assertEquals(500, view.evaluateActualSizeY())
            assertEquals(IntPoint(120, 500), view.evaluateActualSize())
            assertEquals(expectedEvents, view.events)
        }
    }

    @Test
    fun `WRAPとFILLの組合せを保って各軸の評価結果を再利用するのだ`() {
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

                view.evaluateActualSizeX()
                view.evaluateActualSizeY()

                assertEquals(listOf("sizingX", "sizingY"), view.events)
                assertEquals(expectedX, view.evaluateActualSizeX())
                assertEquals(expectedY, view.evaluateActualSizeY())
                assertEquals(listOf("sizingX", "sizingY"), view.events)
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

        stackView.evaluateActualSizeX()
        assertEquals(95, childView.evaluateActualSizeX())
        assertEquals(10, otherChildView.evaluateActualSizeX())
        assertEquals(listOf("sizingX", "sizingX"), childView.events)
        assertEquals(listOf("sizingX"), otherChildView.events)

        stackView.evaluateActualSizeY()

        assertEquals(100, stackView.evaluateActualSizeX())
        assertEquals(200, stackView.evaluateActualSizeY())
        assertEquals(100, marginView.evaluateActualSizeX())
        assertEquals(200, marginView.evaluateActualSizeY())
        assertEquals(95, childView.evaluateActualSizeX())
        assertEquals(188, childView.evaluateActualSizeY())
        assertEquals(listOf("sizingX", "sizingX", "sizingY", "sizingY"), childView.events)
        assertEquals(10, otherChildView.evaluateActualSizeX())
        assertEquals(20, otherChildView.evaluateActualSizeY())
        assertEquals(listOf("sizingX", "sizingY"), otherChildView.events)
    }

    @Test
    fun `ラッパーはどちらの軸からでも子へ割り当て領域を供給するのだ`() {
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
                view.evaluateActualSizeX()
                assertEquals(101, childView.evaluateActualSizeX())
                view.evaluateActualSizeY()
            } else {
                view.evaluateActualSizeY()
                assertEquals(203, childView.evaluateActualSizeY())
                view.evaluateActualSizeX()
            }
            val expectedEvents = if (xFirst) listOf("sizingX", "sizingX", "sizingY", "sizingY") else listOf("sizingY", "sizingY", "sizingX", "sizingX")
            assertEquals(IntPoint(101, 203), childView.evaluateActualSize())
            assertEquals(expectedEvents, childView.events)
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
                    view.evaluateActualSizeX()
                    childViews.forEachIndexed { index, childView ->
                        assertEquals(expectedSizes[index].x, childView.evaluateActualSizeX())
                    }
                    view.evaluateActualSizeY()
                } else {
                    view.evaluateActualSizeY()
                    childViews.forEachIndexed { index, childView ->
                        assertEquals(expectedSizes[index].y, childView.evaluateActualSizeY())
                    }
                    view.evaluateActualSizeX()
                }
                childViews.forEachIndexed { index, childView ->
                    val expectedSize = expectedSizes[index]
                    val expectedEvents = if (xFirst) listOf("sizingX", "sizingY") else listOf("sizingY", "sizingX")
                    assertEquals(expectedSize, childView.evaluateActualSize())
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
                        view.evaluateActualSizeX()
                        view.evaluateActualSizeY()
                    } else {
                        view.evaluateActualSizeY()
                        view.evaluateActualSizeX()
                    }

                    val expectedSizes = if (horizontal) listOf(IntPoint(10, 20), IntPoint(26, 203), IntPoint(10, 20), IntPoint(25, 203)) else listOf(IntPoint(10, 20), IntPoint(101, 51), IntPoint(10, 20), IntPoint(101, 51))
                    assertEquals(expectedSizes, childViews.map { it.evaluateActualSize() })
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
            view.evaluateActualSizeX()
            view.evaluateActualSizeY()

            val providers = if (horizontal) listOf(firstChildView.getStoredRegionSizeXProvider(), secondChildView.getStoredRegionSizeXProvider()) else listOf(firstChildView.getStoredRegionSizeYProvider(), secondChildView.getStoredRegionSizeYProvider())
            val expectedRegions = if (horizontal) listOf(50, 51, 50, 51) else listOf(101, 102, 101, 102)
            assertEquals(expectedRegions, listOf(1, 0, 1, 0).map { providers[it]() })
            assertEquals(IntPoint(10, 20), firstChildView.evaluateActualSize())
            assertEquals(if (horizontal) IntPoint(50, 203) else IntPoint(101, 101), secondChildView.evaluateActualSize())
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
                view.evaluateActualSizeX()
                view.evaluateActualSizeY()
            }
            val childViews = mutableListOf(addChild(), addChild())
            calculate()
            val expectedInitialSizes = if (horizontal) listOf(IntPoint(51, 203), IntPoint(50, 203)) else listOf(IntPoint(101, 102), IntPoint(101, 101))
            assertEquals(expectedInitialSizes, childViews.map { it.evaluateActualSize() })

            childViews += addChild()
            calculate()

            val expectedSizes = if (horizontal) listOf(IntPoint(34, 203), IntPoint(33, 203), IntPoint(34, 203)) else listOf(IntPoint(101, 68), IntPoint(101, 67), IntPoint(101, 68))
            assertEquals(expectedSizes, childViews.map { it.evaluateActualSize() })
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
                view.evaluateActualSizeX()
                childViews.forEachIndexed { index, childView ->
                    assertEquals(expectedSizes[index].x, childView.evaluateActualSizeX())
                }
                view.evaluateActualSizeY()
            } else {
                view.evaluateActualSizeY()
                childViews.forEachIndexed { index, childView ->
                    assertEquals(expectedSizes[index].y, childView.evaluateActualSizeY())
                }
                view.evaluateActualSizeX()
            }
            childViews.forEachIndexed { index, childView ->
                assertEquals(expectedSizes[index], childView.evaluateActualSize())
                assertEquals(if (xFirst) listOf("sizingX", "sizingY") else listOf("sizingY", "sizingX"), childView.events)
            }
        }
    }

}
