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
        fun evaluateAlignmentOffset(alignment: Alignment, remaining: Int): Int {
            return when (alignment) {
                Alignment.START -> 0
                Alignment.CENTER -> remaining / 2
                Alignment.END -> remaining
            }
        }

        val relativeOffset = IntPoint(
            evaluateAlignmentOffset(alignmentX, evaluateActualSizeX() - childView.evaluateActualSizeX()),
            evaluateAlignmentOffset(alignmentY, evaluateActualSizeY() - childView.evaluateActualSizeY()),
        )
        val controller = super.attachTo(offset + relativeOffset, viewPlacer)
        return object : ViewController {
            override fun setOffset(offset: IntPoint) = controller.setOffset(offset + relativeOffset)
            override fun detach() = controller.detach()
            override fun dispose() = controller.dispose()
        }
    }

}
