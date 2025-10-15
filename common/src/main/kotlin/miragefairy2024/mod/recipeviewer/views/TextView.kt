package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.ColorPair
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.sized
import mirrg.kotlin.helium.atLeast
import net.minecraft.network.chat.Component

class TextView(val text: Component) : AbstractView(), PlaceableView {
    @JvmField
    var minWidth = 0

    override fun calculateContentSize(): IntPoint {
        return IntPoint(
            minWidth,
            renderingProxy.getTextHeight(),
        )
    }

    override fun calculateActualSize(): IntPoint {
        return IntPoint(
            renderingProxy.calculateTextWidth(text) atLeast minWidth,
            renderingProxy.getTextHeight(),
        )
    }

    var color: ColorPair? = null
    var shadow = true
    var horizontalAlignment: Alignment? = null
    var tooltip: List<Component>? = null

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = viewPlacer.place(this, offset.sized(actualSize))
}
