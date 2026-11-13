package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.client.mod.getKeyMapping
import miragefairy2024.mod.recipeviewer.vanilla.MessageViewScreenDefinition
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen

class MessageViewScreen(private val parent: Screen?, private val definition: MessageViewScreenDefinition) : ViewScreen(definition.title) {

    override fun createRootView() = definition.rootViewFactory()

    override fun renderBackground(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        if (definition.transparentBackground) {
            renderTransparentBackground(guiGraphics)
        } else {
            super.renderBackground(guiGraphics, mouseX, mouseY, partialTick)
        }
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (super.keyPressed(keyCode, scanCode, modifiers)) return true

        // インベントリキーで閉じるのだ～🌱
        if (definition.closeOnInventoryKey && minecraft!!.options.keyInventory.matches(keyCode, scanCode)) {
            onClose()
            return true
        }

        // 追加の閉じるキーで閉じるのだ～🌱
        val additionalCloseKey = definition.additionalCloseKey
        if (additionalCloseKey != null) {
            if (getKeyMapping(additionalCloseKey).matches(keyCode, scanCode)) {
                onClose()
                return true
            }
        }

        return false
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (super.mouseClicked(mouseX, mouseY, button)) return true

        // GUI 外部をクリックで閉じるのだ～🌱
        if (definition.closeOnOutsideClick) {
            onClose()
            return true
        }

        return false
    }

    override fun onClose() {
        minecraft!!.setScreen(parent)
    }

}
