package miragefairy2024.mod.magicplant

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.NinePatchTextureCard
import miragefairy2024.mod.common.guiFullScreenTranslation
import miragefairy2024.mod.magicplant.contents.getTraitPower
import miragefairy2024.mod.recipeviewer.vanilla.ScreenEventSource
import miragefairy2024.mod.recipeviewer.vanilla.ViewProviderMenu
import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.ColorPair
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewTexture
import miragefairy2024.mod.recipeviewer.views.AlignView
import miragefairy2024.mod.recipeviewer.views.DynamicView
import miragefairy2024.mod.recipeviewer.views.EmptyView
import miragefairy2024.mod.recipeviewer.views.ImageView
import miragefairy2024.mod.recipeviewer.views.MultiLineTextView
import miragefairy2024.mod.recipeviewer.views.StackView
import miragefairy2024.mod.recipeviewer.views.TextView
import miragefairy2024.mod.recipeviewer.views.TooltipBackgroundView
import miragefairy2024.mod.recipeviewer.views.View
import miragefairy2024.mod.recipeviewer.views.XListView
import miragefairy2024.mod.recipeviewer.views.YFlatScrollbarView
import miragefairy2024.mod.recipeviewer.views.YListView
import miragefairy2024.mod.recipeviewer.views.YScrollContainerView
import miragefairy2024.mod.recipeviewer.views.YSpaceView
import miragefairy2024.mod.recipeviewer.views.configure
import miragefairy2024.mod.recipeviewer.views.connectScrollbar
import miragefairy2024.mod.recipeviewer.views.createView
import miragefairy2024.mod.recipeviewer.views.fixedContentSizeX
import miragefairy2024.mod.recipeviewer.views.fixedContentSizeY
import miragefairy2024.mod.recipeviewer.views.margin
import miragefairy2024.mod.recipeviewer.views.onClick
import miragefairy2024.mod.recipeviewer.views.plusAssign
import miragefairy2024.mod.recipeviewer.views.tooltip
import miragefairy2024.mod.recipeviewer.views.wrapClickable
import miragefairy2024.util.Registration
import miragefairy2024.util.Translation
import miragefairy2024.util.darkGray
import miragefairy2024.util.enJa
import miragefairy2024.util.fire
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
import net.minecraft.world.level.Level

val traitListScreenHandlerType = Registration(BuiltInRegistries.MENU, MirageFairy2024.identifier("trait_list")) {
    ExtendedScreenHandlerType({ syncId, playerInventory, buf ->
        TraitListScreenHandler(syncId, playerInventory, ContainerLevelAccess.NULL, buf.first, buf.second)
    }, TraitListScreenHandler.STREAM_CODEC)
}

val traitListScreenTranslation = Translation({ "gui.${MirageFairy2024.identifier("trait_list").toLanguageKey()}" }, "Traits", "特性")

context(_: ModContext)
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

    override fun createRootView(title: Component, playerInventoryTitle: Component, screenEventSource: ScreenEventSource) = TraitListScreenContent(this, title).view

}

private class TraitListScreenContent(menu: TraitListScreenHandler, title: Component) {

    val view = run {
        val level: Level = menu.playerInventory.player.level()

        fun createTraitCardView(traitStack: TraitStack): View {
            return View {
                view += YListView().configure {
                    view.sizingX = Sizing.FILL
                    view.sizingY = Sizing.FILL

                    val trait = traitStack.trait
                    val context = TraitConditionContext(level, menu.blockPos, level.getMagicPlantBlockEntity(menu.blockPos))
                    val conditionFactors = trait.conditions.map { it.getFactor(context) }
                    val totalConditionFactor = conditionFactors.fold(1.0) { a, b -> a * b }

                    // 特性名
                    view += MultiLineTextView().configure {
                        view.text.value = trait.getName().style(trait.style)
                        view.alignmentX = Alignment.CENTER
                        view.shadow = false
                    }

                    view += YSpaceView(5)

                    // 特性アイコン欄
                    view += XListView().configure {
                        view.sizingX = Sizing.FILL
                        view.sizingY = Sizing.FILL

                        // 条件
                        view += YListView().configure {
                            position.alignmentY = Alignment.END
                            position.weight = 1.0
                            position.ignoreLayoutX = true
                            view.sizingX = Sizing.FILL

                            trait.conditions.forEachIndexed { index, condition ->
                                view += TextView(text { condition.emoji + " "() + (conditionFactors[index] * 100.0 formatAs "%.1f%%")() }).configure {
                                    view.shadow = false
                                }.tooltip(condition.name)
                            }
                        }

                        // 特性アイコン
                        view += ImageView(ViewTexture(trait.texture, IntPoint(32, 32)))

                        // 効果
                        view += YListView().configure {
                            position.alignmentY = Alignment.END
                            position.weight = 1.0
                            position.ignoreLayoutX = true
                            view.sizingX = Sizing.FILL

                            trait.traitEffectKeyEntries.forEach { entry ->
                                fun <T : Any> createEffectText(key: TraitEffectKey<T>): Component {
                                    val value = key.getValue(entry.factor * getTraitPower(traitStack.level) * totalConditionFactor)
                                    return text { key.renderValue(value) + " "() + key.emoji.style(key.style) }
                                }

                                view += TextView(createEffectText(entry.traitEffectKey)).configure {
                                    position.alignmentX = Alignment.END
                                    view.shadow = false
                                }.tooltip(entry.traitEffectKey.name)
                            }
                        }

                    }.fixedContentSizeY(32)

                    view += YSpaceView(5)

                    // 特性ポエム
                    // バーを本文に重ね、従来の折り返し幅を維持するのだ～🌱
                    view += StackView().configure {
                        position.weight = 1.0
                        view.sizingX = Sizing.FILL
                        view.sizingY = Sizing.FILL

                        val scrollView = YScrollContainerView().configure {
                            view += MultiLineTextView().configure {
                                view.text.value = trait.text
                                view.shadow = false
                            }
                        }
                        view += scrollView
                        val scrollbarView = YFlatScrollbarView().configure {
                            view.scrollbarColor = 0xA0FFFFFF.toInt()
                            view.keyboardFocusable = false
                        }
                        view += AlignView().configure {
                            view.alignmentX = Alignment.END
                            view += scrollbarView
                        }
                        connectScrollbar(scrollView.view, scrollbarView.view)
                    }.wrapClickable {
                        view.hoverOverlayColor = 0x00000000
                        view.onClick.register {
                            onOpenTraitEncyclopediaPageScreen.fire {
                                if (it(trait)) return@register true
                            }
                            false
                        }
                    }.tooltip(text { guiFullScreenTranslation() })

                }
            }
        }

        View {
            // 外枠
            view += StackView().configure {
                view += NinePatchTextureCard.PANEL.createView()
                // 内枠
                view += YListView().configure {
                    view.sizingX = Sizing.FILL

                    val traitStackList = menu.traitStacks.traitStackList

                    val cardContentDynamicView = DynamicView(if (traitStackList.isNotEmpty()) createTraitCardView(traitStackList.first()) else EmptyView())

                    // GUI名
                    view += MultiLineTextView().configure {
                        view.text.value = title
                        view.alignmentX = Alignment.CENTER
                        view.color = ColorPair.DARK_GRAY
                        view.shadow = false
                    }.margin(1, 0)

                    view += YSpaceView(3)

                    // 選択中の特性
                    view += StackView().configure {
                        view.sizingX = Sizing.FILL
                        view.sizingY = Sizing.FILL

                        view += NinePatchTextureCard.TRAIT_BACKGROUND.createView()
                        view += cardContentDynamicView.configure().margin(5)
                    }.fixedContentSizeY(18 * 6)

                    view += YSpaceView(4)

                    // 特性リスト
                    view += StackView().configure {
                        view.sizingX = Sizing.FILL
                        view.sizingY = Sizing.FILL

                        view += TooltipBackgroundView()
                        view += XListView().configure {
                            view.sizingX = Sizing.FILL
                            view.sizingY = Sizing.FILL

                            val scrollView = YScrollContainerView().configure {
                                position.weight = 1.0

                                view += YListView().configure {
                                    view.sizingX = Sizing.FILL

                                    val context = TraitConditionContext(level, menu.blockPos, level.getMagicPlantBlockEntity(menu.blockPos))

                                    traitStackList.forEach { traitStack ->
                                        val trait = traitStack.trait
                                        val conditionFactors = trait.conditions.map { it.getFactor(context) }
                                        val totalConditionFactor = conditionFactors.fold(1.0) { a, b -> a * b }

                                        // 特性
                                        view += MultiLineTextView().configure {
                                            view.text.value = text {
                                                val texts = mutableListOf<Component>()
                                                val styleFunction: (Component) -> Component = { if (totalConditionFactor == 0.0) it.darkGray else it.style(trait.style) }
                                                texts += styleFunction(trait.getName())
                                                texts += styleFunction(traitStack.level.toString(2)())
                                                if (trait.conditions.isNotEmpty()) texts += trait.conditions.map { it.emoji }.join() + " →"()
                                                if (trait.traitEffectKeyEntries.isNotEmpty()) texts += trait.traitEffectKeyEntries.map { it.traitEffectKey.emoji.style(it.traitEffectKey.style) }.join()
                                                texts.join(" "())
                                            }
                                            view.shadow = false
                                        }.onClick {
                                            cardContentDynamicView.content.value = createTraitCardView(traitStack)
                                            true
                                        }

                                    }
                                }
                            }
                            view += scrollView
                            val scrollbarView = YFlatScrollbarView().configure {
                                view.scrollbarColor = 0xA0FFFFFF.toInt()
                                view.keyboardFocusable = false
                            }
                            view += scrollbarView
                            connectScrollbar(scrollView.view, scrollbarView.view)
                        }.margin(4)
                    }.fixedContentSizeY(18 * 4)

                }.fixedContentSizeX(18 * 9).margin(7)
            }
        }
    }

}
