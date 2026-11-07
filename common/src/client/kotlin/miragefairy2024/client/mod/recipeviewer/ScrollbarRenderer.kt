package miragefairy2024.client.mod.recipeviewer

import miragefairy2024.client.mod.recipeviewer.common.FlatScrollbarRenderer
import miragefairy2024.mod.recipeviewer.view.FlatScrollbar
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.Scrollbar
import net.minecraft.client.gui.GuiGraphics

fun interface ScrollbarRenderer {
    fun render(graphics: GuiGraphics, bounds: IntRectangle, color: Int)
}

object ScrollbarRendererRegistry {
    private val registry = mutableMapOf<Scrollbar, ScrollbarRenderer>(FlatScrollbar to FlatScrollbarRenderer)

    fun register(scrollbar: Scrollbar, scrollbarRenderer: ScrollbarRenderer) {
        check(scrollbar !in registry) { "Scrollbar renderer already registered: $scrollbar" }
        registry[scrollbar] = scrollbarRenderer
    }

    fun get(scrollbar: Scrollbar) = registry[scrollbar] ?: error("Unknown scrollbar: $scrollbar")
}
