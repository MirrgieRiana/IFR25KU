package miragefairy2024.client.mod.fairy

import miragefairy2024.ModContext
import miragefairy2024.client.mod.recipeviewer.vanilla.AbstractViewContainerScreen
import miragefairy2024.client.util.registerHandledScreen
import miragefairy2024.mod.NinePatchTextureCard
import miragefairy2024.mod.fairy.MotifTableScreenHandler
import miragefairy2024.mod.fairy.motifTableScreenHandlerType
import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.ColorPair
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.views.NinePatchImageView
import miragefairy2024.mod.recipeviewer.views.OutputSlotView
import miragefairy2024.mod.recipeviewer.views.ScrollView
import miragefairy2024.mod.recipeviewer.views.ScrollbarStyle
import miragefairy2024.mod.recipeviewer.views.StackView
import miragefairy2024.mod.recipeviewer.views.TextView
import miragefairy2024.mod.recipeviewer.views.View
import miragefairy2024.mod.recipeviewer.views.XListView
import miragefairy2024.mod.recipeviewer.views.XSpaceView
import miragefairy2024.mod.recipeviewer.views.YListView
import miragefairy2024.mod.recipeviewer.views.configure
import miragefairy2024.mod.recipeviewer.views.margin
import miragefairy2024.mod.recipeviewer.views.maxContentSizeX
import miragefairy2024.mod.recipeviewer.views.minContentSizeX
import miragefairy2024.mod.recipeviewer.views.noBackground
import miragefairy2024.mod.recipeviewer.views.noMargin
import miragefairy2024.mod.recipeviewer.views.plusAssign
import miragefairy2024.mod.recipeviewer.views.tooltip
import miragefairy2024.util.invoke
import miragefairy2024.util.plus
import miragefairy2024.util.text
import mirrg.kotlin.hydrogen.formatAs
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag

context(ModContext)
fun initMotifTableClientModule() {

    // GUI登録
    motifTableScreenHandlerType.registerHandledScreen { gui, inventory, title -> MotifTableScreen(gui, inventory, title) }

}

/** 設定に合わせた詳細さで、アイテムのツールチップを取るのだ～🌱 */
private val ItemStack.itemTooltipLines: List<Component>
    get() {
        val tooltipFlag = if (Minecraft.getInstance().options.advancedItemTooltips) TooltipFlag.ADVANCED else TooltipFlag.NORMAL
        return this.getTooltipLines(Item.TooltipContext.of(Minecraft.getInstance().level), Minecraft.getInstance().player, tooltipFlag)
    }

class MotifTableScreen(handler: MotifTableScreenHandler, playerInventory: Inventory, title: Component) : AbstractViewContainerScreen<MotifTableScreenHandler>(handler, playerInventory, title) {

    override fun createRootView(): View {
        /** 幅を固定して、その中で文字を寄せるのだ～🌱 */
        fun fixedTextView(text: Component, sizeX: Int, alignmentX: Alignment): View = View {
            view += TextView(text).configure {
                view.sizingX = Sizing.FILL
                view.alignmentX = alignmentX
                view.color = ColorPair.DARK_GRAY
                view.shadow = false
            }.minContentSizeX(sizeX).maxContentSizeX(sizeX)
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
                    view.sizingY = Sizing.FILL

                    view += NinePatchImageView(NinePatchTextureCard.PANEL.texture, 16, 16, 5, 6, 5, 5, 6, 5, true)

                    // 確率の一覧なのだ～🌱
                    view += ScrollView(18, ScrollbarStyle.Vanilla).configure {
                        view += YListView().configure {
                            view.sizingX = Sizing.FILL

                            menu.chanceTable.forEach { chance ->
                                view += XListView().configure {
                                    view.sizingX = Sizing.FILL

                                    view += OutputSlotView(chance.showingItemStack).noBackground().noMargin().configure {
                                        position.alignmentY = Alignment.CENTER
                                    }

                                    view += XSpaceView(3)

                                    view += fixedTextView(chance.showingItemStack.hoverName, 150, Alignment.START).configure { position.alignmentY = Alignment.CENTER }
                                    view += fixedTextView(text { (chance.item.weight * 100 formatAs "%.4f%%")() }, 50, Alignment.END).configure { position.alignmentY = Alignment.CENTER }
                                    view += fixedTextView(text { "x"() + (chance.item.item.count formatAs "%.2f")() }, 80, Alignment.END).configure { position.alignmentY = Alignment.CENTER }
                                }.tooltip { _, _ -> chance.showingItemStack.itemTooltipLines }
                            }
                        }.margin(0, 3, 0, 0)
                    }.margin(7)
                }.margin(4)
            }
        }
    }

}
