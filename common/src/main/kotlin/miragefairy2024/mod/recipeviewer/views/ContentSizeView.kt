package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import mirrg.kotlin.helium.max
import mirrg.kotlin.helium.min

class MaxContentSizeXView(private val x: Int) : WrapperView() {
    override fun calculateContentSize(): IntPoint {
        val contentSize = super.calculateContentSize()
        return IntPoint(contentSize.x min x, contentSize.y)
    }
}

class MaxContentSizeYView(private val y: Int) : WrapperView() {
    override fun calculateContentSize(): IntPoint {
        val contentSize = super.calculateContentSize()
        return IntPoint(contentSize.x, contentSize.y min y)
    }
}

fun <P> Child<P, *>.maxContentSizeX(x: Int) = this.wrap(MaxContentSizeXView(x))
fun <P> Child<P, *>.maxContentSizeY(y: Int) = this.wrap(MaxContentSizeYView(y))


class MinContentSizeXView(private val x: Int) : WrapperView() {
    override fun calculateContentSize(): IntPoint {
        val contentSize = super.calculateContentSize()
        return IntPoint(contentSize.x max x, contentSize.y)
    }
}

class MinContentSizeYView(private val y: Int) : WrapperView() {
    override fun calculateContentSize(): IntPoint {
        val contentSize = super.calculateContentSize()
        return IntPoint(contentSize.x, contentSize.y max y)
    }
}

fun <P> Child<P, *>.minContentSizeX(x: Int) = this.wrap(MinContentSizeXView(x))
fun <P> Child<P, *>.minContentSizeY(y: Int) = this.wrap(MinContentSizeYView(y))
