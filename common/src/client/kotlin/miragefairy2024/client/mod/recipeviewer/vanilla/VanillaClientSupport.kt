package miragefairy2024.client.mod.recipeviewer.vanilla

import dev.architectury.platform.Platform
import miragefairy2024.client.mod.recipeviewer.renderingProxy
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewPlacerRegistry
import miragefairy2024.util.Remover
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.AbstractContainerMenu

class VanillaViewPlacerContext(
    val addRenderableWidget: (AbstractWidget) -> Remover,
    val addRenderableOnly: (AbstractWidget) -> Remover,
    /** バニラの GUI が座標の原点として使う点の、画面の左上からの絶対座標なのだ～🌱 */
    val origin: IntPoint,
)

val VANILLA_VIEW_PLACER_REGISTRY = ViewPlacerRegistry<VanillaViewPlacerContext>()

/** View のツリーを [net.minecraft.client.gui.screens.Screen] へ直接配置するのだ～🌱 */
class ViewVanillaAdapter(private val rootView: View, private val context: VanillaViewPlacerContext) {

    /** [offset] を左上として [regionSize] の空間へ View のツリーを配置し、一括解除用の [miragefairy2024.util.Remover] を返すのだ～🌱 */
    fun layout(offset: IntPoint, regionSize: IntPoint): Remover {
        rootView.calculateContentSize(renderingProxy)
        rootView.calculateActualSize(regionSize)
        return rootView.attachTo(offset) { view, bounds ->
            VANILLA_VIEW_PLACER_REGISTRY.place(context, view, bounds)
        }
    }

}

/** 中身をViewのツリーで組むメニューの画面の土台なのだ～🌱 */
abstract class ViewContainerScreen<T : AbstractContainerMenu>(menu: T, playerInventory: Inventory, title: Component) : AbstractContainerScreen<T>(menu, playerInventory, title) {

    private var viewRemover: Remover? = null

    /** 画面の中身のViewのツリーを作るのだ～🌱 */
    protected abstract fun createRootView(): View

    /**
     * View のツリーを再構築して、各ウィジェットを画面へ直接登録するのだ～🌱
     * 親クラスは、画面の寸法が変わる度に、ウィジェットを捨ててからこれを呼ぶのだ～🌱
     */
    override fun init() {
        removeView()
        super.init()
        fun register(widget: AbstractWidget, add: () -> Unit): Remover {
            add()
            var registered = true
            return Remover {
                if (registered) {
                    registered = false
                    removeWidget(widget)
                }
            }
        }
        val context = VanillaViewPlacerContext(
            addRenderableWidget = { widget -> register(widget) { addRenderableWidget(widget) } },
            addRenderableOnly = { widget -> register(widget) { addRenderableOnly(widget) } },
            origin = IntPoint(leftPos, topPos),
        )
        viewRemover = ViewVanillaAdapter(createRootView(), context).layout(IntPoint.ZERO, IntPoint(width, height))
    }

    private fun removeView() {
        val remover = viewRemover
        viewRemover = null
        if (remover != null) remover.remove()
    }

    override fun removed() {
        removeView()
        super.removed()
    }

    override fun removeWidget(listener: GuiEventListener) {
        super.removeWidget(listener)
        if (focused === listener && children().none { it === listener }) {
            focused = null
            isDragging = false
        }
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        val handled = super.mouseClicked(mouseX, mouseY, button)
        val focused = focused
        if (focused != null && children().none { it === focused }) {
            this.focused = null
            isDragging = false
        }
        return handled
    }

    /**
     * Fabric 以外ではフォーカスされた子へドラッグを中継してから、スロットの操作へ渡すのだ～🌱
     * Fabric では Fabric API が [net.minecraft.client.gui.screens.inventory.AbstractContainerScreen.mouseDragged] 内で子へ転送するため、手動では中継しないのだ～🌱
     */
    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, dragX: Double, dragY: Double): Boolean {
        if (!Platform.isFabric()) {
            val focused = focused
            if (focused != null) {
                if (isDragging) {
                    if (button == 0) {
                        if (focused.mouseDragged(mouseX, mouseY, button, dragX, dragY)) return true
                    }
                }
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY)
    }

    /** 枠の絵はViewのツリーが描くのだ～🌱 */
    final override fun renderBg(guiGraphics: GuiGraphics, partialTick: Float, mouseX: Int, mouseY: Int) = Unit

    /** 題名と持ち物の名前もViewのツリーが描くのだ～🌱 */
    final override fun renderLabels(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) = Unit

}
