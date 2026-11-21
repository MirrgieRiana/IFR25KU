package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.Sizing

abstract class ScrollbarView : AbstractScrollbarView(5) {
    var scrollbarColor = 0xA0000000.toInt()
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
