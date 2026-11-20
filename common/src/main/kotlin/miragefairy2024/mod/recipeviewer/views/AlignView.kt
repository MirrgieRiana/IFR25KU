package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.plus

class AlignView : WrapperView() {

    override var sizingX = Sizing.FILL
    override var sizingY = Sizing.FILL
    var alignmentX = Alignment.START
    var alignmentY = Alignment.START

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): ViewController {
        val relativeOffsetX = when (alignmentX) {
            Alignment.START -> 0
            Alignment.CENTER -> (evaluateActualSizeX() - childView.evaluateActualSizeX()) / 2
            Alignment.END -> evaluateActualSizeX() - childView.evaluateActualSizeX()
        }
        val relativeOffsetY = when (alignmentY) {
            Alignment.START -> 0
            Alignment.CENTER -> (evaluateActualSizeY() - childView.evaluateActualSizeY()) / 2
            Alignment.END -> evaluateActualSizeY() - childView.evaluateActualSizeY()
        }
        val relativeOffset = IntPoint(relativeOffsetX, relativeOffsetY)
        val controller = super.attachTo(offset + relativeOffset, viewPlacer)
        return object : ViewController {
            override fun setOffset(offset: IntPoint) = controller.setOffset(offset + relativeOffset)
            override fun detach() = controller.detach()
            override fun dispose() = controller.dispose()
        }
    }

}
