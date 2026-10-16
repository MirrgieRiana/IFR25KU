package miragefairy2024.client.util

import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.Sizing
import miragefairy2024.MirageFairy2024
import net.minecraft.client.gui.components.WidgetSprites
import net.minecraft.resources.ResourceLocation

typealias OwoComponent = io.wispforest.owo.ui.core.Component

enum class SlotType(val texture: ResourceLocation) {
    NORMAL(MirageFairy2024.identifier("textures/gui/slot.png")),
    FAIRY(MirageFairy2024.identifier("textures/gui/fairy_slot.png")),
}

fun horizontalSpace(width: Int): FlowLayout = Containers.verticalFlow(Sizing.fixed(width), Sizing.content())

fun verticalSpace(height: Int): FlowLayout = Containers.verticalFlow(Sizing.content(), Sizing.fixed(height))

fun WidgetSprites(identifier: ResourceLocation): WidgetSprites = WidgetSprites(identifier, identifier)
