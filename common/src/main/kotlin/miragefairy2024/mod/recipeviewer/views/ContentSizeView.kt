package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import mirrg.kotlin.helium.max
import mirrg.kotlin.helium.min

class MaxContentSizeXView(private val sizeX: Int) : WrapperView() {
    override fun calculateContentSizeImpl(): IntPoint {
        val contentSize = super.calculateContentSizeImpl()
        return IntPoint(contentSize.x min sizeX, contentSize.y)
    }
}

class MaxContentSizeYView(private val sizeY: Int) : WrapperView() {
    override fun calculateContentSizeImpl(): IntPoint {
        val contentSize = super.calculateContentSizeImpl()
        return IntPoint(contentSize.x, contentSize.y min sizeY)
    }
}

fun <P> Child<P, *>.maxContentSizeX(x: Int) = this.wrap(MaxContentSizeXView(x))
fun <P> Child<P, *>.maxContentSizeY(y: Int) = this.wrap(MaxContentSizeYView(y))


class MinContentSizeXView(private val sizeX: Int) : WrapperView() {
    override fun calculateContentSizeImpl(): IntPoint {
        val contentSize = super.calculateContentSizeImpl()
        return IntPoint(contentSize.x max sizeX, contentSize.y)
    }
}

class MinContentSizeYView(private val sizeY: Int) : WrapperView() {
    override fun calculateContentSizeImpl(): IntPoint {
        val contentSize = super.calculateContentSizeImpl()
        return IntPoint(contentSize.x, contentSize.y max sizeY)
    }
}

fun <P> Child<P, *>.minContentSizeX(x: Int) = this.wrap(MinContentSizeXView(x))
fun <P> Child<P, *>.minContentSizeY(y: Int) = this.wrap(MinContentSizeYView(y))
