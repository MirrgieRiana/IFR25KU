package miragefairy2024.mod.recipeviewer.view

import net.minecraft.network.chat.Component

interface RenderingProxy {
    fun calculateTextWidth(component: Component): Int
    fun getTextHeight(): Int
}
