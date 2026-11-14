package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.mod.recipeviewer.views.SpaceView
import miragefairy2024.util.empty
import miragefairy2024.util.text
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.Screen
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.lwjgl.glfw.GLFW
import org.mockito.Mockito

private class KeyboardViewScreen : ViewScreen(text { empty() }) {
    override fun createRootView() = SpaceView()

    fun initialize() {
        width = 200
        height = 200
        init()
    }

    fun register(widget: AbstractWidget) {
        addRenderableWidget(widget)
    }
}

class ViewScreenTest {

    @Test
    fun `画面のホイールを重なる子へ一度ずつ渡すのだ`() {
        Mockito.mockStatic(Screen::class.java).use {
            val screen = KeyboardViewScreen()
            val background: AbstractWidget = Mockito.mock(AbstractWidget::class.java)
            val child: AbstractWidget = Mockito.mock(AbstractWidget::class.java)
            Mockito.`when`(background.isMouseOver(10.0, 20.0)).thenReturn(true)
            Mockito.`when`(child.isMouseOver(10.0, 20.0)).thenReturn(true)
            Mockito.`when`(child.mouseScrolled(10.0, 20.0, 0.0, -1.0)).thenReturn(true)
            screen.initialize()
            screen.register(background)
            screen.register(child)
            try {
                Assertions.assertTrue(screen.mouseScrolled(10.0, 20.0, 0.0, -1.0))
                Mockito.verify(background).mouseScrolled(10.0, 20.0, 0.0, -1.0)
                Mockito.verify(child).mouseScrolled(10.0, 20.0, 0.0, -1.0)
            } finally {
                screen.removed()
            }
        }
    }

    @Test
    fun `入力欄の上下はフォーカスを移動し左右はカーソルを移動するのだ`() {
        Mockito.mockStatic(Screen::class.java).use {
            val font: Font = Mockito.mock(Font::class.java)
            Mockito.`when`(font.plainSubstrByWidth(Mockito.anyString(), Mockito.anyInt())).thenAnswer { it.getArgument<String>(0) }
            Mockito.`when`(font.plainSubstrByWidth(Mockito.anyString(), Mockito.anyInt(), Mockito.anyBoolean())).thenAnswer { it.getArgument<String>(0) }
            val screen = KeyboardViewScreen()
            val keys = mutableListOf<Int>()
            val editBox: EditBox = object : EditBox(font, 80, 80, 40, 20, text { empty() }) {
                override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
                    keys += keyCode
                    return super.keyPressed(keyCode, scanCode, modifiers)
                }
            }
            val buttons = mapOf(
                GLFW.GLFW_KEY_UP to Button.builder(text { empty() }) { }.bounds(80, 20, 40, 20).build(),
                GLFW.GLFW_KEY_DOWN to Button.builder(text { empty() }) { }.bounds(80, 140, 40, 20).build(),
                GLFW.GLFW_KEY_LEFT to Button.builder(text { empty() }) { }.bounds(20, 80, 40, 20).build(),
                GLFW.GLFW_KEY_RIGHT to Button.builder(text { empty() }) { }.bounds(140, 80, 40, 20).build(),
            )
            screen.initialize()
            screen.register(editBox)
            buttons.values.forEach {
                screen.register(it)
            }
            editBox.value = "abcd"
            try {
                buttons.forEach { (keyCode, button) ->
                    screen.focused = editBox
                    editBox.moveCursorTo(2, false)
                    keys.clear()

                    val handled = screen.keyPressed(keyCode, 0, 0)

                    Assertions.assertEquals(listOf(keyCode), keys)
                    when (keyCode) {
                        GLFW.GLFW_KEY_UP, GLFW.GLFW_KEY_DOWN -> {
                            Assertions.assertFalse(handled)
                            Assertions.assertSame(button, screen.focused)
                        }
                        GLFW.GLFW_KEY_LEFT, GLFW.GLFW_KEY_RIGHT -> {
                            Assertions.assertTrue(handled)
                            Assertions.assertSame(editBox, screen.focused)
                            Assertions.assertEquals(if (keyCode == GLFW.GLFW_KEY_LEFT) 1 else 3, editBox.cursorPosition)
                        }
                    }
                }
            } finally {
                screen.removed()
            }
        }
    }

    @Test
    fun `入力欄が消費した上下キーではフォーカスを移動しないのだ`() {
        Mockito.mockStatic(Screen::class.java).use {
            val screen = KeyboardViewScreen()
            val keys = mutableListOf<Int>()
            val editBox: EditBox = object : EditBox(null, 80, 80, 40, 20, text { empty() }) {
                override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
                    keys += keyCode
                    return true
                }
            }
            screen.initialize()
            screen.register(editBox)
            screen.register(Button.builder(text { empty() }) { }.bounds(80, 20, 40, 20).build())
            screen.register(Button.builder(text { empty() }) { }.bounds(80, 140, 40, 20).build())
            screen.focused = editBox
            try {
                listOf(GLFW.GLFW_KEY_UP, GLFW.GLFW_KEY_DOWN).forEach { keyCode ->
                    keys.clear()

                    Assertions.assertTrue(screen.keyPressed(keyCode, 0, 0))

                    Assertions.assertSame(editBox, screen.focused)
                    Assertions.assertEquals(listOf(keyCode), keys)
                }
            } finally {
                screen.removed()
            }
        }
    }

}
