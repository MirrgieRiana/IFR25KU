package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.max
import miragefairy2024.mod.recipeviewer.view.min
import mirrg.kotlin.helium.max
import mirrg.kotlin.helium.min

class MaxContentSizeView(private val size: IntPoint) : WrapperView() {
    override fun calculateContentSize() = super.calculateContentSize() min size
    override fun calculateActualSizeY() = super.calculateActualSizeY() min size.y
}

// 制限しない軸には、小さい方を採る演算で選ばれない値を渡すのだ～🌱
fun <P> Child<P, *>.maxContentSizeX(x: Int) = this.wrap(MaxContentSizeView(IntPoint(x, Int.MAX_VALUE)))
fun <P> Child<P, *>.maxContentSizeY(y: Int) = this.wrap(MaxContentSizeView(IntPoint(Int.MAX_VALUE, y)))


class MinContentSizeView(private val size: IntPoint) : WrapperView() {
    override fun calculateContentSize() = super.calculateContentSize() max size
    override fun calculateActualSizeY() = super.calculateActualSizeY() max size.y
}

fun <P> Child<P, *>.minContentSizeX(x: Int) = this.wrap(MinContentSizeView(IntPoint(x, 0)))
fun <P> Child<P, *>.minContentSizeY(y: Int) = this.wrap(MinContentSizeView(IntPoint(0, y)))
