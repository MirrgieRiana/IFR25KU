package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.offset

abstract class ListView : ContainerView<ListView.Position>() {

    class Position {
        var alignment = Alignment.START
    }

    override fun createDefaultPosition() = Position()

}

class XListView : ListView() {

    override fun calculateContentSizeImpl(): IntPoint {
        return IntPoint(
            children.sumOf { it.view.contentSize.x },
            (children.maxOfOrNull { it.view.contentSize.y } ?: 0),
        )
    }

    override fun calculateActualSizeImpl(regionSize: IntPoint): IntPoint {
        return IntPoint(
            children.sumOf { it.view.actualSize.x },
            (children.maxOfOrNull { it.view.actualSize.y } ?: 0),
        )
    }

    override fun calculateChildrenActualSize() {
        children.calculateActualSize { actualSize }
    }

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) {
        var x = 0
        children.attachTo(viewPlacer) {
            val childX = x
            val childY = when (it.position.alignment) {
                Alignment.START -> 0
                Alignment.CENTER -> (actualSize.y - it.view.actualSize.y) / 2
                Alignment.END -> actualSize.y - it.view.actualSize.y
            }
            x += it.view.actualSize.x
            offset.offset(childX, childY)
        }
    }

}

class YListView : ListView() {

    override fun calculateContentSizeImpl(): IntPoint {
        return IntPoint(
            (children.maxOfOrNull { it.view.contentSize.x } ?: 0),
            children.sumOf { it.view.contentSize.y },
        )
    }

    override fun calculateActualSizeImpl(regionSize: IntPoint): IntPoint {
        return IntPoint(
            (children.maxOfOrNull { it.view.actualSize.x } ?: 0),
            children.sumOf { it.view.actualSize.y },
        )
    }

    override fun calculateChildrenActualSize() {
        children.calculateActualSize { actualSize }
    }

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) {
        var y = 0
        children.attachTo(viewPlacer) {
            val childY = y
            val childX = when (it.position.alignment) {
                Alignment.START -> 0
                Alignment.CENTER -> (actualSize.x - it.view.actualSize.x) / 2
                Alignment.END -> actualSize.x - it.view.actualSize.x
            }
            y += it.view.actualSize.y
            offset.offset(childX, childY)
        }
    }

}
