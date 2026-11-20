package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.ViewPlacerRegistry
import miragefairy2024.util.EventRegistry
import miragefairy2024.util.Remover
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.world.inventory.Slot

interface VanillaViewPlacerContext {
    /** バニラの GUI が座標の原点として使う点の、画面の左上からの絶対座標なのだ～🌱 */
    val origin: IntPoint

    val canPlaceMenuSlots: Boolean
    val onBeforeRender: EventRegistry<() -> Unit>

    fun addRenderableWidget(widget: AbstractWidget): Remover
    fun addRenderableOnly(widget: AbstractWidget): Remover
    fun registerMenuSlot(slot: Slot, clipBounds: () -> IntRectangle?): Remover = throw UnsupportedOperationException("Menu slots cannot be registered in this context")
}

val VANILLA_VIEW_PLACER_REGISTRY = ViewPlacerRegistry<VanillaViewPlacerContext>()
