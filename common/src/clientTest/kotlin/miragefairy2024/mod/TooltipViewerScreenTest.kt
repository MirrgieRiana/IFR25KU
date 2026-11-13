package miragefairy2024.mod

import miragefairy2024.ClientProxy
import miragefairy2024.MessageViewScreenDefinition
import miragefairy2024.clientProxy
import miragefairy2024.mod.common.KeyMappingType
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.views.TextFieldView
import miragefairy2024.util.invoke
import miragefairy2024.util.text
import mirrg.kotlin.helium.atLeast
import net.minecraft.SharedConstants
import net.minecraft.network.chat.FormattedText
import net.minecraft.network.chat.Style
import net.minecraft.server.Bootstrap
import net.minecraft.util.FormattedCharSequence
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito

private val TOOLTIP_VIEWER_RENDERING_PROXY: RenderingProxy = object : RenderingProxy {
    override fun calculateTextWidth(text: FormattedText) = text.string.length * 6
    override fun calculateTextWidth(text: FormattedCharSequence) = throw UnsupportedOperationException()
    override fun getTextHeight() = 9
    override fun wrapText(text: FormattedText, maxWidth: Int): List<FormattedCharSequence> {
        return text.string.chunked(maxWidth / 6 atLeast 1).map { FormattedCharSequence.forward(it, Style.EMPTY) }
    }
}

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TooltipViewerScreenTest {

    @BeforeAll
    fun initMinecraft() {
        SharedConstants.tryDetectVersion()
        Bootstrap.bootStrap()
    }

    @Test
    fun `画面の再初期化ではフィルタを保持し開き直すと空のフィルタで開始するのだ`() {
        val oldProxy = clientProxy
        @Suppress("UNCHECKED_CAST")
        val delegate: ClientProxy<Any> = Mockito.mock(ClientProxy::class.java) as ClientProxy<Any>
        val screen = Any()
        val definitions = mutableListOf<MessageViewScreenDefinition>()
        val proxy: ClientProxy<Any> = object : ClientProxy<Any> by delegate {
            override fun createMessageViewScreen(parent: Any?, definition: MessageViewScreenDefinition): Any {
                Assertions.assertNull(parent)
                definitions += definition
                return screen
            }
        }
        clientProxy = proxy
        try {
            fun withField(definition: MessageViewScreenDefinition, block: (TextFieldView) -> Unit) {
                val view = definition.rootViewFactory()
                view.initSizeCalculation(TOOLTIP_VIEWER_RENDERING_PROXY, { 400 }, { 240 })
                val fields = mutableListOf<TextFieldView>()
                val controller = view.attachTo(IntPoint.ZERO) { child, _ ->
                    if (child is TextFieldView) fields += child
                    ViewController.NONE
                }
                try {
                    block(fields.single())
                } finally {
                    controller.dispose()
                    controller.detach()
                }
            }

            openTooltipViewerScreen()
            val definition = definitions.single()
            Assertions.assertEquals(text { TOOLTIP_VIEWER_KEY_TRANSLATION() }, definition.title)
            Assertions.assertTrue(definition.transparentBackground)
            Assertions.assertTrue(definition.closeOnInventoryKey)
            Assertions.assertEquals(KeyMappingType.OPEN_TOOLTIP_VIEWER, definition.additionalCloseKey)
            Assertions.assertFalse(definition.closeOnOutsideClick)
            withField(definition) { field ->
                Assertions.assertEquals("", field.text.value)
                field.text.value = "0,2"
            }
            withField(definition) { field ->
                Assertions.assertEquals("0,2", field.text.value)
            }

            openTooltipViewerScreen()
            withField(definitions.last()) { field ->
                Assertions.assertEquals("", field.text.value)
            }
            Mockito.verify(delegate, Mockito.times(2)).setCurrentScreen(screen)
        } finally {
            clientProxy = oldProxy
        }
    }

}
