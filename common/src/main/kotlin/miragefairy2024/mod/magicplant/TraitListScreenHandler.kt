package miragefairy2024.mod.magicplant

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.NinePatchTextureCard
import miragefairy2024.mod.magicplant.contents.getTraitPower
import miragefairy2024.mod.recipeviewer.vanilla.ViewProviderMenu
import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.ColorPair
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewTexture
import miragefairy2024.mod.recipeviewer.views.DynamicView
import miragefairy2024.mod.recipeviewer.views.ImageView
import miragefairy2024.mod.recipeviewer.views.MultiLineTextView
import miragefairy2024.mod.recipeviewer.views.NinePatchImageView
import miragefairy2024.mod.recipeviewer.views.SpaceView
import miragefairy2024.mod.recipeviewer.views.StackView
import miragefairy2024.mod.recipeviewer.views.TextView
import miragefairy2024.mod.recipeviewer.views.TooltipBackgroundView
import miragefairy2024.mod.recipeviewer.views.View
import miragefairy2024.mod.recipeviewer.views.XListView
import miragefairy2024.mod.recipeviewer.views.YListView
import miragefairy2024.mod.recipeviewer.views.YScrollView
import miragefairy2024.mod.recipeviewer.views.YSpaceView
import miragefairy2024.mod.recipeviewer.views.configure
import miragefairy2024.mod.recipeviewer.views.margin
import miragefairy2024.mod.recipeviewer.views.maxContentSizeX
import miragefairy2024.mod.recipeviewer.views.maxContentSizeY
import miragefairy2024.mod.recipeviewer.views.minContentSizeX
import miragefairy2024.mod.recipeviewer.views.minContentSizeY
import miragefairy2024.mod.recipeviewer.views.onClick
import miragefairy2024.mod.recipeviewer.views.plusAssign
import miragefairy2024.mod.recipeviewer.views.tooltip
import miragefairy2024.util.ObservableValue
import miragefairy2024.util.Registration
import miragefairy2024.util.Translation
import miragefairy2024.util.darkGray
import miragefairy2024.util.enJa
import miragefairy2024.util.invoke
import miragefairy2024.util.join
import miragefairy2024.util.plus
import miragefairy2024.util.register
import miragefairy2024.util.registerViewScreen
import miragefairy2024.util.style
import miragefairy2024.util.text
import mirrg.kotlin.hydrogen.formatAs
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ContainerLevelAccess
import net.minecraft.world.item.ItemStack

val traitListScreenHandlerType = Registration(BuiltInRegistries.MENU, MirageFairy2024.identifier("trait_list")) {
    ExtendedScreenHandlerType({ syncId, playerInventory, buf ->
        TraitListScreenHandler(syncId, playerInventory, ContainerLevelAccess.NULL, buf.first, buf.second)
    }, TraitListScreenHandler.STREAM_CODEC)
}

val traitListScreenTranslation = Translation({ "gui.${MirageFairy2024.identifier("trait_list").toLanguageKey()}" }, "Traits", "特性")

context(ModContext)
fun initTraitListScreenHandler() {
    traitListScreenHandlerType.register()
    traitListScreenHandlerType.registerViewScreen()
    traitListScreenTranslation.enJa()
}

class TraitListScreenHandler(syncId: Int, val playerInventory: Inventory, val context: ContainerLevelAccess, val traitStacks: TraitStacks, val blockPos: BlockPos) : AbstractContainerMenu(traitListScreenHandlerType(), syncId), ViewProviderMenu {
    companion object {
        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, Pair<TraitStacks, BlockPos>> = StreamCodec.composite(
            TraitStacks.STREAM_CODEC,
            { it.first },
            BlockPos.STREAM_CODEC,
            { it.second },
            ::Pair,
        )
    }

    override fun stillValid(player: Player) = true
    override fun quickMoveStack(player: Player, slot: Int): ItemStack = slots[slot].item

    override fun createView(title: Component, playerInventoryTitle: Component): View {
        val level = playerInventory.player.level()
        val blockEntity = level.getMagicPlantBlockEntity(blockPos)
        val context = TraitConditionContext(level, blockPos, blockEntity)

        fun traitCardView(traitStack: TraitStack) = View {
            view += YListView().configure {
                view.sizingX = Sizing.FILL
                view.sizingY = Sizing.FILL

                view += MultiLineTextView().configure {
                    view.text.value = text { traitStack.trait.getName().style(traitStack.trait.style) }
                    view.alignmentX = Alignment.CENTER
                    view.shadow = false
                }
                view += YSpaceView(5)
                view += XListView().configure {
                    view.sizingX = Sizing.FILL

                    view += YListView().configure {
                        view.sizingX = Sizing.FILL
                        view.sizingY = Sizing.FILL
                        position.weight = 1.0
                        position.ignoreLayoutX = true

                        view += SpaceView().configure { position.weight = 1.0 }
                        traitStack.trait.conditions.forEach { condition ->
                            val factor = condition.getFactor(context)
                            view += TextView(text { condition.emoji + " "() + (factor * 100.0 formatAs "%.1f%%")() }).configure {
                                view.shadow = false
                            }.tooltip(condition.name)
                        }
                    }
                    view += ImageView(ViewTexture(traitStack.trait.texture, IntPoint(32, 32)))
                    view += YListView().configure {
                        view.sizingX = Sizing.FILL
                        view.sizingY = Sizing.FILL
                        position.weight = 1.0
                        position.ignoreLayoutX = true

                        view += SpaceView().configure { position.weight = 1.0 }
                        val totalConditionFactor = traitStack.trait.conditions.map { it.getFactor(context) }.fold(1.0) { a, b -> a * b }
                        traitStack.trait.traitEffectKeyEntries.forEach { entry ->
                            fun <T : Any> effectView(traitEffectKey: TraitEffectKey<T>, traitEffectFactor: Double) = View {
                                val value = traitEffectKey.getValue(traitEffectFactor * getTraitPower(traitStack.level) * totalConditionFactor)
                                view += TextView(text { traitEffectKey.renderValue(value) + " "() + traitEffectKey.emoji.style(traitEffectKey.style) }).configure {
                                    view.shadow = false
                                }.tooltip(traitEffectKey.name)
                            }
                            view += effectView(entry.traitEffectKey, entry.factor).configure {
                                position.alignmentX = Alignment.END
                            }
                        }
                    }
                }.minContentSizeY(32).maxContentSizeY(32)
                view += YSpaceView(5)
                view += YScrollView().configure {
                    position.weight = 1.0
                    view.scrollbarColor = 0xA0FFFFFF.toInt()
                    view.reserveScrollbarSpace = false

                    view += MultiLineTextView().configure {
                        view.text.value = text { traitStack.trait.text }
                        view.shadow = false
                    }
                }
            }
        }

        val traitStack = traitStacks.traitStackList.firstOrNull()
        val cardContent = ObservableValue<View>(if (traitStack == null) SpaceView() else traitCardView(traitStack))

        return View {
            view += StackView().configure {
                view += NinePatchImageView(MirageFairy2024.identifier("textures/gui/panel.png"), 16, 16, 5, 5, 5, 5, 5, 5, false)
                view += YListView().configure {
                    view.sizingX = Sizing.FILL

                    view += MultiLineTextView().configure {
                        view.text.value = text { traitListScreenTranslation() }
                        view.alignmentX = Alignment.CENTER
                        view.color = ColorPair.DARK_GRAY
                        view.shadow = false
                    }.margin(1, 0)
                    view += YSpaceView(3)
                    view += StackView().configure {
                        view.sizingX = Sizing.FILL

                        view += NinePatchImageView(NinePatchTextureCard.TRAIT_BACKGROUND.texture, 66, 66, 22, 22, 22, 22, 22, 22, true)
                        view += DynamicView(cardContent).configure {}.margin(5)
                    }.minContentSizeY(108).maxContentSizeY(108)
                    view += YSpaceView(4)
                    view += StackView().configure {
                        view.sizingX = Sizing.FILL

                        view += TooltipBackgroundView()
                        view += YScrollView().configure {
                            view.scrollbarColor = 0xA0FFFFFF.toInt()

                            view += YListView().configure {
                                view.sizingX = Sizing.FILL

                                traitStacks.traitStackList.forEach { traitStack ->
                                    val totalConditionFactor = traitStack.trait.conditions.map { it.getFactor(context) }.fold(1.0) { a, b -> a * b }
                                    view += MultiLineTextView().configure {
                                        view.text.value = text {
                                            val texts = mutableListOf<Component>()
                                            val styleFunction: (Component) -> Component = { if (totalConditionFactor == 0.0) it.darkGray else it.style(traitStack.trait.style) }
                                            texts += styleFunction(traitStack.trait.getName())
                                            texts += styleFunction(traitStack.level.toString(2)())
                                            if (traitStack.trait.conditions.isNotEmpty()) texts += traitStack.trait.conditions.map { it.emoji }.join() + " →"()
                                            if (traitStack.trait.traitEffectKeyEntries.isNotEmpty()) texts += traitStack.trait.traitEffectKeyEntries.map { it.traitEffectKey.emoji.style(it.traitEffectKey.style) }.join()
                                            texts.join(" "())
                                        }
                                        view.shadow = false
                                    }.onClick {
                                        cardContent.value = traitCardView(traitStack)
                                        true
                                    }
                                }
                            }
                        }.margin(4)
                    }.minContentSizeY(72).maxContentSizeY(72)
                }.minContentSizeX(162).maxContentSizeX(162).margin(7)
            }
        }
    }
}
