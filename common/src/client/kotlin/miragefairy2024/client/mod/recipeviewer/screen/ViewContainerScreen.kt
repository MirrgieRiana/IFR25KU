package miragefairy2024.client.mod.recipeviewer.screen

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.View
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.AbstractContainerMenu

/**
 * 中身をViewのツリーで組むメニューの画面の土台なのだ～🌱
 * メニューを持たない画面は、[miragefairy2024.client.mod.recipeviewer.screen.ViewScreen]を使うのだ～🌱
 */
abstract class ViewContainerScreen<T : AbstractContainerMenu>(menu: T, playerInventory: Inventory, title: Component) : AbstractContainerScreen<T>(menu, playerInventory, title) {

    /** 画面の中身のViewのツリーを作るのだ～🌱 */
    protected abstract fun createRootView(): View

    /**
     * Viewのツリーを再構築して、ウィジェットを登録するのだ～🌱
     * 親クラスは、画面の寸法が変わる度に、ウィジェットを捨ててからこれを呼ぶのだ～🌱
     */
    override fun init() {
        // バニラはスロットの座標と当たり判定をこの原点からの相対で扱うため、Viewが決めた絶対座標をそのまま渡せるようにするのだ～🌱
        leftPos = 0
        topPos = 0

        val adapter = ViewScreenAdapter(createRootView())
        adapter.layout(IntPoint.ZERO, IntPoint(width, height))
        adapter.widgets.forEach {
            addRenderableWidget(it)
        }
    }

    /** 枠の絵はViewのツリーが描くのだ～🌱 */
    final override fun renderBg(guiGraphics: GuiGraphics, partialTick: Float, mouseX: Int, mouseY: Int) = Unit

    /** 題名と持ち物の名前もViewのツリーが描くのだ～🌱 */
    final override fun renderLabels(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) = Unit

}
