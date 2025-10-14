package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.ColorPair
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import mirrg.kotlin.helium.atLeast
import net.minecraft.network.chat.Component

class TextView(val text: Component) : View, PlaceableView {
    @JvmField
    var minWidth = 0
    private var widthCache = 0
    private var heightCache = 0

    override fun layout(renderingProxy: RenderingProxy) {
        widthCache = renderingProxy.calculateTextWidth(text) atLeast minWidth
        heightCache = renderingProxy.getTextHeight()
    }

    override val contentSize get() = IntPoint(minWidth, heightCache)
    override val actualSize get() = IntPoint(widthCache, heightCache)

    var color: ColorPair? = null
    var shadow = true
    var horizontalAlignment: Alignment? = null
    var tooltip: List<Component>? = null

    override fun assemble(x: Int, y: Int, viewPlacer: ViewPlacer<PlaceableView>) = viewPlacer.place(this, IntRectangle(x, y, actualSize.x, actualSize.y))
}
