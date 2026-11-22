package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.util.EventRegistry
import miragefairy2024.util.ObservableValue
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

class LayeredImageButtonView(
    size: IntPoint,
    val backgroundSprites: Sprites,
    val foregroundSprites: Sprites,
    tooltip: Component,
) : FixedWidgetView(size) {
    class Sprites(
        val selected: ResourceLocation,
        val unselected: ResourceLocation,
        val selectedFocused: ResourceLocation,
        val unselectedFocused: ResourceLocation,
    ) {
        constructor(texture: ResourceLocation) : this(texture, texture, texture, texture)
    }

    val selected = ObservableValue(false)
    val tooltip = ObservableValue(tooltip)
    val onClick = EventRegistry<() -> Unit>()
}
