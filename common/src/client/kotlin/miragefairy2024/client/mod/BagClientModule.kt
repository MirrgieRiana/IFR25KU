package miragefairy2024.client.mod

import miragefairy2024.ModContext
import miragefairy2024.client.mod.recipeviewer.screen.MENU_SLOT_BACKGROUND_SIZE
import miragefairy2024.client.mod.recipeviewer.screen.ViewContainerScreen
import miragefairy2024.client.mod.recipeviewer.screen.menuSlotView
import miragefairy2024.client.util.SlotType
import miragefairy2024.client.util.registerHandledScreen
import miragefairy2024.mod.BagCard
import miragefairy2024.mod.BagScreenHandler
import miragefairy2024.mod.NinePatchTextureCard
import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.ColorPair
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.views.NinePatchImageView
import miragefairy2024.mod.recipeviewer.views.SpaceView
import miragefairy2024.mod.recipeviewer.views.StackView
import miragefairy2024.mod.recipeviewer.views.TextView
import miragefairy2024.mod.recipeviewer.views.View
import miragefairy2024.mod.recipeviewer.views.XListView
import miragefairy2024.mod.recipeviewer.views.YListView
import miragefairy2024.mod.recipeviewer.views.YSpaceView
import miragefairy2024.mod.recipeviewer.views.configure
import miragefairy2024.mod.recipeviewer.views.margin
import miragefairy2024.mod.recipeviewer.views.minContentSizeX
import miragefairy2024.mod.recipeviewer.views.plusAssign
import mirrg.kotlin.helium.atLeast
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory

context(ModContext)
fun initBagClientModule() {
    BagCard.screenHandlerType.registerHandledScreen { gui, inventory, title -> BagScreen(gui, inventory, title) }
}

private const val PLAYER_INVENTORY_WIDTH = 9

/** ホットバーを除いた段数なのだ～🌱 */
private const val PLAYER_INVENTORY_HEIGHT = 3

private const val PANEL_MARGIN = 7

private const val INVENTORY_NAME_SPACE = 3

private const val PLAYER_INVENTORY_NAME_SPACE = 1

private const val HOTBAR_SPACE = 4

private const val INVENTORY_NAME_MARGIN_X = 1

class BagScreen(handler: BagScreenHandler, private val playerInventory: Inventory, title: Component) : ViewContainerScreen<BagScreenHandler>(handler, playerInventory, title) {

    override fun createRootView(): View {
        val card = menu.card ?: return SpaceView()

        /** 持ち物の名前なのだ～🌱 */
        fun inventoryNameView(name: Component) = TextView(name).also {
            it.sizingX = Sizing.FILL
            it.color = ColorPair.DARK_GRAY
            it.shadow = false
        }

        /** スロットを横に並べて、中央へ寄せた1行なのだ～🌱 */
        fun slotRowView(count: Int, slotIndexFunction: (Int) -> Int) = View {
            view += XListView().configure {
                view.sizingX = Sizing.FILL

                view += SpaceView().configure { position.weight = 1.0 }
                repeat(count) { c ->
                    view += menuSlotView(menu.slots[slotIndexFunction(c)], SlotType.NORMAL)
                }
                view += SpaceView().configure { position.weight = 1.0 }
            }
        }

        return View {
            view += YListView().configure {
                view.sizingX = Sizing.FILL
                view.sizingY = Sizing.FILL

                // 外枠なのだ～🌱 余った空間を全部取ってから、その中で中央へ寄せるのだ～🌱
                view += StackView().configure {
                    position.alignmentX = Alignment.CENTER
                    position.alignmentY = Alignment.CENTER
                    position.weight = 1.0

                    view += NinePatchImageView(NinePatchTextureCard.PANEL.texture, 5, 6, 5, 5, 6, 5)

                    // 内枠なのだ～🌱
                    view += YListView().configure {
                        view.sizingX = Sizing.FILL

                        // 枠の横幅はスロットの個数で決まるため、名前の長さを混ぜないのだ～🌱
                        view += inventoryNameView(title).configure { position.ignoreLayoutX = true }.margin(INVENTORY_NAME_MARGIN_X, 0)

                        view += YSpaceView(INVENTORY_NAME_SPACE)

                        // カバンの持ち物なのだ～🌱
                        repeat(card.inventoryHeight) { r ->
                            view += slotRowView(card.inventoryWidth) { c -> PLAYER_INVENTORY_WIDTH * (PLAYER_INVENTORY_HEIGHT + 1) + card.inventoryWidth * r + c }
                        }

                        view += YSpaceView(INVENTORY_NAME_SPACE)

                        view += inventoryNameView(playerInventory.name).configure { position.ignoreLayoutX = true }.margin(INVENTORY_NAME_MARGIN_X, 0)

                        view += YSpaceView(PLAYER_INVENTORY_NAME_SPACE)

                        // プレイヤーの持ち物なのだ～🌱
                        repeat(PLAYER_INVENTORY_HEIGHT) { r ->
                            view += slotRowView(PLAYER_INVENTORY_WIDTH) { c -> PLAYER_INVENTORY_WIDTH * r + c }
                        }

                        view += YSpaceView(HOTBAR_SPACE)

                        // ホットバーなのだ～🌱
                        view += slotRowView(PLAYER_INVENTORY_WIDTH) { c -> PLAYER_INVENTORY_WIDTH * PLAYER_INVENTORY_HEIGHT + c }
                    }.minContentSizeX(MENU_SLOT_BACKGROUND_SIZE * (card.inventoryWidth atLeast PLAYER_INVENTORY_WIDTH)).margin(PANEL_MARGIN)
                }
            }
        }
    }

}
