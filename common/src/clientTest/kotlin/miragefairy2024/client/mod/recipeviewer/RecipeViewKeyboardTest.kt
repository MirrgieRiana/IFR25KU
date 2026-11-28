package miragefairy2024.client.mod.recipeviewer

import dev.emi.emi.api.EmiApi
import dev.emi.emi.screen.RecipeScreen
import dev.emi.emi.screen.WidgetGroup
import me.shedaniel.rei.api.client.REIRuntime
import miragefairy2024.client.mod.recipeviewer.common.ClickableViewRenderer
import miragefairy2024.client.mod.recipeviewer.emi.EmiContainerWidget
import miragefairy2024.client.mod.recipeviewer.emi.EmiViewRendererWidget
import miragefairy2024.client.mod.recipeviewer.emi.registerEmiRecipeKeyboard
import miragefairy2024.client.mod.recipeviewer.rei.ReiContainerWidget
import miragefairy2024.client.mod.recipeviewer.rei.ReiViewRendererWidget
import miragefairy2024.client.mod.recipeviewer.vanilla.GuiTestEnvironment
import miragefairy2024.client.util.getActualFocused
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.views.ClickableView
import miragefairy2024.util.register
import net.fabricmc.fabric.api.client.screen.v1.Screens
import net.minecraft.SharedConstants
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.navigation.FocusNavigationEvent
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.server.Bootstrap
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.lwjgl.glfw.GLFW
import org.mockito.Mockito

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RecipeViewKeyboardTest {

    @BeforeAll
    fun initMinecraft() {
        SharedConstants.tryDetectVersion()
        Bootstrap.bootStrap()
    }

    @Test
    fun `REI で Tab を押すと別のクリック項目へフォーカスが移るのだ`() {
        GuiTestEnvironment().use {
            val calls = mutableListOf<Int>()
            val container = ReiContainerWidget(ScreenRectangle(10, 20, 100, 100))
            listOf(0, 1).forEach { index ->
                val view = ClickableView()
                view.onClick.register {
                    calls += index
                    false
                }
                container.add(ReiViewRendererWidget(ClickableViewRenderer, view, IntRectangle(10, 20 + index * 10, 40, 10)))
            }

            val event = FocusNavigationEvent.TabNavigation(true)
            val runtime = Mockito.mock(REIRuntime::class.java)
            Mockito.mockStatic(REIRuntime::class.java).use { reiRuntime ->
                reiRuntime.`when`<REIRuntime> { REIRuntime.getInstance() }.thenReturn(runtime)
                val firstPath = checkNotNull(container.nextFocusPath(event))
                firstPath.applyFocus(true)
                val first = container.getActualFocused { true } as RecipeViewKeyboardTarget
                Assertions.assertFalse(first.activate(GLFW.GLFW_KEY_ENTER, 0, 0))
                Assertions.assertFalse(container.keyPressed(GLFW.GLFW_KEY_ENTER, 0, 0))

                val secondPath = checkNotNull(container.nextFocusPath(event))
                firstPath.applyFocus(false)
                secondPath.applyFocus(true)
                val second = container.getActualFocused { true } as RecipeViewKeyboardTarget
                Assertions.assertFalse(first.activate(GLFW.GLFW_KEY_ENTER, 0, 0))
                Assertions.assertFalse(second.activate(GLFW.GLFW_KEY_SPACE, 0, 0))
                Assertions.assertEquals(listOf(0, 1), calls)
            }
        }
    }

    @Test
    fun `EMI でページを切り替えると以前のクリック項目へキーを送らないのだ`() {
        GuiTestEnvironment().use {
            val screen = Mockito.mock(RecipeScreen::class.java)
            val buttons = mutableListOf<AbstractWidget>()
            val container = EmiContainerWidget(screen, IntPoint(50, 60))
            val view = ClickableView()
            var calls = 0
            view.onClick.register {
                calls++
                false
            }
            val widget = EmiViewRendererWidget(ClickableViewRenderer, view, IntRectangle(10, 20, 40, 30), container)
            container.add(widget)
            val group = WidgetGroup(null, 50, 60, 100, 100)
            Mockito.`when`(screen.getGroup(container)).thenReturn(group)

            Mockito.mockStatic(Screens::class.java).use { screens ->
                screens.`when`<List<AbstractWidget>> { Screens.getButtons(screen) }.thenReturn(buttons)
                Mockito.mockStatic(EmiApi::class.java).use {
                    registerEmiRecipeKeyboard(screen, container)
                    val focusHost = buttons.single()
                    val event = FocusNavigationEvent.TabNavigation(true)
                    checkNotNull(focusHost.nextFocusPath(event)).applyFocus(true)
                    val target = focusHost.getActualFocused { true } as RecipeViewKeyboardTarget
                    Assertions.assertEquals(ScreenRectangle(60, 80, 40, 30), target.rectangle)
                    Assertions.assertFalse(target.activate(GLFW.GLFW_KEY_ENTER, 0, 0))
                    Assertions.assertFalse(container.keyPressed(GLFW.GLFW_KEY_ENTER, 0, 0))

                    Mockito.`when`(screen.getGroup(container)).thenReturn(null)
                    Assertions.assertFalse(target.activate(GLFW.GLFW_KEY_ENTER, 0, 0))
                    Assertions.assertNull(focusHost.nextFocusPath(event))
                    Assertions.assertEquals(1, calls)
                }
            }
        }
    }
}
