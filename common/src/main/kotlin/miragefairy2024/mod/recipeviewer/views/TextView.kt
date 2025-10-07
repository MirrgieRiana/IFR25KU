package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.ColorPair
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.sized
import mirrg.kotlin.helium.atLeast
import mirrg.kotlin.helium.atMost
import net.minecraft.network.chat.Component

class TextView(val text: Component) : AbstractView(), PlaceableView {
    var minWidth = 0
    var color: ColorPair? = null
    var shadow = true
    var xAlignment: Alignment? = null
    var tooltip: List<Component>? = null
    override fun calculateMinSizeImpl() = IntPoint(minWidth, rendererProxy.getTextHeight())
    override fun calculateSizeImpl(regionSize: IntPoint) = IntPoint(rendererProxy.calculateTextWidth(text) atMost regionSize.x atLeast minWidth, calculatedMinSize.y)
    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = viewPlacer.place(this, offset.sized(calculatedSize))
}
