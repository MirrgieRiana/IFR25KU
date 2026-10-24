package miragefairy2024.mod.recipeviewer.view

import miragefairy2024.mod.recipeviewer.views.Child
import miragefairy2024.mod.recipeviewer.views.ListView
import miragefairy2024.mod.recipeviewer.views.MultiLineTextView
import miragefairy2024.mod.recipeviewer.views.XListView
import miragefairy2024.util.Remover
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.FormattedText
import net.minecraft.network.chat.Style
import net.minecraft.util.FormattedCharSequence
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

private class MultiLineRenderingProxy : RenderingProxy {
    var wrapCount = 0
        private set
    val widths = mutableListOf<Int>()

    override fun calculateTextWidth(text: FormattedText) = throw UnsupportedOperationException()
    override fun calculateTextWidth(text: FormattedCharSequence) = throw UnsupportedOperationException()

    override fun getTextHeight() = 9

    override fun wrapText(text: FormattedText, maxWidth: Int): List<FormattedCharSequence> {
        wrapCount += 1
        widths += maxWidth
        return text.string.chunked(maxWidth.coerceAtLeast(1)).map { FormattedCharSequence.forward(it, Style.EMPTY) }
    }
}

class MultiLineTextViewTest {

    @Test
    fun `初期化と横のコンテンツサイズ評価は割り当て幅を要求しないのだ`() {
        val proxy = MultiLineRenderingProxy()
        val view = MultiLineTextView()
        view.text.value = Component.literal("abcdefghij")
        view.initSizeCalculation(proxy, { throw UnsupportedOperationException() }, { throw UnsupportedOperationException() })

        assertEquals(0, view.evaluateContentSizeX())
        assertEquals(0, proxy.wrapCount)
    }

    @Test
    fun `高さと実占有幅の評価順序によらず折り返しを一度だけ計算するのだ`() {
        listOf(true, false).forEach { heightFirst ->
            val proxy = MultiLineRenderingProxy()
            val view = MultiLineTextView()
            view.text.value = Component.literal("abcdefghij")
            view.initSizeCalculation(proxy, { 6 }, { throw UnsupportedOperationException() })

            if (heightFirst) {
                assertEquals(20, view.evaluateContentSizeY())
                assertEquals(6, view.evaluateActualSizeX())
            } else {
                assertEquals(6, view.evaluateActualSizeX())
                assertEquals(20, view.evaluateContentSizeY())
            }

            assertEquals(IntPoint(0, 20), view.evaluateContentSize())
            assertEquals(IntPoint(6, 20), view.evaluateActualSize())
            assertEquals(listOf(6), proxy.widths)
            assertEquals(1, proxy.wrapCount)
            assertSame(view.lines.evaluate(), view.lines.evaluate())
        }
    }

    @Test
    fun `親の整列は文字列の幅で複数行Viewの位置を動かさないのだ`() {
        val proxy = MultiLineRenderingProxy()
        val parent = XListView()
        parent.sizingX = Sizing.FILL
        val children = List(2) {
            MultiLineTextView().also { child ->
                child.text.value = Component.literal("abcde")
                parent.add(Child(ListView.Position().also {
                    it.weight = 1.0
                    it.alignmentX = Alignment.END
                }, child))
            }
        }
        parent.initSizeCalculation(proxy, { 12 }, { throw UnsupportedOperationException() })

        assertEquals(9, parent.evaluateContentSizeY())
        assertEquals(listOf(6, 6), children.map { it.evaluateActualSizeX() })
        val bounds = mutableListOf<IntRectangle>()
        parent.attachTo(IntPoint.ZERO) { _, rectangle ->
            bounds += rectangle
            Remover.NONE
        }

        assertEquals(listOf(0, 6), bounds.map { it.x })
        assertEquals(listOf(6, 6), bounds.map { it.sizeX })
        assertEquals(listOf(6, 6), proxy.widths)
        assertEquals(2, proxy.wrapCount)
    }

    @Test
    fun `再初期化でテキストと割り当て幅と行間隔から行分割とサイズを再計算するのだ`() {
        val proxy = MultiLineRenderingProxy()
        val view = MultiLineTextView()
        view.text.value = Component.literal("abcdefghij")
        view.initSizeCalculation(proxy, { 6 }, { throw UnsupportedOperationException() })
        assertEquals(IntPoint(6, 20), view.evaluateActualSize())

        view.text.value = Component.literal("abcdefgh")
        view.lineSpacing = 4
        view.initSizeCalculation(proxy, { 3 }, { throw UnsupportedOperationException() })
        assertEquals(IntPoint(3, 35), view.evaluateActualSize())

        view.initSizeCalculation(proxy, { 20 }, { throw UnsupportedOperationException() })
        assertEquals(IntPoint(20, 9), view.evaluateActualSize())
        assertEquals(listOf(6, 3, 20), proxy.widths)
        assertEquals(3, proxy.wrapCount)
    }

    @Test
    fun `FILLの実占有幅は折り返しを先に評価せず割り当て幅を返すのだ`() {
        val proxy = MultiLineRenderingProxy()
        val view = MultiLineTextView()
        view.text.value = Component.literal("abcdefghij")
        view.initSizeCalculation(proxy, { 6 }, { throw UnsupportedOperationException() })

        assertEquals(6, view.evaluateActualSizeX())
        assertEquals(0, proxy.wrapCount)
        assertEquals(20, view.evaluateActualSizeY())
        assertEquals(1, proxy.wrapCount)
    }

    @Test
    fun `空のテキストと幅ゼロでも評価できるのだ`() {
        val proxy = MultiLineRenderingProxy()
        val view = MultiLineTextView()
        view.initSizeCalculation(proxy, { 0 }, { throw UnsupportedOperationException() })
        assertEquals(IntPoint.ZERO, view.evaluateActualSize())

        view.text.value = Component.literal("ab")
        view.initSizeCalculation(proxy, { 0 }, { throw UnsupportedOperationException() })
        assertEquals(IntPoint(0, 20), view.evaluateActualSize())
        assertEquals(listOf(0, 0), proxy.widths)
    }

}
