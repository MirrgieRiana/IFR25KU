package miragefairy2024.client.mod.recipeviewer

import dev.architectury.event.events.client.ClientScreenInputEvent
import dev.emi.emi.api.EmiApi
import dev.emi.emi.screen.RecipeScreen
import dev.emi.emi.screen.WidgetGroup
import me.shedaniel.rei.api.client.gui.widgets.Widget
import miragefairy2024.client.mod.recipeviewer.emi.EmiContainerWidget
import miragefairy2024.client.mod.recipeviewer.emi.EmiViewRendererWidget
import miragefairy2024.client.mod.recipeviewer.rei.ReiContainerWidget
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.views.ClickableView
import mirrg.kotlin.helium.Slot
import net.minecraft.SharedConstants
import net.minecraft.client.Minecraft
import net.minecraft.server.Bootstrap
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.lwjgl.glfw.GLFW
import org.mockito.Mockito

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RecipeWidgetFocusTest {
    @BeforeAll
    fun initMinecraft() {
        SharedConstants.tryDetectVersion()
        Bootstrap.bootStrap()
        ReiContainerWidget.initKeyboardFocus()
        EmiContainerWidget.initKeyboardFocus()
    }

    @Test
    fun reiDispatchesKeyboardEventsToTheClickedWidgetUntilItIsRemoved() {
        val minecraft: Minecraft = Mockito.mock(Minecraft::class.java)
        Mockito.mockStatic(Minecraft::class.java).use { minecraftMock ->
            minecraftMock.`when`<Minecraft> { Minecraft.getInstance() }.thenReturn(minecraft)
            val container = ReiContainerWidget()
            val first: Widget = Mockito.mock(Widget::class.java)
            val second: Widget = Mockito.mock(Widget::class.java)
            val key = GLFW.GLFW_KEY_ENTER
            Mockito.`when`(second.containsMouse(20.0, 10.0)).thenReturn(true)
            Mockito.`when`(second.mouseClicked(20.0, 10.0, 0)).thenReturn(true)
            Mockito.`when`(second.keyPressed(key, 0, 0)).thenReturn(true)
            Mockito.`when`(second.keyReleased(key, 0, 0)).thenReturn(true)
            Mockito.`when`(second.charTyped('a', 0)).thenReturn(true)
            container.add(first)
            val remover = container.add(second)

            Assertions.assertFalse(container.keyPressed(key, 0, 0))
            Assertions.assertTrue(container.mouseClicked(20.0, 10.0, 0))
            Assertions.assertTrue(container.keyPressed(key, 0, 0))
            Assertions.assertTrue(container.keyReleased(key, 0, 0))
            Assertions.assertTrue(container.charTyped('a', 0))
            remover.remove()
            Assertions.assertFalse(container.keyPressed(key, 0, 0))
            Assertions.assertFalse(container.keyReleased(key, 0, 0))
            Assertions.assertFalse(container.charTyped('a', 0))
            Mockito.verify(first, Mockito.never()).keyPressed(key, 0, 0)
            Mockito.verify(first, Mockito.never()).keyReleased(key, 0, 0)
            Mockito.verify(first, Mockito.never()).charTyped('a', 0)
        }
    }

    @Test
    fun emiRoutesKeysToTheClickedWidgetOnTheCurrentPage() {
        val minecraft: Minecraft = Mockito.mock(Minecraft::class.java)
        val screen: RecipeScreen = Mockito.mock(RecipeScreen::class.java)
        val group: WidgetGroup = Mockito.mock(WidgetGroup::class.java)
        minecraft.screen = screen
        Mockito.mockStatic(Minecraft::class.java).use { minecraftMock ->
            minecraftMock.`when`<Minecraft> { Minecraft.getInstance() }.thenReturn(minecraft)
            Mockito.mockStatic(EmiApi::class.java).use { emiApi ->
                val pressed = mutableListOf<ClickableView>()
                val renderer: ViewRenderer<ClickableView, Unit> = object : ViewRenderer<ClickableView, Unit> {
                    override fun createState(view: ClickableView) = Unit
                    override fun isFocusable(view: ClickableView, state: Slot<Unit>) = true
                    override fun mouseClicked(view: ClickableView, state: Slot<Unit>, bounds: IntRectangle, mouseX: Int, mouseY: Int, button: Int) = true
                    override fun keyPressed(view: ClickableView, state: Slot<Unit>, bounds: IntRectangle, keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
                        pressed += view
                        return true
                    }
                }
                val first = ClickableView()
                val second = ClickableView()
                val container = EmiContainerWidget()
                container.add(EmiViewRendererWidget(renderer, first, IntRectangle(0, 0, 10, 10)))
                val remover = container.add(EmiViewRendererWidget(renderer, second, IntRectangle(10, 0, 10, 10)))
                Mockito.`when`(screen.getGroup(container)).thenReturn(group)
                val keyPressed = ClientScreenInputEvent.KEY_PRESSED_PRE.invoker()

                Assertions.assertFalse(keyPressed.keyPressed(minecraft, screen, GLFW.GLFW_KEY_ENTER, 0, 0).isPresent)
                Assertions.assertTrue(container.mouseClicked(15, 5, 0))
                Assertions.assertTrue(keyPressed.keyPressed(minecraft, screen, GLFW.GLFW_KEY_ENTER, 0, 0).isFalse)
                emiApi.`when`<Boolean> { EmiApi.isSearchFocused() }.thenReturn(true)
                Assertions.assertFalse(keyPressed.keyPressed(minecraft, screen, GLFW.GLFW_KEY_ENTER, 0, 0).isPresent)
                emiApi.`when`<Boolean> { EmiApi.isSearchFocused() }.thenReturn(false)
                Mockito.`when`(screen.getGroup(container)).thenReturn(null)
                Assertions.assertFalse(keyPressed.keyPressed(minecraft, screen, GLFW.GLFW_KEY_ENTER, 0, 0).isPresent)
                Mockito.`when`(screen.getGroup(container)).thenReturn(group)
                remover.remove()
                Assertions.assertFalse(keyPressed.keyPressed(minecraft, screen, GLFW.GLFW_KEY_ENTER, 0, 0).isPresent)
                Assertions.assertEquals(listOf(second), pressed)
            }
        }
    }
}
