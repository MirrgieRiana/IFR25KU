package miragefairy2024.mod.recipeviewer.views

import mirrg.kotlin.helium.max
import mirrg.kotlin.helium.min

class MaxContentSizeXView(private val sizeX: Int) : WrapperView() {
    override fun evaluateOwnContentSizeX() = super.evaluateOwnContentSizeX() min sizeX
}

class MaxContentSizeYView(private val sizeY: Int) : WrapperView() {
    override fun evaluateOwnContentSizeY() = super.evaluateOwnContentSizeY() min sizeY
}

fun <P> Child<P, *>.maxContentSizeX(x: Int) = this.wrap(MaxContentSizeXView(x))
fun <P> Child<P, *>.maxContentSizeY(y: Int) = this.wrap(MaxContentSizeYView(y))


class MinContentSizeXView(private val sizeX: Int) : WrapperView() {
    override fun evaluateOwnContentSizeX() = super.evaluateOwnContentSizeX() max sizeX
}

class MinContentSizeYView(private val sizeY: Int) : WrapperView() {
    override fun evaluateOwnContentSizeY() = super.evaluateOwnContentSizeY() max sizeY
}

fun <P> Child<P, *>.minContentSizeX(x: Int) = this.wrap(MinContentSizeXView(x))
fun <P> Child<P, *>.minContentSizeY(y: Int) = this.wrap(MinContentSizeYView(y))
