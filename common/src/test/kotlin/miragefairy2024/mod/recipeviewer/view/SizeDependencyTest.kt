package miragefairy2024.mod.recipeviewer.view

import miragefairy2024.mod.recipeviewer.views.AbstractView
import miragefairy2024.mod.recipeviewer.views.AbsoluteView
import miragefairy2024.mod.recipeviewer.views.Child
import miragefairy2024.mod.recipeviewer.views.ListView
import miragefairy2024.mod.recipeviewer.views.MarginView
import miragefairy2024.mod.recipeviewer.views.StackView
import miragefairy2024.mod.recipeviewer.views.XListView
import miragefairy2024.mod.recipeviewer.views.YListView
import net.minecraft.network.chat.FormattedText
import net.minecraft.util.FormattedCharSequence
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

private val TEST_RENDERING_PROXY: RenderingProxy = object : RenderingProxy {
    override fun calculateTextWidth(text: FormattedText) = throw UnsupportedOperationException()
    override fun calculateTextWidth(text: FormattedCharSequence) = throw UnsupportedOperationException()
    override fun getTextHeight() = throw UnsupportedOperationException()
    override fun wrapText(text: FormattedText, maxWidth: Int): List<FormattedCharSequence> = throw UnsupportedOperationException()
}

private class DependentSizeView(
    private val contentSizeXFunction: DependentSizeView.() -> Int,
    private val contentSizeYFunction: DependentSizeView.() -> Int,
) : AbstractView() {
    override var sizingX = Sizing.WRAP
    override var sizingY = Sizing.WRAP

    var contentSizeXCalculationCount = 0
        private set

    var contentSizeYCalculationCount = 0
        private set

    fun readRegionSizeX() = regionSizeXProvider()
    fun readRegionSizeY() = regionSizeYProvider()

    override fun evaluateContentSizeXImpl(): Int {
        contentSizeXCalculationCount += 1
        return contentSizeXFunction()
    }

    override fun evaluateContentSizeYImpl(): Int {
        contentSizeYCalculationCount += 1
        return contentSizeYFunction()
    }

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = ViewController.NONE
}

private fun createWrappingView(rotated: Boolean): DependentSizeView {
    return DependentSizeView(
        {
            if (rotated) {
                val height = readRegionSizeY()
                (120 + height - 1) / height * 9
            } else {
                0
            }
        },
        {
            if (rotated) {
                0
            } else {
                val width = readRegionSizeX()
                (120 + width - 1) / width * 9
            }
        },
    ).also {
        it.sizingX = if (rotated) Sizing.WRAP else Sizing.FILL
        it.sizingY = if (rotated) Sizing.FILL else Sizing.WRAP
    }
}

class SizeDependencyTest {

    @Test
    fun `逆軸の割り当て領域と計算済みの実寸は計算順序に依存せず取得できるのだ`() {
        listOf(true, false).forEach { rotated ->
            listOf(true, false).forEach { xFirst ->
                val view = if (rotated) YListView() else XListView()
                view.sizingX = Sizing.FILL
                view.sizingY = Sizing.FILL
                val childViews = List(2) {
                    createWrappingView(rotated).also { childView ->
                        view.add(Child(ListView.Position().also { position -> position.weight = 1.0 }, childView))
                    }
                }
                view.initSizeCalculation(TEST_RENDERING_PROXY, { 100 }, { 200 })
                childViews.forEach {
                    assertEquals(0, it.contentSizeXCalculationCount)
                    assertEquals(0, it.contentSizeYCalculationCount)
                }

                if (xFirst) {
                    view.evaluateContentSizeX()
                    view.evaluateContentSizeY()
                } else {
                    view.evaluateContentSizeY()
                    view.evaluateContentSizeX()
                }

                assertEquals(if (rotated) IntPoint(18, 0) else IntPoint(0, 27), view.evaluateContentSize())
                if (rotated) {
                    assertEquals(200, view.evaluateActualSizeY())
                } else {
                    assertEquals(100, view.evaluateActualSizeX())
                }
                childViews.forEach {
                    assertEquals(1, it.contentSizeXCalculationCount)
                    assertEquals(1, it.contentSizeYCalculationCount)
                }

                childViews.forEach {
                    assertEquals(if (rotated) IntPoint(18, 100) else IntPoint(50, 27), it.evaluateActualSize())
                    assertEquals(1, it.contentSizeXCalculationCount)
                    assertEquals(1, it.contentSizeYCalculationCount)
                }
            }
        }
    }

    @Test
    fun `マージンを通した領域の取得は親の必要な軸だけを計算するのだ`() {
        listOf(true, false).forEach { xFirst ->
            val childView = createWrappingView(false)
            val marginView = MarginView(2, 3, 5, 7)
            marginView.add(Child(Unit, childView))
            val view = StackView()
            view.sizingX = Sizing.FILL
            view.sizingY = Sizing.FILL
            view.add(Child(Unit, marginView))
            var regionSizeXCalls = 0
            var regionSizeYCalls = 0
            view.initSizeCalculation(TEST_RENDERING_PROXY, {
                regionSizeXCalls += 1
                101
            }, {
                regionSizeYCalls += 1
                203
            })

            if (xFirst) {
                view.evaluateContentSizeX()
                view.evaluateContentSizeY()
            } else {
                view.evaluateContentSizeY()
                view.evaluateContentSizeX()
            }

            assertEquals(IntPoint(5, 30), view.evaluateContentSize())
            assertEquals(IntPoint(0, 18), childView.evaluateContentSize())
            assertEquals(96, childView.readRegionSizeX())
            assertEquals(96, childView.readRegionSizeX())
            assertEquals(1, regionSizeXCalls)
            assertEquals(0, regionSizeYCalls)
            listOf(view, marginView).forEach {
                assertEquals(101, it.evaluateActualSizeX())
            }

            view.evaluateActualSizeY()
            assertEquals(IntPoint(96, 18), childView.evaluateActualSize())
            assertEquals(1, regionSizeXCalls)
            assertEquals(1, regionSizeYCalls)
        }
    }

    @Test
    fun `Offsetの領域取得は親の割り当て領域を要求しないのだ`() {
        val childView = DependentSizeView({ 10 }, { readRegionSizeX() + 1 })
        val view = AbsoluteView(IntPoint(100, 80))
        view.sizingX = Sizing.FILL
        view.sizingY = Sizing.FILL
        view.add(Child(AbsoluteView.Offset(IntPoint.ZERO), childView))
        view.initSizeCalculation(TEST_RENDERING_PROXY, { throw UnsupportedOperationException() }, { throw UnsupportedOperationException() })

        assertEquals(11, childView.evaluateContentSizeY())
        assertEquals(IntPoint(10, 11), childView.evaluateContentSize())
    }

    @Test
    fun `再初期化で内容と領域と実寸のキャッシュを更新するのだ`() {
        val view = XListView()
        view.sizingX = Sizing.FILL
        view.sizingY = Sizing.FILL
        val childViews = List(2) {
            createWrappingView(false).also { childView ->
                view.add(Child(ListView.Position().also { position -> position.weight = 1.0 }, childView))
            }
        }
        view.initSizeCalculation(TEST_RENDERING_PROXY, { 100 }, { 200 })
        assertEquals(27, view.evaluateContentSizeY())

        view.initSizeCalculation(TEST_RENDERING_PROXY, { 60 }, { 200 })
        assertEquals(36, view.evaluateContentSizeY())

        childViews.forEach {
            assertEquals(IntPoint(30, 36), it.evaluateActualSize())
            assertEquals(2, it.contentSizeXCalculationCount)
            assertEquals(2, it.contentSizeYCalculationCount)
        }
    }

    @Test
    fun `リストの配分が失敗しても再取得でき再初期化で配分を破棄するのだ`() {
        listOf(true, false).forEach { horizontal ->
            val view = if (horizontal) XListView() else YListView()
            view.sizingX = Sizing.FILL
            view.sizingY = Sizing.FILL
            val positions = List(2) { ListView.Position().also { it.weight = 1.0 } }
            val childViews = positions.map {
                DependentSizeView({ 0 }, { 0 }).also { childView ->
                    view.add(Child(it, childView))
                }
            }
            var regionAvailable = false
            view.initSizeCalculation(TEST_RENDERING_PROXY, {
                if (!regionAvailable) throw UnsupportedOperationException()
                101
            }, {
                if (!regionAvailable) throw UnsupportedOperationException()
                203
            })
            fun evaluateRegionSize(index: Int) = if (horizontal) childViews[index].readRegionSizeX() else childViews[index].readRegionSizeY()

            assertThrows(UnsupportedOperationException::class.java) { evaluateRegionSize(1) }
            regionAvailable = true
            val expectedInitialSizes = if (horizontal) listOf(51, 50) else listOf(102, 101)
            assertEquals(expectedInitialSizes, childViews.indices.map { evaluateRegionSize(it) })
            assertEquals(expectedInitialSizes.reversed(), childViews.indices.reversed().map { evaluateRegionSize(it) })

            positions[1].weight = 3.0
            view.initSizeCalculation(TEST_RENDERING_PROXY, { 60 }, { 80 })

            val expectedSizes = if (horizontal) listOf(15, 45) else listOf(20, 60)
            assertEquals(expectedSizes, childViews.indices.map { evaluateRegionSize(it) })
            assertEquals(expectedSizes.reversed(), childViews.indices.reversed().map { evaluateRegionSize(it) })
            if (horizontal) {
                assertEquals(60, view.evaluateActualSizeX())
            } else {
                assertEquals(80, view.evaluateActualSizeY())
            }
        }
    }

    @Test
    fun `親子のコンテンツサイズと領域が循環する配置を検出するのだ`() {
        listOf(true, false).forEach { xFirst ->
            val childView = DependentSizeView({ readRegionSizeY() + 1 }, { readRegionSizeX() + 1 })
            val view = StackView()
            view.add(Child(Unit, childView))
            view.initSizeCalculation(TEST_RENDERING_PROXY, { throw UnsupportedOperationException() }, { throw UnsupportedOperationException() })

            val exception = assertThrows(IllegalStateException::class.java) {
                if (xFirst) view.evaluateContentSizeX() else view.evaluateContentSizeY()
            }

            assertTrue(exception.message!!.contains("Recursive evaluation of CachingEvaluator"))
            assertThrows(IllegalStateException::class.java) { view.evaluateActualSizeX() }
            assertThrows(IllegalStateException::class.java) { view.evaluateActualSizeY() }
        }
    }

}
