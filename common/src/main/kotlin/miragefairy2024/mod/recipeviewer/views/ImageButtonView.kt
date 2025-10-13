package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.ViewTexture
import miragefairy2024.util.EventRegistry
import miragefairy2024.util.ObservableValue

class ImageButtonView(size: IntPoint) : FixedWidgetView(size) {
    var texture: ViewTexture? = null
    var hoveredTexture: ViewTexture? = null
    var disabledTexture: ViewTexture? = null
    val enabled = ObservableValue(true)
    val onClick = EventRegistry<() -> Boolean>()
}
