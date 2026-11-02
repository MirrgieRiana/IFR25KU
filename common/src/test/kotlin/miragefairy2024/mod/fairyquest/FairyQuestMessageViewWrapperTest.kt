package miragefairy2024.mod.fairyquest

import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.evaluateActualSize
import miragefairy2024.mod.recipeviewer.view.offset
import miragefairy2024.mod.recipeviewer.views.MultiLineTextView
import miragefairy2024.mod.recipeviewer.views.NinePatchImageView
import miragefairy2024.mod.recipeviewer.views.TextButtonView
import miragefairy2024.mod.recipeviewer.views.YScrollView
import miragefairy2024.util.fire
import miragefairy2024.util.invoke
import miragefairy2024.util.register
import miragefairy2024.util.text
import mirrg.kotlin.helium.atLeast
import net.minecraft.network.chat.FormattedText
import net.minecraft.network.chat.Style
import net.minecraft.util.FormattedCharSequence
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

private class MessageRenderingProxy : RenderingProxy {
    val widths = mutableListOf<Int>()

    override fun calculateTextWidth(text: FormattedText) = text.string.length * 6
    override fun calculateTextWidth(text: FormattedCharSequence) = throw UnsupportedOperationException()
    override fun getTextHeight() = 9

    override fun wrapText(text: FormattedText, maxWidth: Int): List<FormattedCharSequence> {
        widths += maxWidth
        return text.string.split('\n').flatMap { line ->
            line.chunked(maxWidth / 6 atLeast 1).map { FormattedCharSequence.forward(it, Style.EMPTY) }
        }
    }
}

private fun FairyQuestMessageViewWrapper.place(): List<Pair<PlaceableView, IntRectangle>> {
    val placements = mutableListOf<Pair<PlaceableView, IntRectangle>>()
    fun place(view: PlaceableView, bounds: IntRectangle): ViewController {
        placements += Pair(view, bounds)
        if (view is YScrollView) return view.childView.attachTo(bounds.offset, ::place)
        return ViewController.NONE
    }
    this.view.attachTo(IntPoint.ZERO, ::place)
    return placements
}

class FairyQuestMessageViewWrapperTest {

    @Test
    fun `長文をカード幅で折り返し本文と題名と依頼人を一緒にスクロールするのだ`() {
        val proxy = MessageRenderingProxy()
        val view = FairyQuestMessageViewWrapper(text { "title"() }, text { "message".repeat(200)() }, text { "client"() })
        view.view.initSizeCalculation(proxy, { 400 }, { 240 })

        Assertions.assertEquals(IntPoint(400, 240), view.view.evaluateActualSize())
        val placements = view.place()
        val scroll = placements.single { it.first is YScrollView }
        Assertions.assertEquals(IntRectangle(75, 10, 260, 196), scroll.second)
        val scrollView = scroll.first as YScrollView
        Assertions.assertTrue(scrollView.childView.evaluateActualSizeY() > scroll.second.sizeY)
        Assertions.assertEquals(250, scrollView.childView.evaluateActualSizeX())
        Assertions.assertEquals(10, scrollView.scrollbarThickness)
        Assertions.assertEquals(0xA0FFFFFF.toInt(), scrollView.scrollbarColor)

        val texts = placements.mapNotNull { it.first as? MultiLineTextView }
        Assertions.assertEquals(listOf("title", "message".repeat(200), "client"), texts.map { it.text.value.string })
        Assertions.assertEquals(listOf(Alignment.CENTER, Alignment.START, Alignment.END), texts.map { it.alignmentX })
        Assertions.assertTrue(texts.all { !it.shadow && it.lineSpacing == 2 && it.color.lightModeArgb == 0xFF6B472E.toInt() })
        Assertions.assertEquals(listOf(228, 228, 228), proxy.widths)
        val backgrounds = placements.filter { it.first is NinePatchImageView }
        Assertions.assertEquals(250, backgrounds[0].second.sizeX)
        Assertions.assertEquals(228, backgrounds[1].second.sizeX)
    }

    @Test
    fun `再初期化すると画面の寸法に合わせてスクロール領域とボタンを再配置するのだ`() {
        val view = FairyQuestMessageViewWrapper(text { "title"() }, text { "message"() }, text { "client"() })
        listOf(IntPoint(400, 240), IntPoint(320, 180)).forEach { size ->
            view.view.initSizeCalculation(MessageRenderingProxy(), { size.x }, { size.y })
            val placements = view.place()
            val scroll = placements.single { it.first is YScrollView }.second
            Assertions.assertEquals(IntRectangle((size.x - 270) / 2 + 10, 10, 260, size.y - 44), scroll)
            val buttons = placements.filter { it.first is TextButtonView }.map { it.second }
            Assertions.assertEquals(listOf(IntRectangle((size.x - 180) / 2, size.y - 24, 80, 20), IntRectangle((size.x - 180) / 2 + 100, size.y - 24, 80, 20)), buttons)
        }
    }

    @Test
    fun `二つのボタンがゲーム復帰と親画面復帰を別々に通知するのだ`() {
        val view = FairyQuestMessageViewWrapper(text { "title"() }, text { "message"() }, text { "client"() })
        val events = mutableListOf<String>()
        view.onBackToGame.register {
            events += "game"
        }
        view.onBack.register {
            events += "parent"
        }
        view.view.initSizeCalculation(MessageRenderingProxy(), { 400 }, { 240 })
        val buttons = view.place().mapNotNull { it.first as? TextButtonView }

        buttons[0].onPress.fire()
        Assertions.assertEquals(listOf("game"), events)
        buttons[1].onPress.fire()
        Assertions.assertEquals(listOf("game", "parent"), events)
    }

}
