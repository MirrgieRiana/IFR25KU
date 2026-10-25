package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.ChildrenGenerator
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.ViewControllerList
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.evaluateActualSize
import miragefairy2024.mod.recipeviewer.view.plusAssign
import miragefairy2024.util.ObservableValue
import miragefairy2024.util.register

class PagingView : ParentView<Alignment>() {

    val childrenGenerators = mutableListOf<ChildrenGenerator<Alignment>>()

    fun add(childrenGenerator: ChildrenGenerator<Alignment>) {
        childrenGenerators += childrenGenerator
    }


    override fun createDefaultPosition() = Alignment.START

    override fun evaluateContentSizeXImpl() = 0
    override fun evaluateContentSizeYImpl() = 0

    override var sizingX = Sizing.FILL
    override var sizingY = Sizing.FILL

    override fun evaluateChildRegionSizeX(index: Int) = evaluateActualSizeX()
    override fun evaluateChildRegionSizeY(index: Int) = evaluateActualSizeY()

    val pageCount = ObservableValue(0)

    val pageIndex = ObservableValue(0)

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): ViewController {
        val children = childrenGenerators.flatMap { it.generateChildren(renderingProxy, evaluateActualSize()) }
        children.initSizeCalculation()

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

        var currentOffset = offset
        val childrenControllerList = ViewControllerList()
        fun load() {
            childrenControllerList.detach()
            childrenControllerList.clear()

            var y = 0
            val page = pages.getOrNull(pageIndex.value) ?: return
            childrenControllerList += page.attachTo(viewPlacer, currentOffset) {
                val x = when (it.position) {
                    Alignment.START -> 0
                    Alignment.CENTER -> (evaluateActualSizeX() - it.view.evaluateActualSizeX()) / 2
                    Alignment.END -> evaluateActualSizeX() - it.view.evaluateActualSizeX()
                }
                val relativeOffset = IntPoint(x, y)
                y += it.view.evaluateActualSizeY()
                relativeOffset
            }
        }
        val pageIndexRemover = pageIndex.register { _, _ -> load() }
        load()

        return object : ViewController {
            override fun setOffset(offset: IntPoint) {
                currentOffset = offset
                childrenControllerList.setOffset(offset)
            }

            override fun detach() {
                childrenControllerList.detach()
                pageIndexRemover.remove()
            }

            override fun dispose() {
                childrenControllerList.dispose()
                pageIndexRemover.remove()
            }
        }
    }

}

operator fun PagingView.plusAssign(childrenGenerator: ChildrenGenerator<Alignment>) = this.add(childrenGenerator)
