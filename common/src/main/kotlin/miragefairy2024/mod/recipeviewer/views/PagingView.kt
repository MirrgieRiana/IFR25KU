package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.ChildrenGenerator
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.RemoverList
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.actualSize
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

    override fun getOwnContentSizeX() = 0
    override fun getOwnContentSizeY() = 0

    override var sizingX = Sizing.FILL
    override var sizingY = Sizing.FILL

    override fun createChildRegionSizeXFunction(): (Int) -> Int = { evaluateActualSizeX() }
    override fun createChildRegionSizeYFunction(): (Int) -> Int = { evaluateActualSizeY() }

    val pageCount = ObservableValue(0)

    val pageIndex = ObservableValue(0)

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): Remover {
        val children = childrenGenerators.flatMap { it.generateChildren(renderingProxy, actualSize) }
        children.initSizeCalculation()
        children.calculateContentSizeX()
        children.calculateContentSizeY()
        children.calculateActualSizeX()
        children.calculateActualSizeY()

        val pages = mutableListOf<List<Child<Alignment, *>>>()
        var page = mutableListOf<Child<Alignment, *>>()
        var y = 0
        children.forEach {
            if (page.isNotEmpty() && y + it.view.evaluateActualSizeY() > evaluateActualSizeY()) {
                // ページが空でなく、これを追加するとはみ出す場合、新しいページを作る
                pages += page
                page = mutableListOf()
                y = 0
            }
            page += it
            y += it.view.evaluateActualSizeY()
        }
        if (page.isNotEmpty()) pages += page
        if (pages.isEmpty()) pages += listOf<Child<Alignment, *>>()
        pageCount.value = pages.size

        val removers = RemoverList()

        val childrenRemover = RemoverList()
        fun load() {
            childrenRemover.remove()

            var y = 0
            val page = pages.getOrNull(pageIndex.value) ?: return
            page.forEach {
                val x = when (it.position) {
                    Alignment.START -> 0
                    Alignment.CENTER -> (evaluateActualSizeX() - it.view.evaluateActualSizeX()) / 2
                    Alignment.END -> evaluateActualSizeX() - it.view.evaluateActualSizeX()
                }
                childrenRemover += it.view.attachTo(offset.offset(x, y), viewPlacer)
                y += it.view.evaluateActualSizeY()
            }
        }
        removers += childrenRemover

        removers += pageIndex.register { _, _ -> load() }
        load()

        return removers
    }

}

operator fun PagingView.plusAssign(childrenGenerator: ChildrenGenerator<Alignment>) = this.add(childrenGenerator)
