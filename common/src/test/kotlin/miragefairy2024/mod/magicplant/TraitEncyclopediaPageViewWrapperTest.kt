package miragefairy2024.mod.magicplant

import miragefairy2024.MirageFairy2024
import miragefairy2024.mod.NinePatchTextureCard
import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.evaluateActualSize
import miragefairy2024.mod.recipeviewer.view.offset
import miragefairy2024.mod.recipeviewer.views.ImageView
import miragefairy2024.mod.recipeviewer.views.MultiLineTextView
import miragefairy2024.mod.recipeviewer.views.NinePatchImageView
import miragefairy2024.mod.recipeviewer.views.TextButtonView
import miragefairy2024.mod.recipeviewer.views.YScrollContainerView
import miragefairy2024.mod.recipeviewer.views.YScrollbarView
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

private class TraitEncyclopediaRenderingProxy : RenderingProxy {
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

private fun TraitEncyclopediaPageViewWrapper.place(): List<Pair<PlaceableView, IntRectangle>> {
    val placements = mutableListOf<Pair<PlaceableView, IntRectangle>>()
    fun place(view: PlaceableView, bounds: IntRectangle): ViewController {
        placements += Pair(view, bounds)
        if (view is YScrollContainerView) return view.childView.attachTo(bounds.offset, ::place)
        return ViewController.NONE
    }
    this.view.attachTo(IntPoint.ZERO, ::place)
    return placements
}

class TraitEncyclopediaPageViewWrapperTest {

    @Test
    fun `名前とアイコンを固定した幅160のカード内で本文だけを折り返してスクロールするのだ`() {
        val proxy = TraitEncyclopediaRenderingProxy()
        val texture = MirageFairy2024.identifier("textures/gui/traits/test.png")
        val name = text { "name"() }
        val message = text { "message".repeat(200)() }
        val view = TraitEncyclopediaPageViewWrapper(name, texture, message)
        view.view.initSizeCalculation(proxy, { 400 }, { 240 })

        Assertions.assertEquals(IntPoint(400, 240), view.view.evaluateActualSize())
        val placements = view.place()
        val background = placements.single { it.first is NinePatchImageView }
        Assertions.assertEquals(IntRectangle(120, 20, 160, 186), background.second)
        Assertions.assertEquals(NinePatchTextureCard.TRAIT_BACKGROUND.texture, (background.first as NinePatchImageView).textureId)
        val icon = placements.single { it.first is ImageView }
        Assertions.assertEquals(IntRectangle(184, 39, 32, 32), icon.second)
        Assertions.assertEquals(texture, (icon.first as ImageView).texture.id)
        val scroll = placements.single { it.first is YScrollContainerView }
        Assertions.assertEquals(IntRectangle(125, 76, 145, 125), scroll.second)
        val scrollView = scroll.first as YScrollContainerView
        Assertions.assertEquals(145, scrollView.childView.evaluateActualSizeX())
        Assertions.assertTrue(scrollView.childView.evaluateActualSizeY() > scroll.second.sizeY)
        val scrollbar = placements.single { it.first is YScrollbarView }
        Assertions.assertEquals(IntRectangle(270, 76, 5, 125), scrollbar.second)
        val scrollbarView = scrollbar.first as YScrollbarView
        Assertions.assertEquals(5, scrollbarView.scrollbarThickness)
        Assertions.assertEquals(0xA0FFFFFF.toInt(), scrollbarView.scrollbarColor)
        scrollbarView.scrollOffsetY.value = 125.5
        Assertions.assertEquals(125.5, scrollView.scrollOffsetY.value)
        scrollView.scrollOffsetY.value = 250.25
        Assertions.assertEquals(250.25, scrollbarView.scrollOffsetY.value)
        val texts = placements.filter { it.first is MultiLineTextView }
        Assertions.assertEquals(listOf(name, message), texts.map { (it.first as MultiLineTextView).text.value })
        Assertions.assertEquals(listOf(Alignment.CENTER, Alignment.START), texts.map { (it.first as MultiLineTextView).alignmentX })
        Assertions.assertTrue(texts.all { !(it.first as MultiLineTextView).shadow && (it.first as MultiLineTextView).lineSpacing == 2 })
        Assertions.assertEquals(listOf(150, 145), proxy.widths)
    }

    @Test
    fun `再初期化するとカードとスクロール領域とボタンを画面の寸法に合わせるのだ`() {
        val view = TraitEncyclopediaPageViewWrapper(text { "name"() }, MirageFairy2024.identifier("textures/gui/traits/test.png"), text { "message"() })
        listOf(IntPoint(400, 240), IntPoint(320, 180)).forEach { size ->
            view.view.initSizeCalculation(TraitEncyclopediaRenderingProxy(), { size.x }, { size.y })
            val placements = view.place()
            Assertions.assertEquals(IntRectangle((size.x - 160) / 2, 20, 160, size.y - 54), placements.single { it.first is NinePatchImageView }.second)
            Assertions.assertEquals(IntRectangle((size.x - 150) / 2, 76, 145, size.y - 115), placements.single { it.first is YScrollContainerView }.second)
            Assertions.assertEquals(IntRectangle((size.x - 150) / 2 + 145, 76, 5, size.y - 115), placements.single { it.first is YScrollbarView }.second)
            val buttons = placements.filter { it.first is TextButtonView }.map { it.second }
            Assertions.assertEquals(listOf(IntRectangle((size.x - 180) / 2, size.y - 24, 80, 20), IntRectangle((size.x - 180) / 2 + 100, size.y - 24, 80, 20)), buttons)
        }
    }

    @Test
    fun `二つのボタンがゲーム復帰と親画面復帰を別々に通知するのだ`() {
        val view = TraitEncyclopediaPageViewWrapper(text { "name"() }, MirageFairy2024.identifier("textures/gui/traits/test.png"), text { "message"() })
        val events = mutableListOf<String>()
        view.onBackToGame.register {
            events += "game"
            true
        }
        view.onBack.register {
            events += "parent"
            true
        }
        view.view.initSizeCalculation(TraitEncyclopediaRenderingProxy(), { 400 }, { 240 })
        val buttons = view.place().mapNotNull { it.first as? TextButtonView }

        buttons[0].onClick.fire { it() }
        Assertions.assertEquals(listOf("game"), events)
        buttons[1].onClick.fire { it() }
        Assertions.assertEquals(listOf("game", "parent"), events)
    }

}
