package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.ChildrenGenerator
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.Remover
import miragefairy2024.mod.recipeviewer.view.RendererProxy
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.ViewWithMinSize
import miragefairy2024.mod.recipeviewer.view.ViewWithSize
import miragefairy2024.mod.recipeviewer.view.offset
import miragefairy2024.util.ObservableValue
import miragefairy2024.util.register

class PagingView : ParentView<Alignment>() {
    val childrenGenerators = mutableListOf<ChildrenGenerator<Alignment>>()
    var pageCount: Int = 0
        private set
    val pageIndex = ObservableValue(0)

    override fun createDefaultPosition() = Alignment.START
    override fun calculateMinSize(rendererProxy: RendererProxy): ViewWithMinSize {
        return object : ViewWithMinSize {
            override val minSize = IntPoint.Companion.ZERO
            override fun calculateSize(maxSize: IntPoint): ViewWithSize {
                val children = childrenGenerators.flatMap { it.generateChildren(rendererProxy, maxSize) }
                val childrenWithMinSize = children.map { it.withMinSize(rendererProxy) }
                val childrenWithSize = childrenWithMinSize.map { it.withSize(it.minSize) }

                val pages = mutableListOf<List<ParentView<Alignment>.ChildWithSize>>().also { pages ->
                    var page = mutableListOf<ParentView<Alignment>.ChildWithSize>()
                    var y = 0
                    childrenWithSize.forEach {
                        if (page.isNotEmpty() && y + it.size.y > maxSize.y) {
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
                }
                pageCount = pages.size

                return object : ViewWithSize {
                    override val size = maxSize
                    override fun assemble(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): Remover {
                        val childrenRemovers = mutableListOf<Remover>()

                        fun clear() {
                            childrenRemovers.forEach {
                                it.remove()
                            }
                            childrenRemovers.clear()
                        }

                        fun load() {
                            clear()

                            var childY = 0
                            val page = pages.getOrNull(pageIndex.value) ?: return
                            page.forEach {
                                childrenRemovers += it.assemble(offset.offset(0, childY), viewPlacer)
                                childY += it.size.y
                            }
                        }

                        val pageIndexEventRemover = pageIndex.register { _, _ ->
                            load()
                        }
                        load()

                        return Remover {
                            clear()
                            pageIndexEventRemover()
                        }
                    }
                }
            }
        }
    }

    fun add(childrenGenerator: ChildrenGenerator<Alignment>) {
        childrenGenerators += childrenGenerator
    }
}

operator fun PagingView.plusAssign(childrenGenerator: ChildrenGenerator<Alignment>) = this.add(childrenGenerator)
