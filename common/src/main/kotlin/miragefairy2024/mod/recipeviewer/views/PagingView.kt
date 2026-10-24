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

        val controllerList = ViewControllerList()

        val childrenControllerList = ViewControllerList()
        fun load() {
            childrenControllerList.remove()

            var y = 0
            val page = pages.getOrNull(pageIndex.value) ?: return
            page.forEach {
                val x = when (it.position) {
                    Alignment.START -> 0
                    Alignment.CENTER -> (evaluateActualSizeX() - it.view.evaluateActualSizeX()) / 2
                    Alignment.END -> evaluateActualSizeX() - it.view.evaluateActualSizeX()
                }
                childrenControllerList += it.view.attachTo(offset.offset(x, y), viewPlacer)
                y += it.view.evaluateActualSizeY()
            }
        }
        controllerList += childrenControllerList

        val pageIndexRemover = pageIndex.register { _, _ -> load() }
        controllerList += object : ViewController {
            override fun remove() {
                pageIndexRemover.remove()
            }
        }
        load()

        return controllerList
    }

}

operator fun PagingView.plusAssign(childrenGenerator: ChildrenGenerator<Alignment>) = this.add(childrenGenerator)
