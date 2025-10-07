package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.RendererProxy
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.ViewWithMinSize
import miragefairy2024.mod.recipeviewer.view.ViewWithSize

open class SingleView : ContainerView<Unit>() {
    override fun createDefaultPosition() = Unit
    override fun calculateMinSize(rendererProxy: RendererProxy): ViewWithMinSize {
        val childWithMinSize = children.single().withMinSize(rendererProxy)
        return object : ViewWithMinSize {
            override val minSize = getMinSize(childWithMinSize)
            override fun calculateSize(maxSize: IntPoint): ViewWithSize {
                val childWithSize = childWithMinSize.withSize(maxSize)
                return object : ViewWithSize {
                    override val size = getSize(childWithSize)
                    override fun assemble(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = assemble(childWithSize, offset, viewPlacer)
                }
            }
        }
    }

    protected open fun getMinSize(childWithMinSize: ChildWithMinSize) = childWithMinSize.minSize
    protected open fun getChildWithSize(childWithMinSize: ChildWithMinSize, maxSize: IntPoint) = childWithMinSize.withSize(maxSize)
    protected open fun getSize(childWithSize: ChildWithSize) = childWithSize.size
    protected open fun assemble(childWithSize: ChildWithSize, offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = childWithSize.assemble(offset, viewPlacer)

    val childView get() = children.single().view
}
