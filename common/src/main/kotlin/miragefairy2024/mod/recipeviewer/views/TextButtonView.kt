package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.util.EventRegistry
import miragefairy2024.util.empty
import miragefairy2024.util.text
import net.minecraft.network.chat.Component

class TextButtonView(size: IntPoint) : FixedWidgetView(size) {
    var message: Component = text { empty() }
    val onClick = EventRegistry<() -> Unit>()
}
