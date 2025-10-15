package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.sized
import miragefairy2024.util.EventRegistry
import miragefairy2024.util.Remover
import miragefairy2024.util.plus
import miragefairy2024.util.register

class ClickableView : WrapperView(), PlaceableView {
    val onClick = EventRegistry<() -> Boolean>()
    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): Remover {
        return super.attachTo(offset, viewPlacer) + viewPlacer.place(this, offset.sized(actualSize))
    }
}

fun <P> Child<P, *>.onClick(listener: () -> Boolean) = this.wrap(ClickableView().also { it.onClick.register(listener) })
