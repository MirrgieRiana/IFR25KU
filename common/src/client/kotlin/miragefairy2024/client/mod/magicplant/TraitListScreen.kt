package miragefairy2024.client.mod.magicplant

import miragefairy2024.client.mod.recipeviewer.screen.MENU_SLOT_BACKGROUND_SIZE
import miragefairy2024.client.mod.recipeviewer.screen.ViewContainerScreen
import miragefairy2024.mod.NinePatchTextureCard
import miragefairy2024.mod.magicplant.TraitConditionContext
import miragefairy2024.mod.magicplant.TraitEffectKey
import miragefairy2024.mod.magicplant.TraitListScreenHandler
import miragefairy2024.mod.magicplant.TraitStack
import miragefairy2024.mod.magicplant.contents.getTraitPower
import miragefairy2024.mod.magicplant.getMagicPlantBlockEntity
import miragefairy2024.mod.magicplant.getName
import miragefairy2024.mod.magicplant.style
import miragefairy2024.mod.magicplant.texture
import miragefairy2024.mod.magicplant.traitListScreenTranslation
import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.ColorPair
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewTexture
import miragefairy2024.mod.recipeviewer.views.ImageView
import miragefairy2024.mod.recipeviewer.views.NinePatchImageView
import miragefairy2024.mod.recipeviewer.views.ScrollView
import miragefairy2024.mod.recipeviewer.views.ScrollbarStyle
import miragefairy2024.mod.recipeviewer.views.StackView
import miragefairy2024.mod.recipeviewer.views.TextView
import miragefairy2024.mod.recipeviewer.views.TooltipBackgroundView
import miragefairy2024.mod.recipeviewer.views.View
import miragefairy2024.mod.recipeviewer.views.WrappedTextView
import miragefairy2024.mod.recipeviewer.views.XListView
import miragefairy2024.mod.recipeviewer.views.YListView
import miragefairy2024.mod.recipeviewer.views.YSpaceView
import miragefairy2024.mod.recipeviewer.views.configure
import miragefairy2024.mod.recipeviewer.views.margin
import miragefairy2024.mod.recipeviewer.views.maxContentSizeY
import miragefairy2024.mod.recipeviewer.views.minContentSizeX
import miragefairy2024.mod.recipeviewer.views.minContentSizeY
import miragefairy2024.mod.recipeviewer.views.onClick
import miragefairy2024.mod.recipeviewer.views.plusAssign
import miragefairy2024.mod.recipeviewer.views.tooltip
import miragefairy2024.util.darkGray
import miragefairy2024.util.invoke
import miragefairy2024.util.join
import miragefairy2024.util.plus
import miragefairy2024.util.style
import miragefairy2024.util.text
import mirrg.kotlin.hydrogen.formatAs
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory

private const val PANEL_MARGIN = 7

/** 内枠の横幅は、持ち物の横の個数に揃えてあるのだ～🌱 */
private const val INNER_WIDTH = MENU_SLOT_BACKGROUND_SIZE * 9

private const val CARD_SIZE_Y = MENU_SLOT_BACKGROUND_SIZE * 6

private const val TRAIT_LIST_SIZE_Y = MENU_SLOT_BACKGROUND_SIZE * 4

private const val CARD_MARGIN = 5

/** ツールチップの背景の内側の余白なのだ～🌱 */
private const val TOOLTIP_MARGIN = 4

private const val INVENTORY_NAME_SPACE = 3

private const val TRAIT_LIST_SPACE = 4

private const val SCROLLBAR_THICKNESS = 5

private const val SCROLLBAR_ARGB = 0xA0FFFFFF.toInt()

private const val TRAIT_ICON_SIZE = 32

class TraitListScreen(handler: TraitListScreenHandler, playerInventory: Inventory, title: Component) : ViewContainerScreen<TraitListScreenHandler>(handler, playerInventory, title) {

    /** カードへ出す特性なのだ～🌱 選び直すとウィジェットを作り直すのだ～🌱 */
    private var selectedTraitStack: TraitStack? = menu.traitStacks.traitStackList.firstOrNull()

    override fun createRootView(): View {
        val player = Minecraft.getInstance().player!!
        val level = player.level()
        val blockEntity = level.getMagicPlantBlockEntity(menu.blockPos)
        val context = TraitConditionContext(level, menu.blockPos, blockEntity)

        /** 全部の条件を掛け合わせた割合なのだ～🌱 0 になると、その特性は働かないのだ～🌱 */
        fun getTotalConditionFactor(traitStack: TraitStack) = traitStack.trait.conditions
            .map { it.getFactor(context) }
            .fold(1.0) { a, b -> a * b }

        /** カードの中身なのだ～🌱 */
        fun traitCardContentView(traitStack: TraitStack): View {
            val totalConditionFactor = getTotalConditionFactor(traitStack)

            return View {
                view += YListView().configure {
                    view.sizingX = Sizing.FILL
                    view.sizingY = Sizing.FILL

                    // 特性名なのだ～🌱 カードの横幅は外から決まるため、文字の長さを混ぜないのだ～🌱
                    view += TextView(text { traitStack.trait.getName().style(traitStack.trait.style) }).also {
                        it.sizingX = Sizing.FILL
                        it.alignmentX = Alignment.CENTER
                        it.shadow = false
                    }.configure { position.ignoreLayoutX = true }

                    view += YSpaceView(CARD_MARGIN)

                    // 条件とアイコンと効果なのだ～🌱
                    view += XListView().configure {
                        position.ignoreLayoutX = true
                        view.sizingX = Sizing.FILL

                        // 条件なのだ～🌱
                        view += YListView().configure {
                            position.weight = 1.0
                            position.alignmentY = Alignment.END
                            // 重みで割り当てた横幅を使うのだ～🌱
                            view.sizingX = Sizing.FILL

                            traitStack.trait.conditions.forEach { condition ->
                                val factor = condition.getFactor(context)
                                view += TextView(text { condition.emoji + " "() + (factor * 100.0 formatAs "%.1f%%")() }).also {
                                    it.shadow = false
                                }.configure { }.tooltip(condition.name)
                            }
                        }

                        // 特性アイコンなのだ～🌱
                        view += ImageView(ViewTexture(traitStack.trait.texture, IntPoint(TRAIT_ICON_SIZE, TRAIT_ICON_SIZE), IntRectangle(0, 0, TRAIT_ICON_SIZE, TRAIT_ICON_SIZE)))

                        // 効果なのだ～🌱
                        view += YListView().configure {
                            position.weight = 1.0
                            position.alignmentY = Alignment.END
                            view.sizingX = Sizing.FILL

                            traitStack.trait.traitEffectKeyEntries.forEach { entry ->
                                fun <T : Any> traitEffectView(traitEffectKey: TraitEffectKey<T>, traitEffectFactor: Double) {
                                    val value = traitEffectKey.getValue(traitEffectFactor * getTraitPower(traitStack.level) * totalConditionFactor)
                                    view += TextView(text { traitEffectKey.renderValue(value) + " "() + traitEffectKey.emoji.style(traitEffectKey.style) }).also {
                                        it.sizingX = Sizing.FILL
                                        it.alignmentX = Alignment.END
                                        it.shadow = false
                                    }.configure { }.tooltip(traitEffectKey.name)
                                }
                                traitEffectView(entry.traitEffectKey, entry.factor)
                            }
                        }
                    }

                    view += YSpaceView(CARD_MARGIN)

                    // 特性ポエムなのだ～🌱 スクロールバーは中身へ重ねるのだ～🌱
                    view += ScrollView(SCROLLBAR_THICKNESS, ScrollbarStyle.Flat(SCROLLBAR_ARGB)).configure {
                        position.weight = 1.0

                        view += WrappedTextView(text { traitStack.trait.text }).also {
                            it.shadow = false
                        }
                    }
                }
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

                        // 枠の横幅はスロットの個数で決まるため、文字の長さを混ぜないのだ～🌱
                        view += TextView(text { traitListScreenTranslation() }).also {
                            it.sizingX = Sizing.FILL
                            it.alignmentX = Alignment.CENTER
                            it.color = ColorPair.DARK_GRAY
                            it.shadow = false
                        }.configure { position.ignoreLayoutX = true }

                        view += YSpaceView(INVENTORY_NAME_SPACE)

                        // 特性カードなのだ～🌱
                        view += StackView().configure {
                            position.ignoreLayoutX = true
                            view.sizingX = Sizing.FILL

                            view += NinePatchImageView(NinePatchTextureCard.TRAIT_BACKGROUND.texture, 22, 22, 22, 22, 22, 22)

                            val traitStack = selectedTraitStack
                            if (traitStack != null) view += traitCardContentView(traitStack).configure { }.margin(CARD_MARGIN)
                        }.minContentSizeY(CARD_SIZE_Y).maxContentSizeY(CARD_SIZE_Y)

                        view += YSpaceView(TRAIT_LIST_SPACE)

                        // 特性の一覧なのだ～🌱
                        view += StackView().configure {
                            position.ignoreLayoutX = true
                            view.sizingX = Sizing.FILL

                            view += TooltipBackgroundView()

                            view += ScrollView(SCROLLBAR_THICKNESS, ScrollbarStyle.Flat(SCROLLBAR_ARGB)).configure {
                                view += YListView().configure {
                                    view.sizingX = Sizing.FILL

                                    menu.traitStacks.traitStackList.forEach { traitStack ->
                                        val totalConditionFactor = getTotalConditionFactor(traitStack)
                                        val styleFunction: (Component) -> Component = { if (totalConditionFactor == 0.0) it.darkGray else it.style(traitStack.trait.style) }

                                        view += TextView(text {
                                            val texts = mutableListOf<Component>()
                                            texts += styleFunction(traitStack.trait.getName())
                                            texts += styleFunction(traitStack.level.toString(2)())
                                            if (traitStack.trait.conditions.isNotEmpty()) texts += traitStack.trait.conditions.map { it.emoji }.join() + " →"()
                                            if (traitStack.trait.traitEffectKeyEntries.isNotEmpty()) texts += traitStack.trait.traitEffectKeyEntries.map { it.traitEffectKey.emoji.style(it.traitEffectKey.style) }.join()
                                            texts.join(" "())
                                        }).also {
                                            it.sizingX = Sizing.FILL
                                            it.shadow = false
                                        }.configure { position.ignoreLayoutX = true }.onClick {
                                            selectedTraitStack = traitStack
                                            rebuildWidgets()
                                            true
                                        }
                                    }
                                }.margin(0, SCROLLBAR_THICKNESS, 0, 0)
                            }.margin(TOOLTIP_MARGIN)
                        }.minContentSizeY(TRAIT_LIST_SIZE_Y).maxContentSizeY(TRAIT_LIST_SIZE_Y)
                    }.minContentSizeX(INNER_WIDTH).margin(PANEL_MARGIN)
                }
            }
        }
    }

}
