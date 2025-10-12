package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.util.Remover

open class SingleView : ContainerView<Unit>() {

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

fun <P, V : SingleView> Child<P, *>.wrap(wrapper: V) = Child(this.position, wrapper.also {
    it += this@wrap.view
})

fun View(block: Child<Unit, SingleView>.() -> Unit): View = SingleView().apply { block(Child(Unit, this)) }.childView
