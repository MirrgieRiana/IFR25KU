package miragefairy2024.client.mod.recipeviewer.screen

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.View
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

/** 中身をViewのツリーで組む画面の土台なのだ～🌱 */
abstract class ViewScreen(title: Component) : Screen(title) {

    /** 画面の中身のViewのツリーを作るのだ～🌱 */
    protected abstract fun createRootView(): View

    /**
     * Viewのツリーを再構築して、ウィジェットを登録するのだ～🌱
     * 親クラスは、画面の寸法が変わる度に、ウィジェットを捨ててからこれを呼ぶのだ～🌱
     */
    override fun init() {
        val adapter = ViewScreenAdapter(createRootView())
        adapter.layout(IntPoint.ZERO, IntPoint(width, height))
        adapter.widgets.forEach {
            addRenderableWidget(it)
        }
    }

}
