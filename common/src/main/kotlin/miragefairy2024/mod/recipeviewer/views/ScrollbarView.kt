package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.evaluateActualSize
import miragefairy2024.mod.recipeviewer.view.sized
import miragefairy2024.util.EventRegistry
import miragefairy2024.util.ObservableValue

sealed class ScrollbarStyle(val defaultThickness: Int) {
    class Flat(var color: Int = 0xA0000000.toInt()) : ScrollbarStyle(5)
    object Inventory : ScrollbarStyle(18)
}

/** スクロール位置をコンテンツ座標で保持し、再配置時にも引き継ぐバーなのだ～🌱 */
abstract class ScrollbarView(val style: ScrollbarStyle, val horizontal: Boolean) : AbstractView(), PlaceableView {

    var keyboardFocusable = true
    val scrollOffset = ObservableValue(0.0)

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
    var scrollbarThickness = style.defaultThickness
        set(value) {
            if (value < 0) throw IllegalArgumentException("Scrollbar thickness must be non-negative")
            field = value
        }
    var scrollStep = 15
        set(value) {
            if (value <= 0) throw IllegalArgumentException("Scroll step must be positive")
            field = value
        }

    val onScrollInput = EventRegistry<() -> Unit>()

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = viewPlacer.place(this, offset.sized(evaluateActualSize()))

}

class XScrollbarView(style: ScrollbarStyle) : ScrollbarView(style, true) {

    override var sizingX = Sizing.FILL
    override var sizingY = Sizing.WRAP

    override fun evaluateContentSizeXImpl() = 0
    override fun evaluateContentSizeYImpl() = scrollbarThickness

}

class YScrollbarView(style: ScrollbarStyle) : ScrollbarView(style, false) {

    override var sizingX = Sizing.WRAP
    override var sizingY = Sizing.FILL

    override fun evaluateContentSizeXImpl() = scrollbarThickness
    override fun evaluateContentSizeYImpl() = 0

}
