package miragefairy2024.mod.fairy

import miragefairy2024.MirageFairy2024
import miragefairy2024.mod.AttachmentChangedEvent
import miragefairy2024.mod.common.SlotType
import miragefairy2024.mod.passiveskill.PassiveSkillEffect
import miragefairy2024.mod.passiveskill.PassiveSkillEffectFilter
import miragefairy2024.mod.passiveskill.PassiveSkillResult
import miragefairy2024.mod.passiveskill.collect
import miragefairy2024.mod.passiveskill.effects.ManaBoostPassiveSkillEffect
import miragefairy2024.mod.passiveskill.findPassiveSkillProviders
import miragefairy2024.mod.passiveskill.passiveSkillEffectRegistry
import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.ColorPair
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.views.AlignView
import miragefairy2024.mod.recipeviewer.views.DynamicView
import miragefairy2024.mod.recipeviewer.views.FilledRectangleView
import miragefairy2024.mod.recipeviewer.views.LayeredImageButtonView
import miragefairy2024.mod.recipeviewer.views.StackView
import miragefairy2024.mod.recipeviewer.views.TextView
import miragefairy2024.mod.recipeviewer.views.TickView
import miragefairy2024.mod.recipeviewer.views.TooltipBackgroundView
import miragefairy2024.mod.recipeviewer.views.View
import miragefairy2024.mod.recipeviewer.views.XListView
import miragefairy2024.mod.recipeviewer.views.XSpaceView
import miragefairy2024.mod.recipeviewer.views.YFlatScrollbarView
import miragefairy2024.mod.recipeviewer.views.YInventoryScrollbarView
import miragefairy2024.mod.recipeviewer.views.YListView
import miragefairy2024.mod.recipeviewer.views.YScrollContainerView
import miragefairy2024.mod.recipeviewer.views.YSpaceView
import miragefairy2024.mod.recipeviewer.views.configure
import miragefairy2024.mod.recipeviewer.views.connectScrollbar
import miragefairy2024.mod.recipeviewer.views.margin
import miragefairy2024.mod.recipeviewer.views.menuSlotView
import miragefairy2024.mod.recipeviewer.views.onClick
import miragefairy2024.mod.recipeviewer.views.plusAssign
import miragefairy2024.mod.recipeviewer.views.tooltip
import miragefairy2024.util.EventRegistry
import miragefairy2024.util.ObservableValue
import miragefairy2024.util.blue
import miragefairy2024.util.bold
import miragefairy2024.util.fire
import miragefairy2024.util.getOrDefault
import miragefairy2024.util.gold
import miragefairy2024.util.invoke
import miragefairy2024.util.observe
import miragefairy2024.util.observeAndInitialize
import miragefairy2024.util.plus
import miragefairy2024.util.red
import miragefairy2024.util.register
import miragefairy2024.util.text
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Player

private val BUTTON_14_BACKGROUND = LayeredImageButtonView.Sprites(
    MirageFairy2024.identifier("button_14/background_on"),
    MirageFairy2024.identifier("button_14/background"),
    MirageFairy2024.identifier("button_14/background_on_focused"),
    MirageFairy2024.identifier("button_14/background_focused"),
)

class SoulStreamScreenContent(
    private val menu: SoulStreamScreenHandler,
    private val enabledPassiveSkillEffectFilters: ObservableValue<Map<ResourceLocation, PassiveSkillEffectFilter<*>>>,
    private val enableGlobalFairyHighlight: ObservableValue<Boolean>,
    private val collectionToggleKeyText: Component,
    private val setCollectionEnabled: (Boolean) -> Unit,
) {

    private val player: Player = menu.playerInventory.player

    private val onViewClosed = EventRegistry<() -> Unit>()
    private var lastUpdateEventNanoTime = 0L

    fun createView(title: Component): View {
        dispose()

        return View {
            // 灰色背景
            view += StackView().configure {
                view.sizingX = Sizing.FILL
                view.sizingY = Sizing.FILL

                view += FilledRectangleView().configure {
                    view.color.value = 0xFFC6C6C6.toInt()
                }
                view += XListView().configure {
                    view.sizingX = Sizing.FILL
                    view.sizingY = Sizing.FILL

                    // 左ペイン
                    view += YListView().configure {
                        position.weight = 1.0
                        position.ignoreLayoutX = true
                        view.sizingX = Sizing.FILL
                        view.sizingY = Sizing.FILL

                        // 左ペインボタン
                        view += XListView().configure {

                            // ハイライトリセットボタン
                            view += LayeredImageButtonView(IntPoint(14, 14)).configure {
                                view.backgroundSprites = BUTTON_14_BACKGROUND
                                view.foregroundSprites = LayeredImageButtonView.Sprites.of(MirageFairy2024.identifier("button_14/clear_foreground"))
                                view.tooltip.value = text { SOUL_STREAM_RESET_HIGHLIGHTS_TRANSLATION() }
                                view.onClick.register {
                                    enabledPassiveSkillEffectFilters.value = emptyMap()
                                }
                            }

                            view += XSpaceView(2)

                            // グローバル検索トグルボタン
                            view += LayeredImageButtonView(IntPoint(14, 14)).configure {
                                view.backgroundSprites = BUTTON_14_BACKGROUND
                                view.foregroundSprites = LayeredImageButtonView.Sprites.of(MirageFairy2024.identifier("button_14/global_search_foreground"))
                                view.tooltip.value = text { SOUL_STREAM_GLOBAL_SEARCH_TRANSLATION() }
                                enableGlobalFairyHighlight.observeAndInitialize(onViewClosed) { _, enabled ->
                                    view.selected.value = enabled
                                }
                                view.onClick.register {
                                    enableGlobalFairyHighlight.value = !enableGlobalFairyHighlight.value
                                }
                            }

                        }

                        view += YSpaceView(2)

                        // ハイライトフィルタ
                        view += StackView().configure {
                            view.sizingX = Sizing.FILL
                            view.sizingY = Sizing.FILL
                            position.weight = 1.0

                            view += TooltipBackgroundView()
                            view += XListView().configure {
                                view.sizingX = Sizing.FILL
                                view.sizingY = Sizing.FILL

                                val scrollView = YScrollContainerView()
                                view += scrollView.configure {
                                    position.weight = 1.0
                                    // 長すぎるテキストで UI を壊さないためなのだ～🌱
                                    position.ignoreLayoutX = true
                                    view += YListView().configure {
                                        view.sizingX = Sizing.FILL

                                        // TODO 条件フィルタ
                                        // 条件は実装しても不毛なことになりそう
                                        // 例えば満腹度は上下と数値で個別にエントリを作る必要がある
                                        // doubleで管理されるものもあり、identifierを作りにくい
                                        // ツール系は該当する妖精が1体しかない条件がずらっと並ぶことになる
                                        // 実装しても微妙なのが多すぎて折り畳み可能にしないといけないかも

                                        view += TextView(text { SOUL_STREAM_PASSIVE_SKILL_EFFECT_TRANSLATION().blue.bold }).configure {
                                            view.sizingX = Sizing.FILL
                                            view.alignmentX = Alignment.CENTER
                                            view.shadow = false
                                        }
                                        passiveSkillEffectRegistry.entrySet().sortedBy { it.key.location() }.forEach { (_, effect) ->
                                            fun <T : Any> f(effect: PassiveSkillEffect<T>) {
                                                val samples = motifRegistry.entrySet()
                                                    .flatMap { it.value.passiveSkillSpecifications }
                                                    .mapNotNull { if (it.effect == effect) effect.castOrNull(it.valueProvider(10.0)) else null }
                                                effect.getFilters(samples).sortedBy { it.identifier }.forEach { filter ->
                                                    view += AlignView().configure {
                                                        view.sizingY = Sizing.WRAP

                                                        view += TextView().configure {
                                                            view.shadow = false
                                                            enabledPassiveSkillEffectFilters.observeAndInitialize(onViewClosed) { _, filters ->
                                                                if (filter.identifier in filters) {
                                                                    view.text.value = filter.text.gold.visualOrderText
                                                                } else {
                                                                    view.text.value = filter.text.visualOrderText
                                                                }
                                                            }
                                                        }.tooltip { _, _ ->
                                                            if (filter.identifier in enabledPassiveSkillEffectFilters.value) {
                                                                listOf(filter.text.gold)
                                                            } else {
                                                                listOf(filter.text)
                                                            }
                                                        }
                                                    }.onClick {
                                                        if (filter.identifier in enabledPassiveSkillEffectFilters.value) {
                                                            enabledPassiveSkillEffectFilters.value = enabledPassiveSkillEffectFilters.value - filter.identifier
                                                        } else {
                                                            enabledPassiveSkillEffectFilters.value = enabledPassiveSkillEffectFilters.value + (filter.identifier to filter)
                                                        }
                                                        true
                                                    }
                                                }
                                            }
                                            f(effect)
                                        }
                                    }
                                }
                                val scrollbarView = YFlatScrollbarView().also {
                                    it.scrollbarColor = 0xA0FFFFFF.toInt()
                                }
                                view += scrollbarView
                                connectScrollbar(scrollView, scrollbarView)
                            }.margin(4)
                        }

                    }.margin(0, 0, 0, 20) // レシピMOD用に下部を保護

                    view += XSpaceView(4)

                    // メインコンテナ
                    view += YListView().configure {
                        view.sizingY = Sizing.FILL

                        fun createSlotRow(startSlotIndex: Int, slotType: SlotType): View {
                            return View {
                                view += XListView().configure {
                                    repeat(9) { c ->
                                        view += menuSlotView(menu.slots[startSlotIndex + c], slotType)
                                    }
                                }
                            }
                        }

                        //view += inventoryNameLabel(title)

                        //view += YSpaceView(3)

                        view += createSlotRow(9 * 3 + 9, SlotType.FAIRY)

                        view += YSpaceView(4)

                        view += XListView().configure {
                            view.sizingY = Sizing.FILL
                            position.weight = 1.0

                            val scrollView = YScrollContainerView().also {
                                it.scrollStep = SlotType.NORMAL.viewTexture.size.y
                            }
                            view += scrollView.configure {
                                view += YListView().configure {
                                    (9 * 3 + 9 + 9 until menu.slots.size).chunked(9).forEach { indices ->
                                        view += createSlotRow(indices.first(), SlotType.NORMAL)
                                    }
                                }
                            }
                            val scrollbarView = YInventoryScrollbarView().also {
                                it.scrollStep = scrollView.scrollStep
                            }
                            view += scrollbarView
                            connectScrollbar(scrollView, scrollbarView)
                        }

                        view += YSpaceView(4)

                        //view += inventoryNameLabel(menu.playerInventory.name)

                        //view += YSpaceView(1)

                        // プレイヤーインベントリ
                        repeat(3) { r ->
                            view += createSlotRow(9 * r, SlotType.NORMAL)
                        }
                        view += YSpaceView(4)
                        view += createSlotRow(9 * 3, SlotType.NORMAL)

                        // GUIタイトルはレシピMOD用の保護領域に持ってくる
                        view += YSpaceView(5)
                        view += TextView(title).configure {
                            // 長すぎるテキストで UI を壊さないためなのだ～🌱
                            position.ignoreLayoutX = true
                            view.sizingX = Sizing.FILL
                            view.color = ColorPair.DARK_GRAY
                            view.shadow = false
                            view.alignmentX = Alignment.CENTER
                        }
                        view += YSpaceView(5)

                    }

                    view += XSpaceView(4)

                    // 右ペイン
                    view += YListView().configure {
                        position.weight = 1.0
                        // 長すぎるテキストで UI を壊さないためなのだ～🌱
                        position.ignoreLayoutX = true
                        view.sizingX = Sizing.FILL
                        view.sizingY = Sizing.FILL

                        // 右ペインボタン
                        run {

                            // 収集効果の有効化切り替えボタン
                            view += LayeredImageButtonView(IntPoint(14, 14)).configure {
                                view.backgroundSprites = BUTTON_14_BACKGROUND
                                view.foregroundSprites = LayeredImageButtonView.Sprites(
                                    MirageFairy2024.identifier("button_14/collection_enabled_foreground"),
                                    MirageFairy2024.identifier("button_14/collection_disabled_foreground"),
                                    MirageFairy2024.identifier("button_14/collection_enabled_foreground"),
                                    MirageFairy2024.identifier("button_14/collection_disabled_foreground"),
                                )
                                fun update() {
                                    val enabled = player.collectionEnabled.getOrDefault()
                                    view.selected.value = enabled
                                    view.tooltip.value = text {
                                        val message = if (enabled) COLLECTION_ENABLED_TRANSLATION() else COLLECTION_DISABLED_TRANSLATION()
                                        message + " ("() + collectionToggleKeyText + ")"()
                                    }
                                }
                                AttachmentChangedEvent.eventRegistry.observe(onViewClosed) { identifier ->
                                    if (identifier == COLLECTION_ENABLED_ATTACHMENT_TYPE.identifier()) {
                                        update()
                                    }
                                }
                                update()
                                view.onClick.register {
                                    val enabled = !view.selected.value
                                    view.selected.value = enabled
                                    if (player.collectionEnabled.getOrDefault() != enabled) {
                                        setCollectionEnabled(enabled)
                                    }
                                }
                            }

                        }

                        view += YSpaceView(2)

                        // 効果欄
                        view += StackView().configure {
                            view.sizingX = Sizing.FILL
                            view.sizingY = Sizing.FILL
                            position.weight = 1.0

                            view += TooltipBackgroundView()
                            view += StackView().configure {
                                view.sizingX = Sizing.FILL
                                view.sizingY = Sizing.FILL

                                fun collectEffectList(): List<Component> {
                                    val passiveSkillProviders = player.findPassiveSkillProviders()
                                    val result = PassiveSkillResult()
                                    result.collect(passiveSkillProviders.passiveSkills, player, ManaBoostPassiveSkillEffect.Value(mapOf()), true)
                                    val manaBoostValue = result[ManaBoostPassiveSkillEffect]
                                    result.collect(passiveSkillProviders.passiveSkills, player, manaBoostValue, false)
                                    return result.map.keys.sortedBy { passiveSkillEffectRegistry.getKey(it) }.flatMap { effect ->
                                        fun <T : Any> f(effect: PassiveSkillEffect<T>) = effect.getTexts(result[effect]).map { it.gold }
                                        f(effect)
                                    }
                                }

                                fun createEffectListView(effects: List<Component>): View {
                                    return View {
                                        view += YListView().configure {
                                            if (effects.isEmpty()) {
                                                view += TextView(text { SOUL_STREAM_NO_PASSIVE_SKILL_EFFECTS_TRANSLATION().red }).configure {
                                                    view.shadow = false
                                                }
                                            } else {
                                                effects.forEach {
                                                    view += TextView(it).configure {
                                                        view.shadow = false
                                                    }.tooltip(it)
                                                }
                                            }
                                        }
                                    }
                                }

                                var effectList = collectEffectList()
                                val effectListDynamicView = DynamicView(createEffectListView(effectList))
                                val scrollView = YScrollContainerView().also {
                                    it += effectListDynamicView
                                }
                                view += scrollView
                                val scrollbarView = YFlatScrollbarView().also {
                                    it.scrollbarColor = 0xA0FFFFFF.toInt()
                                }
                                view += AlignView().configure {
                                    view.alignmentX = Alignment.END
                                    view += scrollbarView
                                }
                                connectScrollbar(scrollView, scrollbarView)

                                view += TickView {
                                    val now = System.nanoTime()
                                    if (now - lastUpdateEventNanoTime < 100_000_000) return@TickView
                                    lastUpdateEventNanoTime = now

                                    val newEffectList = collectEffectList()
                                    if (effectList != newEffectList) {
                                        effectList = newEffectList

                                        effectListDynamicView.content.value = createEffectListView(effectList)
                                        scrollView.updateScrollBounds()
                                        scrollView.clampScrollOffset()
                                    }
                                }
                            }.margin(4)
                        }

                    }.margin(0, 0, 0, 20) // レシピMOD用に下部を保護

                }.margin(4)
            }
        }
    }

    fun dispose() {
        onViewClosed.fire()
    }

}
