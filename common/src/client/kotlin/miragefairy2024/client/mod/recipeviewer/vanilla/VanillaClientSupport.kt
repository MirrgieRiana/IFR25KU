package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.client.mod.recipeviewer.renderingProxy
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewPlacerRegistry
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.AbstractContainerMenu

val VANILLA_VIEW_PLACER_REGISTRY = ViewPlacerRegistry<VanillaContainerWidget>()

/** Viewのツリーを、1個のコンテナのウィジェットの中へ配置するのだ～🌱 */
class ViewVanillaAdapter(private val rootView: View) {

    /** 画面へ登録するウィジェットなのだ～🌱 Viewのツリーは、全部この中に入るのだ～🌱 */
    lateinit var widget: VanillaContainerWidget
        private set

    /** [offset]を左上として、[regionSize]の空間へViewのツリーを配置するのだ～🌱 */
    fun layout(offset: IntPoint, regionSize: IntPoint) {
        widget = VanillaContainerWidget(offset.x, offset.y, regionSize.x, regionSize.y)
        rootView.calculateContentSize(renderingProxy)
        rootView.calculateActualSize(regionSize)
        rootView.attachTo(offset) { view, bounds ->
            VANILLA_VIEW_PLACER_REGISTRY.place(widget, view, bounds)
        }
    }

}

/** 中身をViewのツリーで組むメニューの画面の土台なのだ～🌱 */
abstract class ViewContainerScreen<T : AbstractContainerMenu>(menu: T, playerInventory: Inventory, title: Component) : AbstractContainerScreen<T>(menu, playerInventory, title) {

    /** 画面の中身のViewのツリーを作るのだ～🌱 */
    protected abstract fun createRootView(): View

    /**
     * Viewのツリーを再構築して、コンテナのウィジェットを登録するのだ～🌱
     * 親クラスは、画面の寸法が変わる度に、ウィジェットを捨ててからこれを呼ぶのだ～🌱
     */
    override fun init() {
        super.init()
        val adapter = ViewVanillaAdapter(createRootView())
        adapter.layout(IntPoint.ZERO, IntPoint(width, height))
        addRenderableWidget(adapter.widget)
    }

    /**
     * フォーカスされた子へドラッグを中継してから、スロットの操作へ渡すのだ～🌱
     * [net.minecraft.client.gui.screens.inventory.AbstractContainerScreen] はスロットの操作のためにこれを上書きしていて、
     * [net.minecraft.client.gui.components.events.ContainerEventHandler] の既定の実装と違って子へ渡さないのだ～🌱
     */
    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, dragX: Double, dragY: Double): Boolean {
        val focused = focused
        if (focused != null && isDragging && button == 0 && focused.mouseDragged(mouseX, mouseY, button, dragX, dragY)) return true
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY)
    }

    /** 枠の絵はViewのツリーが描くのだ～🌱 */
    final override fun renderBg(guiGraphics: GuiGraphics, partialTick: Float, mouseX: Int, mouseY: Int) = Unit

    /** 題名と持ち物の名前もViewのツリーが描くのだ～🌱 */
    final override fun renderLabels(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) = Unit

}
