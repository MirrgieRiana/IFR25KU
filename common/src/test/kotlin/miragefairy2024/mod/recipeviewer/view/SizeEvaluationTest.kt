package miragefairy2024.mod.recipeviewer.view

import miragefairy2024.mod.recipeviewer.views.AbstractView
import miragefairy2024.mod.recipeviewer.views.AbsoluteView
import miragefairy2024.mod.recipeviewer.views.Child
import miragefairy2024.mod.recipeviewer.views.ListView
import miragefairy2024.mod.recipeviewer.views.StackView
import miragefairy2024.mod.recipeviewer.views.XListView
import miragefairy2024.mod.recipeviewer.views.YListView
import miragefairy2024.util.Remover
import net.minecraft.network.chat.FormattedText
import net.minecraft.util.FormattedCharSequence
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

private val TEST_RENDERING_PROXY: RenderingProxy = object : RenderingProxy {
    override fun calculateTextWidth(text: FormattedText) = throw UnsupportedOperationException()
    override fun calculateTextWidth(text: FormattedCharSequence) = throw UnsupportedOperationException()
    override fun getTextHeight() = throw UnsupportedOperationException()
    override fun wrapText(text: FormattedText, maxWidth: Int): List<FormattedCharSequence> = throw UnsupportedOperationException()
}

private class SizeEvaluationView(
    private val contentSizeXFunction: SizeEvaluationView.() -> Int,
    private val contentSizeYFunction: SizeEvaluationView.() -> Int,
) : AbstractView() {
    override var sizingX = Sizing.WRAP
    override var sizingY = Sizing.WRAP

    var contentSizeXCalculationCount = 0
        private set

    var contentSizeYCalculationCount = 0
        private set

    fun evaluateRegionSizeX() = regionSizeXProvider()
    fun evaluateRegionSizeY() = regionSizeYProvider()

    override fun evaluateOwnContentSizeX(): Int {
        contentSizeXCalculationCount += 1
        return contentSizeXFunction()
    }

    override fun evaluateOwnContentSizeY(): Int {
        contentSizeYCalculationCount += 1
        return contentSizeYFunction()
    }

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = Remover.NONE
}

class SizeEvaluationTest {

    @Test
    fun `公開APIはサイズプロパティやcalculateではなくevaluateを提供するのだ`() {
        val methods = View::class.java.methods.map { it.name }.toSet()
        listOf("ContentSizeX", "ContentSizeY", "ActualSizeX", "ActualSizeY").forEach { suffix ->
            assertTrue("evaluate$suffix" in methods)
            assertFalse("get$suffix" in methods)
            assertFalse("calculate$suffix" in methods)
        }
    }

    @Test
    fun `ゼロと負数を含む計算結果を再利用し再初期化で破棄するのだ`() {
        listOf(IntPoint.ZERO, IntPoint(-1, -2)).forEach { size ->
            val view = SizeEvaluationView({ size.x }, { size.y })
            view.initSizeCalculation(TEST_RENDERING_PROXY, { throw UnsupportedOperationException() }, { throw UnsupportedOperationException() })
            assertEquals(0, view.contentSizeXCalculationCount)
            assertEquals(0, view.contentSizeYCalculationCount)

            repeat(2) {
                assertEquals(size, view.evaluateContentSize())
                assertEquals(size, view.evaluateActualSize())
            }
            assertEquals(1, view.contentSizeXCalculationCount)
            assertEquals(1, view.contentSizeYCalculationCount)

            view.initSizeCalculation(TEST_RENDERING_PROXY, { throw UnsupportedOperationException() }, { throw UnsupportedOperationException() })
            assertEquals(size, view.evaluateActualSize())
            assertEquals(2, view.contentSizeXCalculationCount)
            assertEquals(2, view.contentSizeYCalculationCount)
        }
    }

    @Test
    fun `FILLの実サイズは必要な軸の割り当てだけを評価するのだ`() {
        val view = SizeEvaluationView({ throw UnsupportedOperationException() }, { throw UnsupportedOperationException() })
        view.sizingX = Sizing.FILL
        view.sizingY = Sizing.FILL
        var regionSizeXCalls = 0
        var regionSizeYCalls = 0
        view.initSizeCalculation(TEST_RENDERING_PROXY, {
            regionSizeXCalls += 1
            100
        }, {
            regionSizeYCalls += 1
            200
        })

        repeat(2) {
            assertEquals(100, view.evaluateActualSizeX())
        }
        assertEquals(1, regionSizeXCalls)
        assertEquals(0, regionSizeYCalls)
        assertEquals(200, view.evaluateActualSizeY())
        assertEquals(1, regionSizeYCalls)
        assertEquals(0, view.contentSizeXCalculationCount)
        assertEquals(0, view.contentSizeYCalculationCount)

        view.initSizeCalculation(TEST_RENDERING_PROXY, { 60 }, { 80 })
        assertEquals(IntPoint(60, 80), view.evaluateActualSize())
    }

    @Test
    fun `親の実サイズ評価は未要求の子の実サイズへ伝播しないのだ`() {
        listOf(true, false).forEach { rotated ->
            val firstChild = if (rotated) SizeEvaluationView({ evaluateRegionSizeY() / 5 }, { 0 }) else SizeEvaluationView({ 0 }, { evaluateRegionSizeX() / 5 })
            firstChild.sizingX = if (rotated) Sizing.WRAP else Sizing.FILL
            firstChild.sizingY = if (rotated) Sizing.FILL else Sizing.WRAP
            val secondChild = if (rotated) SizeEvaluationView({ 0 }, { evaluateRegionSizeX() / 2 }) else SizeEvaluationView({ evaluateRegionSizeY() / 2 }, { 0 })
            val view = StackView()
            view.sizingX = if (rotated) Sizing.WRAP else Sizing.FILL
            view.sizingY = if (rotated) Sizing.FILL else Sizing.WRAP
            view.add(Child(Unit, firstChild))
            view.add(Child(Unit, secondChild))
            view.initSizeCalculation(TEST_RENDERING_PROXY, { 100 }, { 200 })

            assertEquals(if (rotated) 40 else 20, if (rotated) view.evaluateContentSizeX() else view.evaluateContentSizeY())
            assertEquals(0, if (rotated) secondChild.contentSizeYCalculationCount else secondChild.contentSizeXCalculationCount)
            assertEquals(if (rotated) IntPoint(40, 200) else IntPoint(100, 20), view.evaluateActualSize())
            assertEquals(if (rotated) IntPoint(40, 200) else IntPoint(100, 20), firstChild.evaluateActualSize())
            assertEquals(if (rotated) IntPoint(0, 20) else IntPoint(10, 0), secondChild.evaluateActualSize())
        }
    }

    @Test
    fun `絶対配置は使わないサイズを引数の評価で要求しないのだ`() {
        listOf<Pair<AbsoluteView.Position, IntPoint>>(
            Pair(AbsoluteView.Fill, IntPoint(81, 102)),
            Pair(AbsoluteView.Bounds(IntRectangle(0, 0, 20, 10)), IntPoint(11, 22)),
        ).forEach { (position, expectedSize) ->
            listOf(true, false).forEach { xFirst ->
                val child = SizeEvaluationView({ evaluateRegionSizeY() + 1 }, { evaluateRegionSizeX() + 2 })
                val view = AbsoluteView(IntPoint(100, 80))
                view.add(Child(position, child))
                view.initSizeCalculation(TEST_RENDERING_PROXY, { throw UnsupportedOperationException() }, { throw UnsupportedOperationException() })

                if (xFirst) {
                    assertEquals(expectedSize.x, child.evaluateContentSizeX())
                } else {
                    assertEquals(expectedSize.y, child.evaluateContentSizeY())
                }
                assertEquals(expectedSize, child.evaluateContentSize())
                assertEquals(expectedSize, child.evaluateActualSize())
            }
        }
    }

    @Test
    fun `余剰領域を使わない子の評価は不要な全体配分へ依存しないのだ`() {
        listOf(true, false).forEach { horizontal ->
            listOf(true, false).forEach { xFirst ->
                listOf(Pair(Sizing.WRAP, 1.0), Pair(Sizing.FILL, 0.0), Pair(Sizing.FILL, 1.0)).forEach { (sizing, secondWeight) ->
                    val view = if (horizontal) XListView() else YListView()
                    view.sizingX = if (horizontal) sizing else Sizing.WRAP
                    view.sizingY = if (horizontal) Sizing.WRAP else sizing
                    val firstChild = if (horizontal) SizeEvaluationView({ 10 }, { evaluateRegionSizeX() + 1 }) else SizeEvaluationView({ evaluateRegionSizeY() + 1 }, { 10 })
                    val secondChild = if (horizontal) SizeEvaluationView({ evaluateRegionSizeY() + 1 }, { 20 }) else SizeEvaluationView({ 20 }, { evaluateRegionSizeX() + 1 })
                    view.add(Child(ListView.Position().also { it.weight = if (sizing == Sizing.WRAP) 1.0 else 0.0 }, firstChild))
                    view.add(Child(ListView.Position().also { it.weight = secondWeight }, secondChild))
                    view.initSizeCalculation(TEST_RENDERING_PROXY, { throw UnsupportedOperationException() }, { throw UnsupportedOperationException() })

                    if (xFirst) {
                        view.evaluateContentSizeX()
                        view.evaluateContentSizeY()
                    } else {
                        view.evaluateContentSizeY()
                        view.evaluateContentSizeX()
                    }
                    assertEquals(if (horizontal) IntPoint(31, 20) else IntPoint(20, 31), view.evaluateContentSize())
                }
            }
        }
    }

    @Test
    fun `循環依存を検出して失敗した評価を再試行できるのだ`() {
        var cyclic = true
        val view = SizeEvaluationView({ if (cyclic) evaluateContentSizeY() + 1 else 10 }, { if (cyclic) evaluateContentSizeX() + 1 else 20 })
        view.initSizeCalculation(TEST_RENDERING_PROXY, { throw UnsupportedOperationException() }, { throw UnsupportedOperationException() })

        val exception: IllegalStateException = assertThrows(IllegalStateException::class.java) { view.evaluateContentSizeX() }
        assertTrue(exception.message!!.contains("Recursive evaluation of CachingEvaluator"))
        assertThrows(IllegalStateException::class.java) { view.evaluateContentSizeY() }

        cyclic = false
        assertEquals(IntPoint(10, 20), view.evaluateContentSize())
    }

    @Test
    fun `実サイズの割り当てが循環しても評価状態を復旧するのだ`() {
        val child = SizeEvaluationView({ 0 }, { 0 })
        val view = StackView()
        view.sizingX = Sizing.FILL
        view.sizingY = Sizing.FILL
        view.add(Child(Unit, child))
        view.initSizeCalculation(TEST_RENDERING_PROXY, { child.evaluateRegionSizeX() }, { child.evaluateRegionSizeY() })

        repeat(2) {
            assertThrows(IllegalStateException::class.java) { view.evaluateActualSizeX() }
            assertThrows(IllegalStateException::class.java) { view.evaluateActualSizeY() }
        }

        view.initSizeCalculation(TEST_RENDERING_PROXY, { 100 }, { 200 })
        assertEquals(IntPoint(100, 200), view.evaluateActualSize())
    }

    @Test
    fun `計算が失敗した場合はキャッシュせず次回に再試行するのだ`() {
        var failing = true
        val view = SizeEvaluationView({
            if (failing) throw UnsupportedOperationException()
            10
        }, { 20 })
        view.initSizeCalculation(TEST_RENDERING_PROXY, { throw UnsupportedOperationException() }, { throw UnsupportedOperationException() })

        assertThrows(UnsupportedOperationException::class.java) { view.evaluateContentSizeX() }
        failing = false
        assertEquals(10, view.evaluateContentSizeX())
        assertEquals(10, view.evaluateContentSizeX())
        assertEquals(2, view.contentSizeXCalculationCount)
    }

}
