package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.ColorPair
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.sized
import net.minecraft.network.chat.Component

class TextView : AbstractView(), PlaceableView {
    var text: Component = Component.empty()

    override var sizingX = Sizing.WRAP
    override val sizingY = Sizing.WRAP

    override fun calculateContentSizeImpl(): IntPoint {
        return IntPoint(
            renderingProxy.calculateTextWidth(text),
            renderingProxy.getTextHeight(),
        )
    }

    var color: ColorPair? = null
    var shadow = true
    var alignmentX: Alignment? = null
    var tooltip: List<Component>? = null

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = viewPlacer.place(this, offset.sized(actualSize))
}

fun TextView(text: Component) = TextView().also { it.text = text }
