package miragefairy2024.mod.recipeviewer.view

import net.minecraft.network.chat.Component

interface RendererProxy {
    fun calculateTextWidth(component: Component): Int
    fun getTextHeight(): Int
}
