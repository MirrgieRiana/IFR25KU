package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.MessageViewScreenDefinition
import miragefairy2024.client.mod.getKeyMapping
import miragefairy2024.mod.common.KeyMappingType
import miragefairy2024.mod.recipeviewer.views.SpaceView
import miragefairy2024.util.empty
import miragefairy2024.util.text
import net.minecraft.client.GameNarrator
import net.minecraft.client.InputType
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.Screen
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class MessageViewScreenTest {

    private fun createMinecraft(): Minecraft {
        val minecraft: Minecraft = Mockito.mock(Minecraft::class.java)
        val narrator: GameNarrator = Mockito.mock(GameNarrator::class.java)
        Mockito.`when`(minecraft.lastInputType).thenReturn(InputType.MOUSE)
        Mockito.`when`(minecraft.narrator).thenReturn(narrator)
        return minecraft
    }

    @Test
    fun `画面を再初期化すると View を再生成し外部クリック終了を無効にした画面は閉じないのだ`() {
        Mockito.mockStatic(Screen::class.java).use {
            val minecraft = createMinecraft()
            val parent: Screen = Mockito.mock(Screen::class.java)
            var creations = 0
            val definition = MessageViewScreenDefinition(text { empty() }) {
                creations++
                SpaceView()
            }
            definition.closeOnOutsideClick = false
            val screen = MessageViewScreen(parent, definition)
            try {
                screen.init(minecraft, 200, 200)
                screen.init(minecraft, 300, 200)

                Assertions.assertEquals(2, creations)
                Assertions.assertFalse(screen.mouseClicked(10.0, 10.0, 0))
                Mockito.verify(minecraft, Mockito.never()).setScreen(parent)
                screen.onClose()
                Mockito.verify(minecraft).setScreen(parent)
            } finally {
                screen.removed()
            }
        }
    }

    @Test
    fun `追加終了キーがある場合だけ未消費のキーを照合して閉じるのだ`() {
        Mockito.mockStatic(Screen::class.java).use {
            Mockito.mockStatic(Class.forName("miragefairy2024.client.mod.CommonClientModuleKt")).use { keyMappings ->
                val minecraft = createMinecraft()
                val parent: Screen = Mockito.mock(Screen::class.java)
                val additionalCloseKey: KeyMapping = Mockito.mock(KeyMapping::class.java)
                Mockito.`when`(additionalCloseKey.matches(70, 10)).thenReturn(true)
                val definition = MessageViewScreenDefinition(text { empty() }) { SpaceView() }
                definition.closeOnInventoryKey = false
                keyMappings.`when`<KeyMapping> { getKeyMapping(KeyMappingType.OPEN_TOOLTIP_VIEWER) }.thenReturn(additionalCloseKey)
                val screen: MessageViewScreen = Mockito.spy(MessageViewScreen(parent, definition))
                screen.init(minecraft, 200, 200)
                try {
                    Assertions.assertFalse(screen.keyPressed(70, 10, 2))

                    definition.additionalCloseKey = KeyMappingType.OPEN_TOOLTIP_VIEWER
                    val editBox: EditBox = object : EditBox(null, 80, 80, 40, 20, text { empty() }) {
                        override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int) = true
                    }
                    Mockito.doReturn(listOf(editBox)).`when`(screen).children()
                    screen.focused = editBox
                    Assertions.assertTrue(screen.keyPressed(70, 10, 2))
                    keyMappings.verifyNoInteractions()
                    Mockito.verifyNoInteractions(additionalCloseKey)
                    Mockito.verify(minecraft, Mockito.never()).setScreen(parent)

                    screen.focused = null
                    Assertions.assertFalse(screen.keyPressed(71, 11, 3))
                    Mockito.verify(additionalCloseKey).matches(71, 11)

                    definition.additionalCloseKey = null
                    Assertions.assertFalse(screen.keyPressed(70, 10, 2))
                    Mockito.verifyNoMoreInteractions(additionalCloseKey)
                    Mockito.verify(minecraft, Mockito.never()).setScreen(parent)

                    definition.additionalCloseKey = KeyMappingType.OPEN_TOOLTIP_VIEWER
                    Assertions.assertTrue(screen.keyPressed(70, 10, 2))
                    keyMappings.verify({ getKeyMapping(KeyMappingType.OPEN_TOOLTIP_VIEWER) }, Mockito.times(2))
                    Mockito.verify(additionalCloseKey).matches(70, 10)
                    Mockito.verify(minecraft).setScreen(parent)
                } finally {
                    screen.removed()
                }
            }
        }
    }

    @Test
    fun `子が消費したクリックでは閉じず未処理クリックは設定に従って親画面へ戻るのだ`() {
        Mockito.mockStatic(Screen::class.java).use {
            val minecraft = createMinecraft()
            val parent: Screen = Mockito.mock(Screen::class.java)
            val definition = MessageViewScreenDefinition(text { empty() }) { SpaceView() }
            val screen: MessageViewScreen = Mockito.spy(MessageViewScreen(parent, definition))
            screen.init(minecraft, 200, 200)
            try {
                val widget: AbstractWidget = Mockito.mock(AbstractWidget::class.java)
                Mockito.`when`(widget.mouseClicked(10.0, 10.0, 0)).thenReturn(true)
                Mockito.doReturn(listOf(widget)).`when`(screen).children()

                Assertions.assertTrue(screen.mouseClicked(20.0, 20.0, 0))
                Mockito.verify(minecraft).setScreen(parent)

                Assertions.assertTrue(screen.mouseClicked(10.0, 10.0, 0))
                Mockito.verify(minecraft).setScreen(parent)

                definition.closeOnOutsideClick = false
                Assertions.assertFalse(screen.mouseClicked(20.0, 20.0, 0))
                Mockito.verify(minecraft).setScreen(parent)

                definition.closeOnOutsideClick = true
                Assertions.assertTrue(screen.mouseClicked(10.0, 10.0, 0))
                Mockito.verify(minecraft).setScreen(parent)
                Assertions.assertTrue(screen.mouseClicked(20.0, 20.0, 0))
                Mockito.verify(minecraft, Mockito.times(2)).setScreen(parent)
            } finally {
                screen.removed()
            }
        }
    }

}
