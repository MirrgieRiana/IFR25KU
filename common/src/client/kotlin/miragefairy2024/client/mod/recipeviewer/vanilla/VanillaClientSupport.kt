package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.ViewPlacerRegistry
import miragefairy2024.util.Remover
import net.minecraft.client.gui.components.AbstractWidget

interface VanillaViewPlacerContext {
    fun addRenderableWidget(widget: AbstractWidget): Remover
    fun addRenderableOnly(widget: AbstractWidget): Remover

    /** バニラの GUI が座標の原点として使う点の、画面の左上からの絶対座標なのだ～🌱 */
    val origin: IntPoint
}

val VANILLA_VIEW_PLACER_REGISTRY = ViewPlacerRegistry<VanillaViewPlacerContext>()
