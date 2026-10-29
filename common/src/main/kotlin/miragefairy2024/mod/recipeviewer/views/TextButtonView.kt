package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.util.EventRegistry
import net.minecraft.network.chat.Component

class TextButtonView(size: IntPoint, val message: Component) : FixedWidgetView(size) {
    val onPress = EventRegistry<() -> Unit>()
}
