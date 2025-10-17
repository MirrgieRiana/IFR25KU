package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.util.Remover

open class WrapperView : ContainerView<Unit>() {

    override fun createDefaultPosition() = Unit

    override val sizingX get() = childView.sizingX
    override val sizingY get() = childView.sizingY

    override fun calculateContentSize() = childView.contentSize

    override fun calculateChildrenActualSize() {
        childView.calculateActualSize(actualSize)
    }

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): Remover {
        return childView.attachTo(offset, viewPlacer)
    }

    val childView get() = children.single().view

}

fun <P, V : WrapperView> Child<P, *>.wrap(wrapper: V) = Child(this.position, wrapper.also {
    it += this@wrap.view
})

inline fun View(block: Child<Unit, WrapperView>.() -> Unit): View = WrapperView().apply { block(Child(Unit, this)) }.childView
