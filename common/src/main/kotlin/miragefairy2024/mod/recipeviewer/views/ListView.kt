package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.plus
import mirrg.kotlin.helium.atLeast

abstract class ListView : ContainerView<Alignment>() {

    override fun createDefaultPosition() = Alignment.START

}

class XListView : ListView() {

    @JvmField
    var minHeight = 0

    override fun calculateContentSize(): IntPoint {
        return IntPoint(
            children.sumOf { it.view.contentSize.x },
            (children.maxOfOrNull { it.view.contentSize.y } ?: 0) atLeast minHeight,
        )
    }

    override fun calculateActualSize(): IntPoint {
        return IntPoint(
            children.sumOf { it.view.actualSize.x },
            (children.maxOfOrNull { it.view.actualSize.y } ?: 0) atLeast minHeight,
        )
    }

    override fun calculateActualSize(renderingProxy: RenderingProxy) {
        super.calculateActualSize(renderingProxy)
        var x = 0
        children.forEach {
            it.offsetCache = IntPoint(
                x,
                when (it.position) {
                    Alignment.START -> 0
                    Alignment.CENTER -> (actualSize.y - it.view.actualSize.y) / 2
                    Alignment.END -> actualSize.y - it.view.actualSize.y
                },
            )
            x += it.view.actualSize.x
        }
    }

    override fun calculateChildrenActualSize() {
        children.forEach {
            it.view.calculateActualSize(renderingProxy)
        }
    }

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) {
        children.forEach {
            it.view.attachTo(offset + it.offsetCache, viewPlacer)
        }
    }

}

class YListView : ListView() {

    @JvmField
    var minWidth = 0

    override fun calculateContentSize(): IntPoint {
        return IntPoint(
            (children.maxOfOrNull { it.view.contentSize.x } ?: 0) atLeast minWidth,
            children.sumOf { it.view.contentSize.y },
        )
    }

    override fun calculateActualSize(): IntPoint {
        return IntPoint(
            (children.maxOfOrNull { it.view.actualSize.x } ?: 0) atLeast minWidth,
            children.sumOf { it.view.actualSize.y },
        )
    }

    override fun calculateActualSize(renderingProxy: RenderingProxy) {
        super.calculateActualSize(renderingProxy)
        var y = 0
        children.forEach {
            it.offsetCache = IntPoint(
                when (it.position) {
                    Alignment.START -> 0
                    Alignment.CENTER -> (actualSize.x - it.view.actualSize.x) / 2
                    Alignment.END -> actualSize.x - it.view.actualSize.x
                },
                y,
            )
            y += it.view.actualSize.y
        }
    }

    override fun calculateChildrenActualSize() {
        children.forEach {
            it.view.calculateActualSize(renderingProxy)
        }
    }

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) {
        children.forEach {
            it.view.attachTo(offset + it.offsetCache, viewPlacer)
        }
    }

}
