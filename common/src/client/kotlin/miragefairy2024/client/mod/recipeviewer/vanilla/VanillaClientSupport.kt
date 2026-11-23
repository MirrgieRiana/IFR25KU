package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.ViewPlacerRegistry
import miragefairy2024.util.EventRegistry
import miragefairy2024.util.Remover
import net.minecraft.client.gui.components.AbstractWidget

/** Widget の領域を基準に描画範囲を指定するのだ～🌱 */
sealed interface WidgetRenderOverflow {
    /** 描画は Widget の領域内に収まるのだ～🌱 */
    data object None : WidgetRenderOverflow

    /** 描画範囲の各辺と Widget の対応する辺との差分なのだ～🌱 */
    data class Bounded(val xMin: Int, val xMax: Int, val yMin: Int, val yMax: Int) : WidgetRenderOverflow

    /** Widget の領域からのはみ出し量に上限を指定しないのだ～🌱 */
    data object Unbounded : WidgetRenderOverflow
}

interface VanillaViewPlacerContext {
    /** バニラの GUI が座標の原点として使う点の、画面の左上からの絶対座標なのだ～🌱 */
    val origin: IntPoint

    val canPlaceMenuSlots: Boolean
    val onBeforeRender: EventRegistry<() -> Unit>

    fun addRenderableWidget(widget: AbstractWidget, renderOverflow: WidgetRenderOverflow): Remover
    fun addRenderableOnly(widget: AbstractWidget, renderOverflow: WidgetRenderOverflow): Remover
    fun registerMenuSlotPlacement(slotId: Int, clipBounds: () -> IntRectangle?): Remover
}

val VANILLA_VIEW_PLACER_REGISTRY = ViewPlacerRegistry<VanillaViewPlacerContext>()
