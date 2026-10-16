package miragefairy2024.client.util

import miragefairy2024.MirageFairy2024
import net.minecraft.client.gui.components.WidgetSprites
import net.minecraft.resources.ResourceLocation

enum class SlotType(val texture: ResourceLocation) {
    NORMAL(MirageFairy2024.identifier("textures/gui/slot.png")),
    FAIRY(MirageFairy2024.identifier("textures/gui/fairy_slot.png")),
}

fun WidgetSprites(identifier: ResourceLocation): WidgetSprites = WidgetSprites(identifier, identifier)
