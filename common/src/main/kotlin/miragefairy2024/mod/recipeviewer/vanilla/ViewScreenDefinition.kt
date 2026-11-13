package miragefairy2024.mod.recipeviewer.vanilla

import miragefairy2024.mod.recipeviewer.view.View
import net.minecraft.network.chat.Component

class ViewScreenDefinition(val title: Component, val rootViewFactory: () -> View) {
    var transparentBackground = true
    var closeOnInventoryKey = true
    var closeOnKeyPressed: (Int, Int, Int) -> Boolean = { _, _, _ -> false }
}
