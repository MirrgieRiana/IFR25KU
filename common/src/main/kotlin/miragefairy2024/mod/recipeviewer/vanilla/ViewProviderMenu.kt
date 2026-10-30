package miragefairy2024.mod.recipeviewer.vanilla

import miragefairy2024.mod.recipeviewer.view.View
import net.minecraft.network.chat.Component

/** 画面のルート View を構築するメニューが実装するのだ～🌱 */
interface ViewProviderMenu {
    /** GUI の寸法を決め、画面の中央へ配置する View のツリーを作るのだ～🌱 */
    fun createView(title: Component, playerInventoryTitle: Component): View
}
