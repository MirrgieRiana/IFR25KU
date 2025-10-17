package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.ChildrenGenerator
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.RemoverList
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.offset
import miragefairy2024.mod.recipeviewer.view.plusAssign
import miragefairy2024.util.ObservableValue
import miragefairy2024.util.Remover
import miragefairy2024.util.register

class PagingView : ParentView<Alignment>() {

    val childrenGenerators = mutableListOf<ChildrenGenerator<Alignment>>()

    fun add(childrenGenerator: ChildrenGenerator<Alignment>) {
        childrenGenerators += childrenGenerator
    }


    override fun createDefaultPosition() = Alignment.START

    override fun calculateContentSize() = IntPoint.ZERO

    override var sizingX = Sizing.FILL
    override var sizingY = Sizing.FILL

    private lateinit var pages: List<List<Child<Alignment, *>>>
    val pageCount = ObservableValue(0)

    val pageIndex = ObservableValue(0)

    override fun calculateChildrenActualSize() {
        val children = childrenGenerators.flatMap { it.generateChildren(renderingProxy, actualSize) }
        children.calculateContentSize()
        children.calculateActualSize { actualSize }

        val pages = mutableListOf<List<Child<Alignment, *>>>()
        var page = mutableListOf<Child<Alignment, *>>()
        var y = 0
        children.forEach {
            if (page.isNotEmpty() && y + it.view.actualSize.y > actualSize.y) {
                // ページが空でなく、これを追加するとはみ出す場合、新しいページを作る
                pages += page
                page = mutableListOf()
                y = 0
            }
            page += it
            y += it.view.actualSize.y
        }
        if (page.isNotEmpty()) pages += page
        if (pages.isEmpty()) pages += listOf<Child<Alignment, *>>()
        this.pages = pages
        pageCount.value = pages.size
    }

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): Remover {
        val removers = RemoverList()

        val childrenRemover = RemoverList()
        fun load() {
            childrenRemover.remove()

            var y = 0
            val page = pages.getOrNull(pageIndex.value) ?: return
            page.forEach {
                val x = when (it.position) {
                    Alignment.START -> 0
                    Alignment.CENTER -> (actualSize.x - it.view.actualSize.x) / 2
                    Alignment.END -> actualSize.x - it.view.actualSize.x
                }
                childrenRemover += it.view.attachTo(offset.offset(x, y), viewPlacer)
                y += it.view.actualSize.y
            }
        }
        removers += childrenRemover

        removers += pageIndex.register { _, _ -> load() }
        load()

        return removers
    }

}

operator fun PagingView.plusAssign(childrenGenerator: ChildrenGenerator<Alignment>) = this.add(childrenGenerator)
