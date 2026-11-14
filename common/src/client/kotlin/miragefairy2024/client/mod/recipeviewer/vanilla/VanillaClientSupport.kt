package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.ViewPlacerRegistry
import miragefairy2024.util.Remover
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.events.ContainerEventHandler

interface VanillaViewPlacerContext {
    /** バニラの GUI が座標の原点として使う点の、画面の左上からの絶対座標なのだ～🌱 */
    val origin: IntPoint

    val canPlaceMenuSlots: Boolean

    fun addRenderableWidget(widget: AbstractWidget): Remover
    fun addRenderableOnly(widget: AbstractWidget): Remover
}

fun ContainerEventHandler.mouseScrolledChildren(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
    return this.children().toList().any { child ->
        if (this.children().none { it === child }) return@any false
        child.isMouseOver(mouseX, mouseY) && child.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
    }
}

val VANILLA_VIEW_PLACER_REGISTRY = ViewPlacerRegistry<VanillaViewPlacerContext>()
