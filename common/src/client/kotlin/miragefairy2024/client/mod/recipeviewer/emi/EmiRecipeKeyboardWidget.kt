package miragefairy2024.client.mod.recipeviewer.emi

import dev.emi.emi.screen.RecipeScreen
import miragefairy2024.client.mod.recipeviewer.RecipeViewKeyboardTarget
import miragefairy2024.util.empty
import miragefairy2024.util.text
import net.fabricmc.fabric.api.client.screen.v1.Screens
import net.minecraft.client.gui.GuiGraphics as GuiGraphicsExtractor // MIGRATION2612 ReplaceImport net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.AbstractContainerWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.gui.navigation.ScreenRectangle

fun registerEmiRecipeKeyboard(screen: RecipeScreen, container: EmiContainerWidget) {
    val buttons = Screens.getButtons(screen)
    val keyboardWidget = buttons.filterIsInstance<EmiRecipeKeyboardWidget>().firstOrNull() ?: EmiRecipeKeyboardWidget(screen).also {
        buttons.add(it)
    }
    keyboardWidget.add(container)
}

private class EmiRecipeKeyboardWidget(private val screen: RecipeScreen) : AbstractContainerWidget(0, 0, 0, 0, text { empty() }) {
    private val containers = mutableListOf<EmiContainerWidget>()

    fun add(container: EmiContainerWidget) {
        containers += container
    }

    override fun children(): List<RecipeViewKeyboardTarget> {
        // ページやカテゴリを切り替えると、EMI は表示中のレシピを配置し直すのだ～🌱
        containers.removeAll { screen.getGroup(it) == null }
        val targets = containers.flatMap { it.getKeyboardTargets() }
        if (focused != null && focused !in targets) setFocused(null)
        return targets
    }

    override fun getRectangle(): ScreenRectangle {
        val rectangles = children().map { it.rectangle }
        if (rectangles.isEmpty()) return ScreenRectangle.empty()
        val left = rectangles.minOf { it.left() }
        val top = rectangles.minOf { it.top() }
        return ScreenRectangle(left, top, rectangles.maxOf { it.right() } - left, rectangles.maxOf { it.bottom() } - top)
    }

    override fun renderWidget(guiGraphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTick: Float) = Unit
    override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) = Unit
}
