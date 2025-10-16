package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewPlacer

open class WrapperView : ContainerView<Unit>() {

    override fun createDefaultPosition() = Unit

    override val sizingX get() = children.single().view.sizingX
    override val sizingY get() = children.single().view.sizingY

    override fun calculateContentSizeImpl() = children.single().view.contentSize

    override fun calculateChildrenActualSize() {
        children.calculateActualSize { actualSize }
    }

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) {
        children.attachTo(viewPlacer) { offset }
    }

    val childView get() = children.single().view

}

fun <P, V : WrapperView> Child<P, *>.wrap(wrapper: V) = Child(this.position, wrapper.also {
    it += this@wrap.view
})

fun View(block: Child<Unit, WrapperView>.() -> Unit): View = Child(Unit, WrapperView()).apply { block() }.view.childView
