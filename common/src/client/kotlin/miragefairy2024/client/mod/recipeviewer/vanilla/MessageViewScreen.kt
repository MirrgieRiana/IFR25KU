package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.mod.recipeviewer.vanilla.ViewScreenDefinition
import miragefairy2024.mod.recipeviewer.view.View
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

class MessageViewScreen(parent: Screen?, title: Component, rootViewFactory: () -> View) : SimpleViewScreen(parent, ViewScreenDefinition(title, rootViewFactory)) {

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (super.mouseClicked(mouseX, mouseY, button)) return true
        onClose()
        return true
    }

}
