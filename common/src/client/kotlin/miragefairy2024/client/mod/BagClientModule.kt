package miragefairy2024.client.mod

import io.wispforest.owo.ui.base.BaseOwoHandledScreen
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.HorizontalAlignment
import io.wispforest.owo.ui.core.Insets
import io.wispforest.owo.ui.core.OwoUIAdapter
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.Surface
import io.wispforest.owo.ui.core.VerticalAlignment
import miragefairy2024.ModContext
import miragefairy2024.client.util.inventoryNameLabel
import miragefairy2024.client.util.registerHandledScreen
import miragefairy2024.client.util.slotContainer
import miragefairy2024.client.util.verticalSpace
import miragefairy2024.mod.BagCard
import miragefairy2024.mod.BagScreenHandler
import mirrg.kotlin.helium.atLeast
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory

context(ModContext)
fun initBagClientModule() {
    BagCard.screenHandlerType.registerHandledScreen { gui, inventory, title -> BagScreen(gui, inventory, title) }
}

class BagScreen(handler: BagScreenHandler, private val playerInventory: Inventory, title: Component) : BaseOwoHandledScreen<FlowLayout, BagScreenHandler>(handler, playerInventory, title) {
    override fun createAdapter(): OwoUIAdapter<FlowLayout> = OwoUIAdapter.create(this, Containers::verticalFlow)
    override fun build(rootComponent: FlowLayout) {
        val card = menu.card ?: return

        rootComponent.apply {
            surface(Surface.VANILLA_TRANSLUCENT)
            verticalAlignment(VerticalAlignment.CENTER)
            horizontalAlignment(HorizontalAlignment.CENTER)

            child(Containers.verticalFlow(Sizing.content(), Sizing.content()).apply { // 外枠
                surface(Surface.PANEL)
                padding(Insets.of(7))

                child(Containers.verticalFlow(Sizing.fixed(18 * (card.inventoryWidth atLeast 9)), Sizing.content()).apply { // 内枠

                    child(inventoryNameLabel(title)) // GUI名

                    child(verticalSpace(3))

                    // カバンインベントリ
                    repeat(card.inventoryHeight) { r ->
                        child(Containers.horizontalFlow(Sizing.fill(100), Sizing.content()).apply {
                            alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
                            repeat(card.inventoryWidth) { c ->
                                child(slotContainer(slotAsComponent(9 * 4 + card.inventoryWidth * r + c)))
                            }
                        })
                    }

                    child(verticalSpace(3))

                    child(inventoryNameLabel(playerInventory.name))

                    child(verticalSpace(1))

                    // プレイヤーインベントリ
                    repeat(3) { r ->
                        child(Containers.horizontalFlow(Sizing.fill(100), Sizing.content()).apply {
                            alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
                            repeat(9) { c ->
                                child(slotContainer(slotAsComponent(9 * r + c)))
                            }
                        })
                    }
                    child(verticalSpace(4))
                    child(Containers.horizontalFlow(Sizing.fill(100), Sizing.content()).apply {
                        alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
                        repeat(9) { c ->
                            child(slotContainer(slotAsComponent(9 * 3 + c)))
                        }
                    })

                })
            })
        }
    }
}
