package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewPlacer

open class SingleView : ContainerView<Unit>() {

    override fun createDefaultPosition() = Unit


    protected lateinit var childWithMinSize: ParentView<Unit>.ChildWithMinSize

    override fun calculateMinSizeImpl(): IntPoint {
        childWithMinSize = children.single().withMinSize(rendererProxy)
        return getMinSize()
    }

    protected open fun getMinSize() = childWithMinSize.minSize


    protected lateinit var childWithSize: ParentView<Unit>.ChildWithSize

    override fun calculateSizeImpl(regionSize: IntPoint): IntPoint {
        childWithSize = getChildWithSize(regionSize)
        return getSize()
    }

    protected open fun getChildWithSize(regionSize: IntPoint) = childWithMinSize.withSize(regionSize)

    protected open fun getSize() = childWithSize.size


    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = childWithSize.attachTo(offset, viewPlacer)


    val childView get() = children.single().view

}

fun View(block: Child<Unit, SingleView>.() -> Unit): View = SingleView().apply { block(Child(Unit, this)) }.childView
