package miragefairy2024.mod.recipeviewer.views

import mirrg.kotlin.helium.max
import mirrg.kotlin.helium.min

class MaxContentSizeXView(private val sizeX: Int) : WrapperView() {
    init {
        require(sizeX >= 0) { "Maximum content width must be non-negative" }
    }

    override fun evaluateContentSizeXImpl() = super.evaluateContentSizeXImpl() min sizeX
}

class MaxContentSizeYView(private val sizeY: Int) : WrapperView() {
    init {
        require(sizeY >= 0) { "Maximum content height must be non-negative" }
    }

    override fun evaluateContentSizeYImpl() = super.evaluateContentSizeYImpl() min sizeY
}

fun <P> Child<P, *>.maxContentSizeX(x: Int) = this.wrap(MaxContentSizeXView(x))
fun <P> Child<P, *>.maxContentSizeY(y: Int) = this.wrap(MaxContentSizeYView(y))


class MinContentSizeXView(private val sizeX: Int) : WrapperView() {
    override fun evaluateContentSizeXImpl() = super.evaluateContentSizeXImpl() max sizeX
}

class MinContentSizeYView(private val sizeY: Int) : WrapperView() {
    override fun evaluateContentSizeYImpl() = super.evaluateContentSizeYImpl() max sizeY
}

fun <P> Child<P, *>.minContentSizeX(x: Int) = this.wrap(MinContentSizeXView(x))
fun <P> Child<P, *>.minContentSizeY(y: Int) = this.wrap(MinContentSizeYView(y))


fun <P> Child<P, *>.fixedContentSizeX(x: Int) = this.maxContentSizeX(x).minContentSizeX(x)
fun <P> Child<P, *>.fixedContentSizeY(y: Int) = this.maxContentSizeY(y).minContentSizeY(y)
