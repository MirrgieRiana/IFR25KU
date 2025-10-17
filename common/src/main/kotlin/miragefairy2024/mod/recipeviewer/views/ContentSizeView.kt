package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.max

class MinContentSizeView(private val size: IntPoint) : WrapperView() {
    override fun calculateContentSize() = super.calculateContentSize() max size
}

fun <P> Child<P, *>.minContentSizeX(x: Int) = this.wrap(MinContentSizeView(IntPoint(x, 0)))
fun <P> Child<P, *>.minContentSizeY(y: Int) = this.wrap(MinContentSizeView(IntPoint(0, y)))
