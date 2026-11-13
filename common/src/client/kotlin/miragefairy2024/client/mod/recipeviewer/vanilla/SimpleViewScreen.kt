package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.mod.recipeviewer.vanilla.ViewScreenDefinition
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen

open class SimpleViewScreen(private val parent: Screen?, private val definition: ViewScreenDefinition) : ViewScreen(definition.title) {

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
        if (definition.closeOnInventoryKey && minecraft!!.options.keyInventory.matches(keyCode, scanCode) || definition.closeOnKeyPressed(keyCode, scanCode, modifiers)) {
            onClose()
            return true
        }
        return false
    }

    override fun onClose() {
        minecraft!!.setScreen(parent)
    }

}
