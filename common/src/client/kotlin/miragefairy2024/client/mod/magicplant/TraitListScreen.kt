package miragefairy2024.client.mod.magicplant

import miragefairy2024.client.mod.recipeviewer.vanilla.AbstractViewContainerScreen
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
import miragefairy2024.mod.recipeviewer.views.MultiLineTextView
import miragefairy2024.mod.recipeviewer.views.NinePatchImageView
import miragefairy2024.mod.recipeviewer.views.ScrollView
import miragefairy2024.mod.recipeviewer.views.ScrollbarStyle
import miragefairy2024.mod.recipeviewer.views.StackView
import miragefairy2024.mod.recipeviewer.views.TextView
import miragefairy2024.mod.recipeviewer.views.TooltipBackgroundView
import miragefairy2024.mod.recipeviewer.views.View
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
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory

class TraitListScreen(handler: TraitListScreenHandler, playerInventory: Inventory, title: Component) : AbstractViewContainerScreen<TraitListScreenHandler>(handler, playerInventory, title) {

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
                    view += TextView(text { traitStack.trait.getName().style(traitStack.trait.style) }).configure {
                        view.sizingX = Sizing.FILL
                        view.alignmentX = Alignment.CENTER
                        view.shadow = false
                        position.ignoreLayoutX = true
                    }

                    view += YSpaceView(5)

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
                                view += TextView(text { condition.emoji + " "() + (factor * 100.0 formatAs "%.1f%%")() }).configure {
                                    view.shadow = false
                                }.tooltip(condition.name)
                            }
                        }

                        // 特性アイコンなのだ～🌱
                        view += ImageView(ViewTexture(traitStack.trait.texture, IntPoint(32, 32)))

                        // 効果なのだ～🌱
                        view += YListView().configure {
                            position.weight = 1.0
                            position.alignmentY = Alignment.END
                            view.sizingX = Sizing.FILL

                            traitStack.trait.traitEffectKeyEntries.forEach { entry ->
                                fun <T : Any> traitEffectView(traitEffectKey: TraitEffectKey<T>, traitEffectFactor: Double) {
                                    val value = traitEffectKey.getValue(traitEffectFactor * getTraitPower(traitStack.level) * totalConditionFactor)
                                    view += TextView(text { traitEffectKey.renderValue(value) + " "() + traitEffectKey.emoji.style(traitEffectKey.style) }).configure {
                                        view.sizingX = Sizing.FILL
                                        view.alignmentX = Alignment.END
                                        view.shadow = false
                                    }.tooltip(traitEffectKey.name)
                                }
                                traitEffectView(entry.traitEffectKey, entry.factor)
                            }
                        }
                    }

                    view += YSpaceView(5)

                    // 特性ポエムなのだ～🌱 スクロールバーは中身へ重ねるのだ～🌱
                    view += ScrollView(5, ScrollbarStyle.Flat(0xA0FFFFFF.toInt())).configure {
                        position.weight = 1.0

                        view += MultiLineTextView().configure {
                            view.text.value = text { traitStack.trait.text }
                            view.shadow = false
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

                    view += NinePatchImageView(NinePatchTextureCard.PANEL.texture, 16, 16, 5, 6, 5, 5, 6, 5, true)

                    // 内枠なのだ～🌱
                    view += YListView().configure {
                        view.sizingX = Sizing.FILL

                        // 枠の横幅はスロットの個数で決まるため、文字の長さを混ぜないのだ～🌱
                        view += TextView(text { traitListScreenTranslation() }).configure {
                            view.sizingX = Sizing.FILL
                            view.alignmentX = Alignment.CENTER
                            view.color = ColorPair.DARK_GRAY
                            view.shadow = false
                            position.ignoreLayoutX = true
                        }

                        view += YSpaceView(3)

                        // 特性カードなのだ～🌱
                        view += StackView().configure {
                            position.ignoreLayoutX = true
                            view.sizingX = Sizing.FILL

                            view += NinePatchImageView(NinePatchTextureCard.TRAIT_BACKGROUND.texture, 66, 66, 22, 22, 22, 22, 22, 22, true)

                            val traitStack = selectedTraitStack
                            if (traitStack != null) view += traitCardContentView(traitStack).configure { }.margin(5)
                        }.minContentSizeY(18 * 6).maxContentSizeY(18 * 6)

                        view += YSpaceView(4)

                        // 特性の一覧なのだ～🌱
                        view += StackView().configure {
                            position.ignoreLayoutX = true
                            view.sizingX = Sizing.FILL

                            view += TooltipBackgroundView()

                            view += ScrollView(5, ScrollbarStyle.Flat(0xA0FFFFFF.toInt())).configure {
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
                                        }).configure {
                                            view.sizingX = Sizing.FILL
                                            view.shadow = false
                                            position.ignoreLayoutX = true
                                        }.onClick {
                                            selectedTraitStack = traitStack
                                            rebuildWidgets()
                                            true
                                        }
                                    }
                                }.margin(0, 5, 0, 0)
                            }.margin(4)
                        }.minContentSizeY(18 * 4).maxContentSizeY(18 * 4)
                    }.minContentSizeX(18 * 9).margin(7)
                }
            }
        }
    }

    override fun renderBackground(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) = renderTransparentBackground(guiGraphics)

}
