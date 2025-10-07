package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.ViewTexture
import miragefairy2024.util.EventRegistry
import miragefairy2024.util.ObservableValue

class ImageButtonView(size: IntPoint) : SolidView(size) {
    val texture: ViewTexture? = null
    val hoverTexture: ViewTexture? = null
    val disabledTexture: ViewTexture? = null
    val enabled = ObservableValue(true)
    val onClick = EventRegistry<() -> Unit>()
}
