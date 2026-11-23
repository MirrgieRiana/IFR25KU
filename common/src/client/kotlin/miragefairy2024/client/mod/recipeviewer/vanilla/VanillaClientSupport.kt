package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.ViewPlacerRegistry
import miragefairy2024.util.EventRegistry
import miragefairy2024.util.Remover
import net.minecraft.client.gui.components.AbstractWidget

/**
 * Widget の各辺から外向きに測った非負のはみ出し量なのだ～🌱
 * `0` を指定した辺からは描画がはみ出さないのだ～🌱
 * 各辺に [kotlin.Int.MAX_VALUE] を指定すると、その方向のはみ出し量に上限がないことを表すのだ～🌱
 */
data class WidgetRenderOverflow(val xMin: Int, val xMax: Int, val yMin: Int, val yMax: Int) {
    init {
        require(xMin >= 0 && xMax >= 0 && yMin >= 0 && yMax >= 0) { "Widget render outsets must be non-negative" }
    }

    companion object {
        val NONE = WidgetRenderOverflow(0, 0, 0, 0)
        val UNBOUNDED = WidgetRenderOverflow(Int.MAX_VALUE, Int.MAX_VALUE, Int.MAX_VALUE, Int.MAX_VALUE)
    }
}

class WidgetRegistration(val widget: AbstractWidget, val interactive: Boolean, val renderOverflow: WidgetRenderOverflow)

interface VanillaViewPlacerContext {
    /** バニラの GUI が座標の原点として使う点の、画面の左上からの絶対座標なのだ～🌱 */
    val origin: IntPoint

    val canPlaceMenuSlots: Boolean
    val onBeforeRender: EventRegistry<() -> Unit>

    fun addRenderable(registration: WidgetRegistration): Remover
    fun registerMenuSlotPlacement(slotId: Int, clipBounds: () -> IntRectangle?): Remover
}

val VANILLA_VIEW_PLACER_REGISTRY = ViewPlacerRegistry<VanillaViewPlacerContext>()
