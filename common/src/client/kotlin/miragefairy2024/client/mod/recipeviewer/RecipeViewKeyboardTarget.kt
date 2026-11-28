package miragefairy2024.client.mod.recipeviewer

import miragefairy2024.client.util.getActualFocused
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents
import net.minecraft.client.gui.ComponentPath
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.navigation.CommonInputs
import net.minecraft.client.gui.navigation.FocusNavigationEvent
import net.minecraft.client.gui.navigation.ScreenRectangle

fun initRecipeViewKeyboard() {
    ScreenEvents.AFTER_INIT.register { _, screen, _, _ ->
        ScreenKeyboardEvents.allowKeyPress(screen).register allow@{ _, keyCode, scanCode, modifiers ->
            val target = screen.getActualFocused { true } as? RecipeViewKeyboardTarget ?: return@allow true
            // ページの再構築で外れた配置は、Screen のフォーカスからも解除するのだ～🌱
            if (screen.getActualFocused { container -> container.children().any { it === container.focused } } !== target) {
                screen.clearFocus()
                return@allow true
            }
            // レシピ画面がホバーを基準にキーを配る前に、フォーカスされた項目に一度だけ届けるのだ～🌱
            !CommonInputs.selected(keyCode) || !target.activate(keyCode, scanCode, modifiers)
        }
    }
}

class RecipeViewKeyboardTarget(
    private val bounds: ScreenRectangle,
    private val canFocus: () -> Boolean,
    private val onKeyPressed: (Int, Int, Int) -> Boolean,
) : GuiEventListener {

    private var focused = false

    override fun getRectangle() = bounds
    override fun isFocused() = focused

    override fun setFocused(focused: Boolean) {
        this.focused = focused
    }

    override fun nextFocusPath(event: FocusNavigationEvent): ComponentPath? {
        if (focused || bounds.width <= 0 || bounds.height <= 0 || !canFocus()) return null
        return ComponentPath.leaf(this)
    }

    fun activate(keyCode: Int, scanCode: Int, modifiers: Int) = focused && canFocus() && onKeyPressed(keyCode, scanCode, modifiers)
}
