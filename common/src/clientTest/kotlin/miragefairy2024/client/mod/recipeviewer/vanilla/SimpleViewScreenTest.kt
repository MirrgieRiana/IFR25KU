package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.mod.recipeviewer.vanilla.ViewScreenDefinition
import miragefairy2024.mod.recipeviewer.views.SpaceView
import miragefairy2024.util.KeyMapping
import miragefairy2024.util.empty
import miragefairy2024.util.text
import net.minecraft.client.GameNarrator
import net.minecraft.client.InputType
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.Screen
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito

private class TestSimpleViewScreen(parent: Screen?, definition: ViewScreenDefinition) : SimpleViewScreen(parent, definition) {
    fun register(widget: AbstractWidget) {
        addRenderableWidget(widget)
    }
}

class SimpleViewScreenTest {

    private fun createMinecraft(): Minecraft {
        val minecraft: Minecraft = Mockito.mock(Minecraft::class.java)
        val narrator: GameNarrator = Mockito.mock(GameNarrator::class.java)
        Mockito.`when`(minecraft.lastInputType).thenReturn(InputType.MOUSE)
        Mockito.`when`(minecraft.narrator).thenReturn(narrator)
        return minecraft
    }

    @Test
    fun `画面を再初期化すると View を再生成し未処理クリックでは閉じないのだ`() {
        Mockito.mockStatic(Screen::class.java).use {
            val minecraft = createMinecraft()
            val parent: Screen = Mockito.mock(Screen::class.java)
            var creations = 0
            val definition = ViewScreenDefinition(text { empty() }) {
                creations++
                SpaceView()
            }
            val screen = SimpleViewScreen(parent, definition)
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
            val minecraft = createMinecraft()
            val parent: Screen = Mockito.mock(Screen::class.java)
            val additionalCloseKey: KeyMapping = Mockito.mock(KeyMapping::class.java)
            Mockito.`when`(additionalCloseKey.matches(70, 10)).thenReturn(true)
            val definition = ViewScreenDefinition(text { empty() }) { SpaceView() }
            definition.closeOnInventoryKey = false
            val screen = TestSimpleViewScreen(parent, definition)
            screen.init(minecraft, 200, 200)
            try {
                Assertions.assertFalse(screen.keyPressed(70, 10, 2))

                definition.additionalCloseKey = additionalCloseKey
                val editBox: EditBox = object : EditBox(null, 80, 80, 40, 20, text { empty() }) {
                    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int) = true
                }
                screen.register(editBox)
                screen.focused = editBox
                Assertions.assertTrue(screen.keyPressed(70, 10, 2))
                Mockito.verifyNoInteractions(additionalCloseKey)
                Mockito.verify(minecraft, Mockito.never()).setScreen(parent)

                screen.focused = null
                Assertions.assertFalse(screen.keyPressed(71, 11, 3))
                Mockito.verify(additionalCloseKey).matches(71, 11)

                definition.additionalCloseKey = null
                Assertions.assertFalse(screen.keyPressed(70, 10, 2))
                Mockito.verifyNoMoreInteractions(additionalCloseKey)
                Mockito.verify(minecraft, Mockito.never()).setScreen(parent)

                definition.additionalCloseKey = additionalCloseKey
                Assertions.assertTrue(screen.keyPressed(70, 10, 2))
                Mockito.verify(additionalCloseKey).matches(70, 10)
                Mockito.verify(minecraft).setScreen(parent)
            } finally {
                screen.removed()
            }
        }
    }

    @Test
    fun `メッセージ画面は未処理クリックで親画面へ戻るのだ`() {
        Mockito.mockStatic(Screen::class.java).use {
            val minecraft = createMinecraft()
            val parent: Screen = Mockito.mock(Screen::class.java)
            val screen = MessageViewScreen(parent, text { empty() }) { SpaceView() }
            screen.init(minecraft, 200, 200)
            try {
                Assertions.assertTrue(screen.mouseClicked(10.0, 10.0, 0))
                Mockito.verify(minecraft).setScreen(parent)
            } finally {
                screen.removed()
            }
        }
    }

}
