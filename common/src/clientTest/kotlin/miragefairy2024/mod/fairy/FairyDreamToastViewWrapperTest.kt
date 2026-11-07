package miragefairy2024.mod.fairy

import miragefairy2024.ClientProxy
import miragefairy2024.MirageFairy2024
import miragefairy2024.clientProxy
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.evaluateActualSize
import miragefairy2024.mod.recipeviewer.views.ImageView
import miragefairy2024.mod.recipeviewer.views.ItemView
import miragefairy2024.mod.recipeviewer.views.TextView
import miragefairy2024.util.black
import miragefairy2024.util.darkBlue
import miragefairy2024.util.invoke
import miragefairy2024.util.text
import net.minecraft.SharedConstants
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.FormattedText
import net.minecraft.server.Bootstrap
import net.minecraft.util.FormattedCharSequence
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito

private val TOAST_RENDERING_PROXY: RenderingProxy = object : RenderingProxy {
    override fun calculateTextWidth(text: FormattedText) = 100
    override fun calculateTextWidth(text: FormattedCharSequence) = 100
    override fun getTextHeight() = 9
    override fun wrapText(text: FormattedText, maxWidth: Int) = throw UnsupportedOperationException()
}

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FairyDreamToastViewWrapperTest {

    @BeforeAll
    fun initMinecraft() {
        SharedConstants.tryDetectVersion()
        Bootstrap.bootStrap()
    }

    @Test
    fun `common のトーストが省略後の名前と元の寸法や装飾設定を使用するのだ`() {
        val oldProxy = clientProxy
        val proxy: ClientProxy<*> = Mockito.mock(ClientProxy::class.java)
        clientProxy = proxy
        try {
            val itemStack: ItemStack = ItemStack(Items.STONE, 64)
            itemStack.set(DataComponents.CUSTOM_NAME, text { "long name".repeat(30)() })
            val displayName = itemStack.hoverName.darkBlue
            val ellipsis = text { "..."().darkBlue }
            val name = text { "short..."().darkBlue }.visualOrderText
            Mockito.`when`(proxy.ellipsizeText(displayName, 118, ellipsis)).thenReturn(name)

            val view = FairyDreamToastViewWrapper(itemStack).view
            view.initSizeCalculation(TOAST_RENDERING_PROXY, { 1000 }, { 1000 })
            Assertions.assertEquals(IntPoint(160, 32), view.evaluateActualSize())
            val placements = mutableListOf<Pair<PlaceableView, IntRectangle>>()
            view.attachTo(IntPoint.ZERO) { child, bounds ->
                placements += Pair(child, bounds)
                ViewController.NONE
            }

            val image = placements.single { it.first is ImageView }
            Assertions.assertEquals(IntRectangle(0, 0, 160, 32), image.second)
            Assertions.assertEquals(MirageFairy2024.identifier("textures/gui/fairy_dream_toast.png"), (image.first as ImageView).texture.id)
            val item = placements.single { it.first is ItemView }
            Assertions.assertEquals(IntRectangle(8, 8, 16, 16), item.second)
            Assertions.assertSame(itemStack, (item.first as ItemView).itemStack)
            Assertions.assertFalse((item.first as ItemView).showDecorations)
            val texts = placements.filter { it.first is TextView }
            Assertions.assertEquals(listOf(IntRectangle(30, 6, 100, 9), IntRectangle(34, 17, 100, 9)), texts.map { it.second })
            val title = (texts[0].first as TextView).text.value
            val expectedTitle = text { GAIN_FAIRY_DREAM_TRANSLATION().black }.visualOrderText
            val titleCharacters = mutableListOf<Pair<Int, Int?>>()
            title.accept { _, style, codePoint ->
                titleCharacters += Pair(codePoint, style.color?.value)
                true
            }
            val expectedTitleCharacters = mutableListOf<Pair<Int, Int?>>()
            expectedTitle.accept { _, style, codePoint ->
                expectedTitleCharacters += Pair(codePoint, style.color?.value)
                true
            }
            Assertions.assertEquals(expectedTitleCharacters, titleCharacters)
            Assertions.assertSame(name, (texts[1].first as TextView).text.value)
            Assertions.assertTrue(texts.all { !(it.first as TextView).shadow })
            Mockito.verify(proxy).ellipsizeText(displayName, 118, ellipsis)
            Mockito.verifyNoMoreInteractions(proxy)
        } finally {
            clientProxy = oldProxy
        }
    }

}
