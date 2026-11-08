package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.ChildrenGenerator
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.evaluateActualSize
import miragefairy2024.util.ObservableValue
import miragefairy2024.util.observeAndInitialize

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
        var childController = ViewController.NONE
        var disposed = false
        var loading = false
        var needsLoad: Boolean

        fun load() {
            // 既に破棄済みの場合は、ロードをしないのだ～🌱
            if (disposed) return
            // 既にロード中の場合はフラグだけ立てて、そうでなければフラグを立てつつロードをするのだ～🌱
            needsLoad = true
            if (loading) return

            // ロード中フラグの管理なのだ～🌱
            loading = true
            try {

                // ロードの必要性が解消されない限り、ロードを繰り返すのだ～🌱
                while (needsLoad) {
                    needsLoad = false

                    // 古い子を破棄し、子の破棄中に破棄を食らった場合はロードをせずに終わるのだ～🌱
                    val oldChildController = childController
                    childController = ViewController.NONE
                    oldChildController.dispose()
                    oldChildController.detach()
                    if (disposed) break

                    // 子を実際にロードするのだ～🌱
                    var y = 0
                    // 指定されたページインデックスが無かった場合は、抜けるのだ～🌱
                    val page = pages.getOrNull(pageIndex.value) ?: break
                    val newChildController = page.attachTo(viewPlacer, currentOffset) {
                        val x = when (it.position) {
                            Alignment.START -> 0
                            Alignment.CENTER -> (evaluateActualSizeX() - it.view.evaluateActualSizeX()) / 2
                            Alignment.END -> evaluateActualSizeX() - it.view.evaluateActualSizeX()
                        }
                        val relativeOffset = IntPoint(x, y)
                        y += it.view.evaluateActualSizeY()
                        relativeOffset
                    }
                    // 子のロード中に破棄された場合は、即子を破棄して抜けるのだ～🌱
                    if (disposed) {
                        newChildController.dispose()
                        newChildController.detach()
                        break
                    }
                    childController = newChildController

                }

            } finally {
                loading = false
            }
        }

        val pageIndexRemover = pageIndex.observeAndInitialize { _, _ -> load() }

        return object : ViewController {
            override fun setOffset(offset: IntPoint) {
                currentOffset = offset
                childController.setOffset(offset)
            }

            override fun detach() {
                childController.detach()
            }

            override fun dispose() {
                disposed = true
                childController.dispose()
                pageIndexRemover.remove()
            }
        }
    }

}

operator fun PagingView.plusAssign(childrenGenerator: ChildrenGenerator<Alignment>) = this.add(childrenGenerator)
