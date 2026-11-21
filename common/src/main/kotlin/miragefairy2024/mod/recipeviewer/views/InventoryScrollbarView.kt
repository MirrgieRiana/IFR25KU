package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.Sizing

abstract class InventoryScrollbarView : AbstractScrollbarView(18)

class XInventoryScrollbarView : InventoryScrollbarView() {

    override var sizingX = Sizing.FILL
    override var sizingY = Sizing.WRAP

    override fun evaluateContentSizeXImpl() = 0
    override fun evaluateContentSizeYImpl() = scrollbarThickness

}

class YInventoryScrollbarView : InventoryScrollbarView() {

    override var sizingX = Sizing.WRAP
    override var sizingY = Sizing.FILL

    override fun evaluateContentSizeXImpl() = scrollbarThickness
    override fun evaluateContentSizeYImpl() = 0

}
