package miragefairy2024.mod.recipeviewer.vanilla

import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.util.KeyMapping
import net.minecraft.network.chat.Component

class ViewScreenDefinition(val title: Component, val rootViewFactory: () -> View) {
    var transparentBackground = true
    var closeOnInventoryKey = true
    var additionalCloseKey: KeyMapping? = null
}
