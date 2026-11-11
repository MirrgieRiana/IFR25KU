package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.evaluateActualSize
import miragefairy2024.mod.recipeviewer.view.plus
import miragefairy2024.mod.recipeviewer.view.sized
import net.minecraft.network.chat.Component

class TooltipView : WrapperView(), PlaceableView {
    var tooltipProvider: (Int, Int) -> List<Component> = { _, _ -> listOf() }
    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): ViewController {
        return super.attachTo(offset, viewPlacer) + viewPlacer.place(this, offset.sized(evaluateActualSize()))
    }
}

fun <P> Child<P, *>.tooltip(provider: (Int, Int) -> List<Component>) = this.wrap(TooltipView().also { it.tooltipProvider = provider })
fun <P> Child<P, *>.tooltip(tooltip: List<Component>) = this.tooltip { _, _ -> tooltip }
fun <P> Child<P, *>.tooltip(text: Component) = this.tooltip(listOf(text))
