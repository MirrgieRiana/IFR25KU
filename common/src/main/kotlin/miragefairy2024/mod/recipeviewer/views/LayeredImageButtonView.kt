package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.util.EventRegistry
import miragefairy2024.util.ObservableValue
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation as Identifier // MIGRATION2612 ReplaceImport net.minecraft.resources.Identifier

class LayeredImageButtonView(size: IntPoint) : FixedWidgetView(size) {
    class Sprites(
        val selected: Identifier,
        val unselected: Identifier,
        val selectedFocused: Identifier,
        val unselectedFocused: Identifier,
    ) {
        companion object {
            fun of(texture: Identifier) = Sprites(texture, texture, texture, texture)
        }
    }

    var backgroundSprites: Sprites? = null
    var foregroundSprites: Sprites? = null
    val selected = ObservableValue(false)
    val tooltip = ObservableValue<Component?>(null)
    val onClick = EventRegistry<() -> Unit>()
}
