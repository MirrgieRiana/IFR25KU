package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.util.Remover

open class WrapperView : ParentView<Unit>() {

    private var child: Child<Unit, *>? = null

    fun add(child: Child<Unit, *>) {
        if (this.child != null) throw IllegalStateException("WrapperView can have only one child.")
        this.child = child
    }

    val childView get() = child!!.view


    override fun createDefaultPosition() = Unit


    override val sizingX get() = childView.sizingX
    override val sizingY get() = childView.sizingY

    final override fun calculateChildrenContentSize() {
        childView.calculateContentSize(renderingProxy)
    }

    override fun calculateContentSize() = childView.contentSize

    override fun calculateChildrenActualSize() {
        childView.calculateActualSize(actualSize)
    }

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): Remover {
        return childView.attachTo(offset, viewPlacer)
    }

}

operator fun WrapperView.plusAssign(view: View) = this.add(Child(this.createDefaultPosition(), view))
operator fun WrapperView.plusAssign(child: Child<Unit, *>) = this.add(child)

fun <P, V : WrapperView> Child<P, *>.wrap(wrapper: V) = Child(this.position, wrapper.also {
    it += this@wrap.view
})

inline fun View(block: Child<Unit, WrapperView>.() -> Unit): View = WrapperView().apply { block(Child(Unit, this)) }.childView
