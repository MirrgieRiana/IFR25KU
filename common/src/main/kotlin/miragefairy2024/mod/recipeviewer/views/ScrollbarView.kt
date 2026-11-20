package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.evaluateActualSize
import miragefairy2024.mod.recipeviewer.view.sized
import miragefairy2024.util.ObservableValue

/** スクロール位置をコンテンツ座標で保持し、再配置時にも引き継ぐバーなのだ～🌱 */
abstract class AbstractScrollbarView(initialScrollbarThickness: Int) : AbstractView(), PlaceableView {

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
    var scrollbarThickness = initialScrollbarThickness
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

abstract class ScrollbarView : AbstractScrollbarView(5)

class XScrollbarView : ScrollbarView() {

    override var sizingX = Sizing.FILL
    override var sizingY = Sizing.WRAP

    val scrollOffsetX = ObservableValue(0.0)

    override fun evaluateContentSizeXImpl() = 0
    override fun evaluateContentSizeYImpl() = scrollbarThickness

}

class YScrollbarView : ScrollbarView() {

    override var sizingX = Sizing.WRAP
    override var sizingY = Sizing.FILL

    val scrollOffsetY = ObservableValue(0.0)

    override fun evaluateContentSizeXImpl() = scrollbarThickness
    override fun evaluateContentSizeYImpl() = 0

}
