package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.evaluateActualSize
import miragefairy2024.mod.recipeviewer.view.sized
import miragefairy2024.util.ObservableValue

/** スクロール位置をコンテンツ座標で保持し、再配置時にも引き継ぐバーなのだ～🌱 */
abstract class AbstractScrollbarView(initialScrollbarThickness: Int, private val horizontal: Boolean) : AbstractView(), PlaceableView {

    override var sizingX = if (horizontal) Sizing.FILL else Sizing.WRAP
    override var sizingY = if (horizontal) Sizing.WRAP else Sizing.FILL

    protected val scrollOffset = ObservableValue(0.0)

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

    override fun evaluateContentSizeXImpl() = if (horizontal) 0 else scrollbarThickness
    override fun evaluateContentSizeYImpl() = if (horizontal) scrollbarThickness else 0

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = viewPlacer.place(this, offset.sized(evaluateActualSize()))

}
