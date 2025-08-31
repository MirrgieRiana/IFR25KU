package miragefairy2024.client.util

import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.component.LabelComponent
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.container.ScrollContainer
import io.wispforest.owo.ui.core.Color
import io.wispforest.owo.ui.core.HorizontalAlignment
import io.wispforest.owo.ui.core.Insets
import io.wispforest.owo.ui.core.Size
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.Surface
import miragefairy2024.MirageFairy2024
import net.minecraft.client.gui.components.WidgetSprites
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import io.wispforest.owo.ui.core.Component as OwoComponent

enum class SlotType(val texture: ResourceLocation) {
    NORMAL(MirageFairy2024.identifier("textures/gui/slot.png")),
    FAIRY(MirageFairy2024.identifier("textures/gui/fairy_slot.png")),
}

fun slotContainer(slotComponent: OwoComponent, type: SlotType? = SlotType.NORMAL): FlowLayout {
    return Containers.verticalFlow(Sizing.content(), Sizing.content()).apply {
        padding(Insets.of(1))
        if (type != null) surface(Surface.tiled(type.texture, 18, 18))
        allowOverflow(true)
        child(slotComponent)
    }
}

fun horizontalSpace(width: Int): FlowLayout = Containers.verticalFlow(Sizing.fixed(width), Sizing.content())

fun verticalSpace(height: Int): FlowLayout = Containers.verticalFlow(Sizing.content(), Sizing.fixed(height))

fun inventoryNameLabel(name: Component, horizontalTextAlignment: HorizontalAlignment? = HorizontalAlignment.LEFT): LabelComponent = Components.label(name).apply {
    margins(Insets.of(0, 0, 1, 1))
    sizing(Sizing.fill(100), Sizing.content())
    horizontalTextAlignment(horizontalTextAlignment)
    color(Color.ofRgb(0x404040))
}

fun verticalScroll(horizontalSizing: Sizing, verticalSizing: Sizing, scrollbarThiccness: Int, overlapped: Boolean = false): ScrollContainer<FlowLayout> {
    return Containers.verticalScroll(horizontalSizing, verticalSizing, Containers.verticalFlow(horizontalSizing, Sizing.content()).apply {
        if (!overlapped) padding(Insets.of(0, 0, 0, scrollbarThiccness))
    }).apply {
        scrollbarThiccness(scrollbarThiccness)
    }
}

fun horizontalScroll(horizontalSizing: Sizing, verticalSizing: Sizing, scrollbarThiccness: Int, overlapped: Boolean = false): ScrollContainer<FlowLayout> {
    return Containers.horizontalScroll(horizontalSizing, verticalSizing, Containers.horizontalFlow(Sizing.content(), verticalSizing).apply {
        if (!overlapped) padding(Insets.of(0, scrollbarThiccness, 0, 0))
    }).apply {
        scrollbarThiccness(scrollbarThiccness)
    }
}

fun tooltipContainer(horizontalSizing: Sizing, verticalSizing: Sizing): FlowLayout {
    return Containers.verticalFlow(horizontalSizing, verticalSizing).apply {
        surface(Surface.TOOLTIP)
        padding(Insets.of(4))
        allowOverflow(true)
    }
}

class CompressionHorizontalFlow(horizontalSizing: Sizing) : FlowLayout(horizontalSizing, Sizing.content(), Algorithm.HORIZONTAL) {
    private lateinit var leaderComponent: OwoComponent

    fun leader(component: OwoComponent) {
        this.leaderComponent = component
        child(component)
    }

    override fun layout(space: Size) {
        leaderComponent.inflate(space)
        verticalSizing(Sizing.fixed(leaderComponent.fullSize().height + padding.get().top + padding.get().bottom))
        applySizing()
        super.layout(space)
    }
}

class CompressionVerticalFlow(verticalSizing: Sizing) : FlowLayout(Sizing.content(), verticalSizing, Algorithm.VERTICAL) {
    private lateinit var leaderComponent: OwoComponent

    fun leader(component: OwoComponent) {
        this.leaderComponent = component
        child(component)
    }

    override fun layout(space: Size) {
        leaderComponent.inflate(space)
        horizontalSizing(Sizing.fixed(leaderComponent.fullSize().width + padding.get().left + padding.get().right))
        applySizing()
        super.layout(space)
    }
}

fun WidgetSprites(identifier: ResourceLocation): WidgetSprites = WidgetSprites(identifier, identifier)
