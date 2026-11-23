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

    /** Widget の各辺から外向きに測った非負のはみ出し量なのだ～🌱 */
    data class Outsets(val xMin: Int, val xMax: Int, val yMin: Int, val yMax: Int) : WidgetRenderOverflow {
        init {
            require(xMin >= 0 && xMax >= 0 && yMin >= 0 && yMax >= 0) { "Widget render outsets must be non-negative" }
        }
    }

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
