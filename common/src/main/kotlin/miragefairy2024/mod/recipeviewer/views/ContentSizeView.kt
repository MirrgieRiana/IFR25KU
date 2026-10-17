package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import mirrg.kotlin.helium.max
import mirrg.kotlin.helium.min

class MaxContentSizeXView(private val sizeX: Int) : WrapperView() {
    override fun calculateContentSize(): IntPoint {
        val contentSize = super.calculateContentSize()
        return IntPoint(contentSize.x min sizeX, contentSize.y)
    }
}

class MaxContentSizeYView(private val sizeY: Int) : WrapperView() {
    override fun calculateContentSize(): IntPoint {
        val contentSize = super.calculateContentSize()
        return IntPoint(contentSize.x, contentSize.y min sizeY)
    }
}

fun <P> Child<P, *>.maxContentSizeX(sizeX: Int) = this.wrap(MaxContentSizeXView(sizeX))
fun <P> Child<P, *>.maxContentSizeY(sizeY: Int) = this.wrap(MaxContentSizeYView(sizeY))


class MinContentSizeXView(private val sizeX: Int) : WrapperView() {
    override fun calculateContentSize(): IntPoint {
        val contentSize = super.calculateContentSize()
        return IntPoint(contentSize.x max sizeX, contentSize.y)
    }
}

class MinContentSizeYView(private val sizeY: Int) : WrapperView() {
    override fun calculateContentSize(): IntPoint {
        val contentSize = super.calculateContentSize()
        return IntPoint(contentSize.x, contentSize.y max sizeY)
    }
}

fun <P> Child<P, *>.minContentSizeX(sizeX: Int) = this.wrap(MinContentSizeXView(sizeX))
fun <P> Child<P, *>.minContentSizeY(sizeY: Int) = this.wrap(MinContentSizeYView(sizeY))
