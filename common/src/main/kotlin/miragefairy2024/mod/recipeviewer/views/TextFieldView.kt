package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.util.ObservableValue
import net.minecraft.network.chat.Component

class TextFieldView(size: IntPoint) : FixedWidgetView(size) {
    val text = ObservableValue("")
    var hint: Component? = null
}
