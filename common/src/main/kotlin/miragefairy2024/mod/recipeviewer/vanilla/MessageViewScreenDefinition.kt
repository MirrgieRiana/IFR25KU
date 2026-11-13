package miragefairy2024.mod.recipeviewer.vanilla

import miragefairy2024.mod.common.KeyMappingType
import miragefairy2024.mod.recipeviewer.view.View
import net.minecraft.network.chat.Component

class MessageViewScreenDefinition(val title: Component, val rootViewFactory: () -> View) {
    var transparentBackground = true
    var closeOnInventoryKey = true
    var additionalCloseKey: KeyMappingType? = null
    var closeOnOutsideClick = true
}
