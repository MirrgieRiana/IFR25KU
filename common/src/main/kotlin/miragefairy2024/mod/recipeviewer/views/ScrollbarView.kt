package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.evaluateActualSize
import miragefairy2024.mod.recipeviewer.view.sized

abstract class ScrollbarView : AbstractView(), PlaceableView {

    var contentLength = 0
        set(value) {
            if (value < 0) throw IllegalArgumentException("Content length must be non-negative")
            field = value
        }
    var viewportLength = 0
        set(value) {
            if (value < 0) throw IllegalArgumentException("Viewport length must be non-negative")
            field = value
        }
    var scrollbarThickness = 5
        set(value) {
            if (value < 0) throw IllegalArgumentException("Scrollbar thickness must be non-negative")
            field = value
        }
    var scrollStep = 15
        set(value) {
            if (value <= 0) throw IllegalArgumentException("Scroll step must be positive")
            field = value
        }
    var scrollbarColor = 0xA0000000.toInt()

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = viewPlacer.place(this, offset.sized(evaluateActualSize()))

}

class XScrollbarView : ScrollbarView() {

    override var sizingX = Sizing.FILL
    override var sizingY = Sizing.WRAP

    override fun evaluateContentSizeXImpl() = 0
    override fun evaluateContentSizeYImpl() = scrollbarThickness

}

class YScrollbarView : ScrollbarView() {

    override var sizingX = Sizing.WRAP
    override var sizingY = Sizing.FILL

    override fun evaluateContentSizeXImpl() = scrollbarThickness
    override fun evaluateContentSizeYImpl() = 0

}
