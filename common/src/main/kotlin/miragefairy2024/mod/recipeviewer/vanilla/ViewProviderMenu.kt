package miragefairy2024.mod.recipeviewer.vanilla

import miragefairy2024.mod.recipeviewer.view.View
import net.minecraft.network.chat.Component

/** 画面のルート View を構築するメニューが実装するのだ～🌱 */
interface ViewProviderMenu {
    fun createView(title: Component, playerInventoryTitle: Component): View
}
