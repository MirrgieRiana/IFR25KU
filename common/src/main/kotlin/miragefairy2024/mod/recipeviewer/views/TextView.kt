package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.ColorPair
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.RendererProxy
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.ViewWithMinSize
import miragefairy2024.mod.recipeviewer.view.ViewWithSize
import miragefairy2024.mod.recipeviewer.view.sized
import mirrg.kotlin.helium.atLeast
import mirrg.kotlin.helium.atMost
import net.minecraft.network.chat.Component

class TextView(val text: Component) : View, PlaceableView {
    var minWidth = 0
    var color: ColorPair? = null
    var shadow = true
    var xAlignment: Alignment? = null
    var tooltip: List<Component>? = null
    override fun calculateMinSize(rendererProxy: RendererProxy): ViewWithMinSize {
        return object : ViewWithMinSize {
            override val minSize = IntPoint(minWidth, rendererProxy.getTextHeight())
            override fun calculateSize(maxSize: IntPoint): ViewWithSize {
                return object : ViewWithSize {
                    override val size = IntPoint(rendererProxy.calculateTextWidth(text) atMost maxSize.x atLeast minSize.x, minSize.y)
                    override fun assemble(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = viewPlacer.place(this@TextView, offset.sized(size))
                }
            }
        }
    }
}
