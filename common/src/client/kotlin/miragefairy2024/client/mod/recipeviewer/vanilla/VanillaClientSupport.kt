package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.ViewPlacerRegistry
import miragefairy2024.util.EventRegistry
import miragefairy2024.util.Remover
import net.minecraft.client.gui.components.AbstractWidget

class WidgetRegistration(val widget: AbstractWidget, val interactive: Boolean)

interface VanillaViewPlacerContext {
    /** バニラの GUI が座標の原点として使う点の、画面の左上からの絶対座標なのだ～🌱 */
    val origin: IntPoint

    val canPlaceMenuSlots: Boolean
    val onBeforeRender: EventRegistry<() -> Unit>
    val onTick: EventRegistry<() -> Unit>

    fun addRenderable(registration: WidgetRegistration): Remover
    fun registerMenuSlotPlacement(slotId: Int, clipBounds: () -> IntRectangle?): Remover
}

val VANILLA_VIEW_PLACER_REGISTRY = ViewPlacerRegistry<VanillaViewPlacerContext>()
