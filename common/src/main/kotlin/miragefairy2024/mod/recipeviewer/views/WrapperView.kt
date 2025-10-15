package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewPlacer

class WrapperView : ContainerView<Unit>() {

    override fun createDefaultPosition() = Unit

    override fun calculateContentSize() = children.single().view.contentSize
    override fun calculateActualSize() = children.single().view.actualSize

    override fun calculateChildrenActualSize() {
        children.forEach {
            it.view.calculateActualSize(renderingProxy)
        }
    }

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) {
        children.forEach {
            it.view.attachTo(offset, viewPlacer)
        }
    }

    val childView get() = children.single().view

}

fun View(block: Child<Unit, WrapperView>.() -> Unit): View = Child(Unit, WrapperView()).apply { block() }.view.childView
