package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.ChildrenGenerator
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.Remover
import miragefairy2024.mod.recipeviewer.view.RemoverList
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.offset
import miragefairy2024.mod.recipeviewer.view.plusAssign
import miragefairy2024.util.ObservableValue
import miragefairy2024.util.register

class PagingView : ParentView<Alignment>() {

    val childrenGenerators = mutableListOf<ChildrenGenerator<Alignment>>()

    fun add(childrenGenerator: ChildrenGenerator<Alignment>) {
        childrenGenerators += childrenGenerator
    }


    override fun createDefaultPosition() = Alignment.START


    override fun calculateMinSizeImpl() = IntPoint.Companion.ZERO


    private lateinit var pages: List<List<ParentView<Alignment>.ChildWithSize>>
    val pageCount get() = pages.size

    val pageIndex = ObservableValue(0)

    override fun calculateSizeImpl(regionSize: IntPoint): IntPoint {
        val children = childrenGenerators.flatMap { it.generateChildren(rendererProxy, regionSize) }
        val childrenWithMinSize = children.map { it.withMinSize(rendererProxy) }
        val childrenWithSize = childrenWithMinSize.map { it.withSize(regionSize) }

        val pages = mutableListOf<List<ParentView<Alignment>.ChildWithSize>>()
        var page = mutableListOf<ParentView<Alignment>.ChildWithSize>()
        var y = 0
        childrenWithSize.forEach {
            if (page.isNotEmpty() && y + it.size.y > regionSize.y) {
                // ページが空でなく、これを追加するとはみ出す場合、新しいページを作る
                pages += page
                page = mutableListOf()
                y = 0
            }
            page += it
            y += it.size.y
        }
        if (page.isNotEmpty()) pages += page
        if (pages.isEmpty()) pages += listOf<ParentView<Alignment>.ChildWithSize>()
        this.pages = pages

        return regionSize
    }


    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): Remover {
        val childrenRemover = RemoverList()

        fun load() {
            childrenRemover.remove()

            var childY = 0
            val page = pages.getOrNull(pageIndex.value) ?: return
            page.forEach {
                childrenRemover += it.attachTo(offset.offset(0, childY), viewPlacer)
                childY += it.size.y
            }
        }

        val pageIndexEventRemover = pageIndex.register { _, _ ->
            load()
        }
        load()

        return Remover {
            childrenRemover.remove()
            pageIndexEventRemover()
        }
    }

}

operator fun PagingView.plusAssign(childrenGenerator: ChildrenGenerator<Alignment>) = this.add(childrenGenerator)
