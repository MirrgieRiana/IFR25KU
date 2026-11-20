package miragefairy2024.mod.recipeviewer.view

import miragefairy2024.mod.recipeviewer.views.AlignView
import miragefairy2024.mod.recipeviewer.views.FixedWidgetView
import miragefairy2024.mod.recipeviewer.views.plusAssign
import net.minecraft.network.chat.FormattedText
import net.minecraft.util.FormattedCharSequence
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

private val ALIGN_VIEW_RENDERING_PROXY: RenderingProxy = object : RenderingProxy {
    override fun calculateTextWidth(text: FormattedText) = throw UnsupportedOperationException()
    override fun calculateTextWidth(text: FormattedCharSequence) = throw UnsupportedOperationException()
    override fun getTextHeight() = throw UnsupportedOperationException()
    override fun wrapText(text: FormattedText, maxWidth: Int): List<FormattedCharSequence> = throw UnsupportedOperationException()
}

class AlignViewTest {

    @Test
    fun `横と縦で個別に先頭中央末尾へ整列するのだ`() {
        listOf(
            Triple(Alignment.START, Alignment.END, IntRectangle(7, 72, 20, 20)),
            Triple(Alignment.CENTER, Alignment.CENTER, IntRectangle(47, 41, 20, 20)),
            Triple(Alignment.END, Alignment.START, IntRectangle(88, 11, 20, 20)),
        ).forEach { (alignmentX, alignmentY, expectedBounds) ->
            val view = AlignView()
            view.alignmentX = alignmentX
            view.alignmentY = alignmentY
            view += object : FixedWidgetView(IntPoint(20, 20)) {}
            view.initSizeCalculation(ALIGN_VIEW_RENDERING_PROXY, { 101 }, { 81 })

            val bounds = mutableListOf<IntRectangle>()
            val controller = view.attachTo(IntPoint(7, 11)) { _, rectangle ->
                bounds += rectangle
                ViewController.NONE
            }
            assertEquals(listOf(expectedBounds), bounds)
            controller.dispose()
            controller.detach()
        }
    }

    @Test
    fun `縦に領域を埋める子を横中央に配置し移動時も相対位置を保つのだ`() {
        val view = AlignView()
        view.alignmentX = Alignment.CENTER
        view += object : FixedWidgetView(IntPoint(20, 20)) {
            override val sizingY = Sizing.FILL
        }
        view.initSizeCalculation(ALIGN_VIEW_RENDERING_PROXY, { 100 }, { 80 })

        val bounds = mutableListOf<IntRectangle>()
        val offsets = mutableListOf<IntPoint>()
        val events = mutableListOf<String>()
        val controller = view.attachTo(IntPoint(7, 11)) { _, rectangle ->
            bounds += rectangle
            object : ViewController {
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
        controller.setOffset(IntPoint(17, 21))
        controller.dispose()
        controller.detach()

        assertEquals(listOf(IntRectangle(47, 11, 20, 80)), bounds)
        assertEquals(listOf(IntPoint(57, 21)), offsets)
        assertEquals(listOf("dispose", "detach"), events)
    }

}
