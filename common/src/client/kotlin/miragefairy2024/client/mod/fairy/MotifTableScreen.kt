package miragefairy2024.client.mod.fairy

import io.wispforest.owo.ui.base.BaseOwoHandledScreen
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.component.ItemComponent
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.container.ScrollContainer
import io.wispforest.owo.ui.core.Color
import io.wispforest.owo.ui.core.HorizontalAlignment
import io.wispforest.owo.ui.core.Insets
import io.wispforest.owo.ui.core.OwoUIAdapter
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.Surface
import io.wispforest.owo.ui.core.VerticalAlignment
import miragefairy2024.ModContext
import miragefairy2024.client.util.horizontalSpace
import miragefairy2024.client.util.registerHandledScreen
import miragefairy2024.client.util.verticalScroll
import miragefairy2024.mod.fairy.MotifTableScreenHandler
import miragefairy2024.mod.fairy.motifTableScreenHandlerType
import miragefairy2024.util.invoke
import miragefairy2024.util.plus
import miragefairy2024.util.text
import mirrg.kotlin.hydrogen.formatAs
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.item.Item

context(ModContext)
fun initMotifTableClientModule() {

    // GUI登録
    motifTableScreenHandlerType.registerHandledScreen { gui, inventory, title -> MotifTableScreen(gui, inventory, title) }

}

class MotifTableScreen(handler: MotifTableScreenHandler, playerInventory: Inventory, title: Component) : BaseOwoHandledScreen<FlowLayout, MotifTableScreenHandler>(handler, playerInventory, title) {
    override fun createAdapter(): OwoUIAdapter<FlowLayout> = OwoUIAdapter.create(this, Containers::verticalFlow)
    override fun build(rootComponent: FlowLayout) {
        rootComponent.apply {
            surface(Surface.VANILLA_TRANSLUCENT)
            padding(Insets.of(4))
            verticalAlignment(VerticalAlignment.CENTER)
            horizontalAlignment(HorizontalAlignment.CENTER)

            // GUIパネル外枠
            child(Containers.verticalFlow(Sizing.content(), Sizing.fill(100)).apply {
                surface(Surface.PANEL)
                padding(Insets.of(7))

                // メインコンテナ
                child(Containers.verticalFlow(Sizing.content(), Sizing.fill(100)).apply {

                    // スクロールパネル
                    child(verticalScroll(Sizing.content(), Sizing.fill(100), 18).apply {
                        scrollbar(ScrollContainer.Scrollbar.vanilla())

                        // スロットパネル
                        child().child(Containers.verticalFlow(Sizing.content(), Sizing.content()).apply {
                            padding(Insets.of(0, 0, 0, 3))

                            menu.chanceTable.forEach { chance ->
                                child(Containers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
                                    verticalAlignment(VerticalAlignment.CENTER)

                                    tooltip(ItemComponent.tooltipFromItem(chance.showingItemStack, Item.TooltipContext.of(Minecraft.getInstance().level), Minecraft.getInstance().player, null))

                                    child(Components.item(chance.showingItemStack))
                                    child(horizontalSpace(3))
                                    child(Components.label(chance.showingItemStack.hoverName).apply {
                                        sizing(Sizing.fixed(150), Sizing.content())
                                        horizontalTextAlignment(HorizontalAlignment.LEFT)
                                        verticalTextAlignment(VerticalAlignment.CENTER)
                                        color(Color.ofRgb(0x404040))
                                    })
                                    child(Components.label(text { (chance.item.weight * 100 formatAs "%.4f%%")() }).apply {
                                        sizing(Sizing.fixed(50), Sizing.content())
                                        horizontalTextAlignment(HorizontalAlignment.RIGHT)
                                        verticalTextAlignment(VerticalAlignment.CENTER)
                                        color(Color.ofRgb(0x404040))
                                    })
                                    child(Components.label(text { "x"() + (chance.item.item.count formatAs "%.2f")() }).apply {
                                        sizing(Sizing.fixed(80), Sizing.content())
                                        horizontalTextAlignment(HorizontalAlignment.RIGHT)
                                        verticalTextAlignment(VerticalAlignment.CENTER)
                                        color(Color.ofRgb(0x404040))
                                    })

                                })
                            }
                        })

                    })

                })

            })

        }
    }

    override fun renderLabels(context: GuiGraphics, mouseX: Int, mouseY: Int) = Unit
}
