package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.ViewPlacerRegistry
import miragefairy2024.util.EventRegistry
import miragefairy2024.util.Remover
import net.minecraft.client.gui.components.AbstractWidget

/** 描画範囲の各辺と Widget の対応する辺との差分なのだ～🌱 */
data class WidgetRenderOverflow(val xMin: Int, val xMax: Int, val yMin: Int, val yMax: Int)

interface VanillaViewPlacerContext {
    /** バニラの GUI が座標の原点として使う点の、画面の左上からの絶対座標なのだ～🌱 */
    val origin: IntPoint

    val canPlaceMenuSlots: Boolean
    val onBeforeRender: EventRegistry<() -> Unit>

    fun addRenderableWidget(widget: AbstractWidget): Remover
    fun addRenderableWidget(widget: AbstractWidget, renderOverflow: WidgetRenderOverflow): Remover = addRenderableWidget(widget)
    fun addRenderableOnly(widget: AbstractWidget): Remover
    fun addRenderableOnly(widget: AbstractWidget, renderOverflow: WidgetRenderOverflow): Remover = addRenderableOnly(widget)
    fun registerMenuSlotPlacement(slotId: Int, clipBounds: () -> IntRectangle?): Remover
}

val VANILLA_VIEW_PLACER_REGISTRY = ViewPlacerRegistry<VanillaViewPlacerContext>()
