package miragefairy2024.mod.recipeviewer.views

abstract class ScrollbarView(horizontal: Boolean) : AbstractScrollbarView(5, horizontal) {
    var scrollbarColor = 0xA0000000.toInt()
}

class XScrollbarView : ScrollbarView(horizontal = true) {
    val scrollOffsetX get() = scrollOffset
}

class YScrollbarView : ScrollbarView(horizontal = false) {
    val scrollOffsetY get() = scrollOffset
}
