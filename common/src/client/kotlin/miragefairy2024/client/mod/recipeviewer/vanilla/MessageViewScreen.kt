package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.MessageViewScreenDefinition
import miragefairy2024.client.mod.getKeyMapping
import miragefairy2024.mod.recipeviewer.vanilla.ScreenEventSource
import net.minecraft.client.gui.GuiGraphics as GuiGraphicsExtractor // MIGRATION2612 ReplaceImport net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen

class MessageViewScreen<T>(private val parent: Screen?, private val definition: MessageViewScreenDefinition<T>) : ViewScreen(definition.title) {

    private val state = definition.createScreenState()
    private var openingKeyCode: Int? = null

    override fun createRootView(screenEventSource: ScreenEventSource) = definition.rootViewFactory(screenEventSource, state)

    fun suppressOpeningKey(keyCode: Int) {
        openingKeyCode = keyCode
    }

    override fun renderBackground(guiGraphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTick: Float) {
        if (definition.transparentBackground) {
            renderTransparentBackground(guiGraphics)
        } else {
            super.renderBackground(guiGraphics, mouseX, mouseY, partialTick)
        }
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (keyCode == openingKeyCode) return true
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

    override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (keyCode == openingKeyCode) {
            openingKeyCode = null
            return true
        }
        return super.keyReleased(keyCode, scanCode, modifiers)
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

    override fun removed() {
        openingKeyCode = null
        super.removed()
    }

}
