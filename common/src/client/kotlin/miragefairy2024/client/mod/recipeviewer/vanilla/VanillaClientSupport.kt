package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.client.mod.recipeviewer.renderingProxy
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewPlacerRegistry
import miragefairy2024.util.Remover
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.AbstractContainerMenu

/** Viewを、バニラのGUIの子のウィジェットとして置く先なのだ～🌱 */
class VanillaViewPlacerContext {

    private class WidgetSlot(val widget: AbstractWidget)

    private val widgetSlots = mutableListOf<WidgetSlot>()
    var widgets = listOf<AbstractWidget>()

    fun add(widget: AbstractWidget): Remover {
        val widgetSlot = WidgetSlot(widget)
        widgetSlots += widgetSlot
        widgets = widgetSlots.map { it.widget }
        return Remover {
            widgetSlots -= widgetSlot
            widgets = widgetSlots.map { it.widget }
        }
    }

}

infix fun VanillaViewPlacerContext.place(widget: AbstractWidget) = this.add(widget)

val VANILLA_VIEW_PLACER_REGISTRY = ViewPlacerRegistry<VanillaViewPlacerContext>()

/** Viewのツリーを、バニラのGUIの子のウィジェットの列へ変換するのだ～🌱 */
class ViewVanillaAdapter(private val rootView: View) {

    private val context = VanillaViewPlacerContext()

    /** [miragefairy2024.client.mod.recipeviewer.vanilla.ViewVanillaAdapter.layout]で置かれたウィジェットなのだ～🌱 GUIの子として登録するのだ～🌱 */
    val widgets get() = context.widgets

    /** [offset]を左上として、[regionSize]の空間へViewのツリーを配置するのだ～🌱 */
    fun layout(offset: IntPoint, regionSize: IntPoint) {
        rootView.calculateContentSize(renderingProxy)
        rootView.calculateActualSize(regionSize)
        rootView.attachTo(offset) { view, bounds ->
            VANILLA_VIEW_PLACER_REGISTRY.place(context, view, bounds)
        }
    }

}

/** 中身をViewのツリーで組むメニューの画面の土台なのだ～🌱 */
abstract class ViewContainerScreen<T : AbstractContainerMenu>(menu: T, playerInventory: Inventory, title: Component) : AbstractContainerScreen<T>(menu, playerInventory, title) {

    /** 画面の中身のViewのツリーを作るのだ～🌱 */
    protected abstract fun createRootView(): View

    /**
     * Viewのツリーを再構築して、ウィジェットを登録するのだ～🌱
     * 親クラスは、画面の寸法が変わる度に、ウィジェットを捨ててからこれを呼ぶのだ～🌱
     */
    override fun init() {
        super.init()
        val adapter = ViewVanillaAdapter(createRootView())
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
