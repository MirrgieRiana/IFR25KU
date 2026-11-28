package miragefairy2024.mod.fairy

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.clientProxy
import miragefairy2024.mod.NinePatchTextureCard
import miragefairy2024.mod.recipeviewer.vanilla.ScreenEventSource
import miragefairy2024.mod.recipeviewer.vanilla.ViewProviderMenu
import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.ColorPair
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.views.ItemView
import miragefairy2024.mod.recipeviewer.views.MultiLineTextView
import miragefairy2024.mod.recipeviewer.views.StackView
import miragefairy2024.mod.recipeviewer.views.View
import miragefairy2024.mod.recipeviewer.views.XListView
import miragefairy2024.mod.recipeviewer.views.XSpaceView
import miragefairy2024.mod.recipeviewer.views.YInventoryScrollbarView
import miragefairy2024.mod.recipeviewer.views.YListView
import miragefairy2024.mod.recipeviewer.views.YScrollContainerView
import miragefairy2024.mod.recipeviewer.views.configure
import miragefairy2024.mod.recipeviewer.views.connectScrollbar
import miragefairy2024.mod.recipeviewer.views.createView
import miragefairy2024.mod.recipeviewer.views.fixedContentSizeX
import miragefairy2024.mod.recipeviewer.views.margin
import miragefairy2024.mod.recipeviewer.views.plusAssign
import miragefairy2024.mod.recipeviewer.views.tooltip
import miragefairy2024.util.Chance
import miragefairy2024.util.CondensedItem
import miragefairy2024.util.EMPTY_ITEM_STACK
import miragefairy2024.util.Registration
import miragefairy2024.util.invoke
import miragefairy2024.util.plus
import miragefairy2024.util.register
import miragefairy2024.util.registerViewScreen
import miragefairy2024.util.string
import miragefairy2024.util.text
import miragefairy2024.util.toIdentifier
import mirrg.kotlin.hydrogen.formatAs
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType as ExtendedMenuType // MIGRATION2612 ReplaceImport net.fabricmc.fabric.api.menu.v1.ExtendedMenuType
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.ItemStack

val MOTIF_TABLE_STREAM_CODEC = object : StreamCodec<RegistryFriendlyByteBuf, List<CondensedMotifChance>> {
    override fun encode(`object`: RegistryFriendlyByteBuf, object2: List<CondensedMotifChance>) {
        `object`.writeInt(object2.size)
        object2.forEach {
            ItemStack.STREAM_CODEC.encode(`object`, it.showingItemStack)
            `object`.writeUtf(it.item.item.item.getIdentifier()!!.string)
            `object`.writeDouble(it.item.weight)
            `object`.writeDouble(it.item.item.count)
        }
    }

    override fun decode(`object`: RegistryFriendlyByteBuf): List<CondensedMotifChance> {
        val length = `object`.readInt()
        val chanceTable = mutableListOf<CondensedMotifChance>()
        repeat(length) {
            val showingItemStack = ItemStack.STREAM_CODEC.decode(`object`)
            val motifId = `object`.readUtf()
            val rate = `object`.readDouble()
            val count = `object`.readDouble()
            chanceTable += CondensedMotifChance(showingItemStack, Chance(rate, CondensedItem(count, motifRegistry.get(motifId.toIdentifier())!!)))
        }
        return chanceTable
    }
}

val motifTableScreenHandlerType = Registration(BuiltInRegistries.MENU, MirageFairy2024.identifier("motif_table")) {
    ExtendedMenuType({ syncId, playerInventory, buf ->
        MotifTableScreenHandler(syncId, buf)
    }, MOTIF_TABLE_STREAM_CODEC)
}

context(_: ModContext)
fun initMotifTableScreenHandler() {
    motifTableScreenHandlerType.register()
    motifTableScreenHandlerType.registerViewScreen()
}

class MotifTableScreenHandler(syncId: Int, val chanceTable: List<CondensedMotifChance>) : AbstractContainerMenu(motifTableScreenHandlerType(), syncId), ViewProviderMenu<MotifTableScreenContent.State> {

    override fun createScreenState() = MotifTableScreenContent.State()

    override fun createRootView(title: Component, playerInventoryTitle: Component, screenEventSource: ScreenEventSource, state: MotifTableScreenContent.State) = MotifTableScreenContent(chanceTable, state).view

    override fun stillValid(player: Player) = true
    override fun quickMoveStack(player: Player, slot: Int) = EMPTY_ITEM_STACK
}

class MotifTableScreenContent(chanceTable: List<CondensedMotifChance>, state: State) {

    class State {
        var scrollOffset = 0.0
    }

    val view = View {
        // 外枠
        view += StackView().configure {
            view.sizingY = Sizing.FILL

            view += NinePatchTextureCard.PANEL.createView()
            // メインコンテナ
            view += XListView().configure {
                view.sizingY = Sizing.FILL
                // スクロールパネル
                val scrollView = YScrollContainerView().configure {
                    view.scrollOffsetY.value = state.scrollOffset
                    view.scrollOffsetY.register { _, offset ->
                        state.scrollOffset = offset
                    }
                    view.sizingX = Sizing.WRAP

                    // スロットパネル
                    view += YListView().configure {
                        chanceTable.forEach { chance ->
                            view += XListView().configure {
                                view += ItemView(chance.showingItemStack).configure {
                                    position.alignmentY = Alignment.CENTER
                                    view.showDecorations = false
                                }
                                view += XSpaceView(3)

                                fun textView(text: Component, width: Int, alignment: Alignment) = View {
                                    view += MultiLineTextView().configure {
                                        view.text.value = text
                                        view.alignmentX = alignment
                                        view.color = ColorPair.DARK_GRAY
                                        view.shadow = false
                                    }.fixedContentSizeX(width)
                                }

                                view += textView(chance.showingItemStack.hoverName, 150, Alignment.START).configure {
                                    position.alignmentY = Alignment.CENTER
                                }
                                view += textView(text { (chance.item.weight * 100 formatAs "%.4f%%")() }, 50, Alignment.END).configure {
                                    position.alignmentY = Alignment.CENTER
                                }
                                view += textView(text { "x"() + (chance.item.item.count formatAs "%.2f")() }, 80, Alignment.END).configure {
                                    position.alignmentY = Alignment.CENTER
                                }
                            }.tooltip { _, _ -> clientProxy!!.getItemTooltip(chance.showingItemStack) }
                        }
                    }.margin(0, 3, 0, 0)

                }
                view += scrollView
                val scrollbarView = YInventoryScrollbarView().configure {
                    view.isAttached = true
                }
                view += scrollbarView
                connectScrollbar(scrollView.view, scrollbarView.view)
            }.margin(7)
        }.margin(0, 4)
    }

}
