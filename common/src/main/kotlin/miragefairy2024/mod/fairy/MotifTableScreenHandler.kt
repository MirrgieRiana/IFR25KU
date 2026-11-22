package miragefairy2024.mod.fairy

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.clientProxy
import miragefairy2024.mod.recipeviewer.vanilla.ViewProviderMenu
import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.ColorPair
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.views.ItemView
import miragefairy2024.mod.recipeviewer.views.MultiLineTextView
import miragefairy2024.mod.recipeviewer.views.NinePatchImageView
import miragefairy2024.mod.recipeviewer.views.StackView
import miragefairy2024.mod.recipeviewer.views.View
import miragefairy2024.mod.recipeviewer.views.XListView
import miragefairy2024.mod.recipeviewer.views.XSpaceView
import miragefairy2024.mod.recipeviewer.views.YInventoryScrollbarView
import miragefairy2024.mod.recipeviewer.views.YListView
import miragefairy2024.mod.recipeviewer.views.YScrollContainerView
import miragefairy2024.mod.recipeviewer.views.configure
import miragefairy2024.mod.recipeviewer.views.connectScrollbar
import miragefairy2024.mod.recipeviewer.views.margin
import miragefairy2024.mod.recipeviewer.views.maxContentSizeX
import miragefairy2024.mod.recipeviewer.views.minContentSizeX
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
import miragefairy2024.util.times
import miragefairy2024.util.toIdentifier
import mirrg.kotlin.hydrogen.formatAs
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType
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
    ExtendedScreenHandlerType({ syncId, playerInventory, buf ->
        MotifTableScreenHandler(syncId, buf)
    }, MOTIF_TABLE_STREAM_CODEC)
}

context(_: ModContext)
fun initMotifTableScreenHandler() {
    motifTableScreenHandlerType.register()
    motifTableScreenHandlerType.registerViewScreen()
}

class MotifTableScreenHandler(syncId: Int, val chanceTable: List<CondensedMotifChance>) : AbstractContainerMenu(motifTableScreenHandlerType(), syncId), ViewProviderMenu {

    override fun createView(title: Component, playerInventoryTitle: Component): View {
        val panelTexture = "textures/gui/" * MirageFairy2024.identifier("panel") * ".png"
        val panelMargin = 7

        fun textView(text: Component, width: Int, alignment: Alignment) = View {
            view += MultiLineTextView().configure {
                view.text.value = text
                view.alignmentX = alignment
                view.color = ColorPair.DARK_GRAY
                view.shadow = false
            }.minContentSizeX(width).maxContentSizeX(width)
        }

        return View {
            view += StackView().configure { // 外枠なのだ～🌱
                view.sizingY = Sizing.FILL

                view += NinePatchImageView(panelTexture, 16, 16, 5, 5, 5, 5, 5, 5, false, false)
                view += XListView().configure {
                    view.sizingY = Sizing.FILL
                    val scrollView = YScrollContainerView().configure {
                        view.sizingX = Sizing.WRAP

                        view += YListView().configure {
                            chanceTable.forEach { chance ->
                                view += XListView().configure {
                                    view += ItemView(chance.showingItemStack).configure {
                                        position.alignmentY = Alignment.CENTER
                                        view.showDecorations = false
                                    }
                                    view += XSpaceView(3)
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
                        view.keyboardFocusable = false
                    }
                    view += scrollbarView
                    connectScrollbar(scrollView.view, scrollbarView.view)
                }.margin(panelMargin)
            }.margin(0, 4)
        }
    }

    override fun stillValid(player: Player) = true
    override fun quickMoveStack(player: Player, slot: Int) = EMPTY_ITEM_STACK
}
