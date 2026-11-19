package miragefairy2024.mod.recipeviewer.views

abstract class VanillaScrollbarView(horizontal: Boolean) : AbstractScrollbarView(18, horizontal)

class XVanillaScrollbarView : VanillaScrollbarView(horizontal = true) {
    val scrollOffsetX get() = scrollOffset
}

class YVanillaScrollbarView : VanillaScrollbarView(horizontal = false) {
    val scrollOffsetY get() = scrollOffset
}
