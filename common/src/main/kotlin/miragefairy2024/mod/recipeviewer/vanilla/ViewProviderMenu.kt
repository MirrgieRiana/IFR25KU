package miragefairy2024.mod.recipeviewer.vanilla

import miragefairy2024.mod.recipeviewer.view.View
import net.minecraft.network.chat.Component

/** 画面のルート View を構築するメニューが実装するのだ～🌱 */
interface ViewProviderMenu<T> {

    /** 画面インスタンスの生成時に一度だけ呼ばれ、戻り値は同じ画面でルート View を作り直すときにも使われるのだ～🌱*/
    fun createScreenState(): T

    /** GUI の寸法を決め、画面の中央へ配置する View のツリーを作るのだ～🌱 */
    fun createRootView(title: Component, playerInventoryTitle: Component, screenEventSource: ScreenEventSource, state: T): View
}
