package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.ColorPair
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.RendererProxy
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import mirrg.kotlin.helium.atLeast
import net.minecraft.network.chat.Component

class TextView(val text: Component) : View {
    @JvmField
    var minWidth = 0
    private var widthCache = 0
    private var heightCache = 0

    override fun layout(rendererProxy: RendererProxy) {
        widthCache = rendererProxy.calculateTextWidth(text) atLeast minWidth
        heightCache = rendererProxy.getTextHeight()
    }

    override fun getMinWidth() = minWidth
    override fun getMinHeight() = heightCache
    override fun getWidth() = widthCache
    override fun getHeight() = heightCache

    var color: ColorPair? = null
    var shadow = true
    var horizontalAlignment: Alignment? = null
    var tooltip: List<Component>? = null

    override fun assemble(x: Int, y: Int, viewPlacer: ViewPlacer<View>) = viewPlacer.place(this, IntRectangle(x, y, getWidth(), getHeight()))
}
