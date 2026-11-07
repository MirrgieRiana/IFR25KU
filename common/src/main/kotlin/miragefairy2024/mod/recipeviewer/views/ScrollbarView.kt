package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.ScrollState
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.evaluateActualSize
import miragefairy2024.mod.recipeviewer.view.sized

/** スクロール対象を参照せず、単独で配置できるスクロールバーなのだ～🌱 */
abstract class ScrollbarView : AbstractView(), PlaceableView {
    abstract override var sizingX: Sizing
    abstract override var sizingY: Sizing

    val scrollState = ScrollState()
    var scrollbarThickness = 5
        set(value) {
            require(value >= 0) { "Scrollbar thickness must be non-negative" }
            field = value
        }
    var scrollbarColor = 0xA0000000.toInt()
    var scrollStep = 15
        set(value) {
            require(value > 0) { "Scroll step must be positive" }
            field = value
        }

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
