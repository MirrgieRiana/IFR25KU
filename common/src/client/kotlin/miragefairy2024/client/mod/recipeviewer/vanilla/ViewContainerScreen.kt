package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.client.mod.recipeviewer.renderingProxy
import miragefairy2024.mod.recipeviewer.vanilla.ViewProviderMenu
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.util.Remover
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.narration.NarratableEntry
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.Slot

/** 中身をViewのツリーで組むメニューの画面の土台なのだ～🌱 */
class ViewContainerScreen<T>(menu: T, playerInventory: Inventory, title: Component) : AbstractContainerScreen<T>(menu, playerInventory, title) where T : AbstractContainerMenu, T : ViewProviderMenu {

    private class SlotPlacement(val clipBounds: () -> IntRectangle?)

    /**
     * 配置解除後も Slot はメニューに残るため、未登録と解除済みを区別して保持するのだ～🌱
     * キーが存在して値が `null` の場合、そのスロットは配置解除済みなのだ～🌱
     */
    private val slotPlacementMap = mutableMapOf<Int, SlotPlacement?>()

    private val viewAdapter = ViewAdapter(object : ViewAdapterHost {

        override fun getRenderingProxy() = renderingProxy
        override fun getViewPlacerRegistry() = VANILLA_VIEW_PLACER_REGISTRY

        override fun getScreen() = this@ViewContainerScreen
        override fun canPlaceMenuSlots() = true
        override fun getRegionSizeX() = this@ViewContainerScreen.width
        override fun getRegionSizeY() = this@ViewContainerScreen.height

        override fun createRootView() = menu.createView(title, playerInventoryTitle)

        override fun getUiOffset() = IntPoint(this@ViewContainerScreen.leftPos, this@ViewContainerScreen.topPos)
        override fun setUiSize(uiSize: IntPoint) {
            this@ViewContainerScreen.imageWidth = uiSize.x
            this@ViewContainerScreen.imageHeight = uiSize.y
        }

        override fun init() {
            slotPlacementMap.clear()
            super@ViewContainerScreen.init()
        }

        override fun <T> addWidget(widget: T): Remover where T : GuiEventListener, T : NarratableEntry {
            this@ViewContainerScreen.addWidget(widget)
            return Remover { this@ViewContainerScreen.removeWidget(widget) }
        }

        override fun addRenderableWidget(widget: AbstractWidget): Remover {
            this@ViewContainerScreen.addRenderableWidget(widget)
            return Remover { this@ViewContainerScreen.removeWidget(widget) }
        }

        override fun addRenderableOnly(widget: AbstractWidget): Remover {
            this@ViewContainerScreen.addRenderableOnly(widget)
            return Remover { this@ViewContainerScreen.removeWidget(widget) }
        }

        /** [clipBounds] は取得時点の画面座標で返し、クリップしない場合は null を返すのだ～🌱 */
        override fun registerMenuSlotPlacement(slotId: Int, clipBounds: () -> IntRectangle?): Remover {
            val slotPlacement = SlotPlacement(clipBounds)
            slotPlacementMap[slotId] = slotPlacement
            return Remover {
                if (slotPlacementMap[slotId] === slotPlacement) slotPlacementMap[slotId] = null
            }
        }

    })

    override fun init() = viewAdapter.init()

    override fun removed() {
        viewAdapter.removed()
        slotPlacementMap.clear()
        super.removed()
    }

    private var slotScissorEnabled = false

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        viewAdapter.beforeRender()
        try {
            super.render(guiGraphics, mouseX, mouseY, partialTick)
        } finally {
            // 通常は通らないのだ～🌱
            disableSlotScissor(guiGraphics)
        }
        renderTooltip(guiGraphics, mouseX, mouseY)
    }

    private fun getSlotClipBounds(slot: Slot): IntRectangle? {
        if (slot.index !in slotPlacementMap) return null
        val placement = slotPlacementMap[slot.index] ?: return IntRectangle.ZERO
        return placement.clipBounds()
    }

    override fun renderSlot(guiGraphics: GuiGraphics, slot: Slot) {
        // 前のスロットの描画の終了処理なのだ～🌱
        disableSlotScissor(guiGraphics)
        val clipBounds = getSlotClipBounds(slot)
        if (clipBounds != null) {
            val (x, y, sizeX, sizeY) = clipBounds
            guiGraphics.enableScissor(x, y, x + sizeX, y + sizeY)
            slotScissorEnabled = true
        }
        // 親クラスが直後に描くハイライトにもこのスロットのクリップを適用するために、クリップの着脱を変なタイミングにしているのだ～🌱
        super.renderSlot(guiGraphics, slot)
    }

    private fun disableSlotScissor(guiGraphics: GuiGraphics) {
        if (!slotScissorEnabled) return
        slotScissorEnabled = false
        guiGraphics.disableScissor()
    }

    override fun removeWidget(listener: GuiEventListener) {
        super.removeWidget(listener)
        viewAdapter.removeWidget(listener)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        viewAdapter.beforeMouseClicked()
        val handled = super.mouseClicked(mouseX, mouseY, button)
        viewAdapter.mouseClicked(mouseX, mouseY)
        return handled
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, dragX: Double, dragY: Double): Boolean {
        if (viewAdapter.mouseDragged(mouseX, mouseY, button, dragX, dragY)) return true
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (viewAdapter.mouseReleased(mouseX, mouseY, button)) return true
        return super.mouseReleased(mouseX, mouseY, button)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        return viewAdapter.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (viewAdapter.keyPressed(keyCode, scanCode, modifiers)) return true
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    /** 枠の絵はViewのツリーが描くのだ～🌱 */
    override fun renderBg(guiGraphics: GuiGraphics, partialTick: Float, mouseX: Int, mouseY: Int) = Unit

    /** 題名とインベントリの名前もViewのツリーが描くのだ～🌱 */
    override fun renderLabels(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        // 最後のスロットのレンダリングの終了処理はここで受けるのだ～🌱
        disableSlotScissor(guiGraphics)
    }

}
