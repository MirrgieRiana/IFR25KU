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
import miragefairy2024.util.red
import miragefairy2024.util.register
import miragefairy2024.util.removeOn
import miragefairy2024.util.text
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

private val BUTTON_14_BACKGROUND = LayeredImageButtonView.Sprites(
    MirageFairy2024.identifier("button_14/background_on"),
    MirageFairy2024.identifier("button_14/background"),
    MirageFairy2024.identifier("button_14/background_on_focused"),
    MirageFairy2024.identifier("button_14/background_focused"),
)

class SoulStreamViewWrapper(
    private val menu: SoulStreamScreenHandler,
    private val enabledPassiveSkillEffectFilters: ObservableValue<Map<ResourceLocation, PassiveSkillEffectFilter<*>>>,
    private val enableGlobalFairyHighlight: ObservableValue<Boolean>,
    private val collectionToggleKeyText: Component,
    private val setCollectionEnabled: (Boolean) -> Unit,
) {

    private val onViewClosed = EventRegistry<() -> Unit>()
    private val scrollViews = mutableListOf<YScrollContainerView>()
    private var refreshEffects: (() -> Unit)? = null

    fun createView(title: Component): View {
        val offsets = scrollViews.map { it.scrollOffsetY.value }
        dispose()

        val player = menu.playerInventory.player

        fun createScrollView(content: View): YScrollContainerView {
            val scrollView = YScrollContainerView()
            scrollView.scrollOffsetY.value = offsets.getOrElse(scrollViews.size) { 0.0 }
            scrollView += content
            scrollViews += scrollView
            return scrollView
        }

        fun createFlatScrollPane(scrollView: YScrollContainerView): View {
            return View {
                view += StackView().configure {
                    view.sizingX = Sizing.FILL
                    view.sizingY = Sizing.FILL
                    view += TooltipBackgroundView()
                    view += StackView().configure {
                        view.sizingX = Sizing.FILL
                        view.sizingY = Sizing.FILL
                        view += scrollView
                        val scrollbarView = YFlatScrollbarView().also { it.scrollbarColor = 0xA0FFFFFF.toInt() }
                        view += AlignView().configure {
                            view.alignmentX = Alignment.END
                            view += scrollbarView
                        }
                        connectScrollbar(scrollView, scrollbarView).removeOn(onViewClosed)
                    }.margin(4)
                }
            }
        }

        fun createSlotRow(indices: Iterable<Int>, type: SlotType): View {
            return View {
                view += XListView().configure {
                    indices.forEach {
                        view += menuSlotView(menu.slots[it], type)
                    }
                }
            }
        }

        return View {
            view += StackView().configure {
                view.sizingX = Sizing.FILL
                view.sizingY = Sizing.FILL

                view += FilledRectangleView().also { it.color.value = 0xFFC6C6C6.toInt() }

                view += XListView().configure {
                    view.sizingX = Sizing.FILL
                    view.sizingY = Sizing.FILL

                    // 左ペイン
                    view += YListView().configure {
                        position.ignoreLayoutX = true
                        position.weight = 1.0
                        view.sizingX = Sizing.FILL
                        view.sizingY = Sizing.FILL

                        view += XListView().configure {
                            view += LayeredImageButtonView(IntPoint(14, 14)).configure {
                                view.backgroundSprites = BUTTON_14_BACKGROUND
                                view.foregroundSprites = LayeredImageButtonView.Sprites.of(MirageFairy2024.identifier("button_14/clear_foreground"))
                                view.tooltip.value = text { SOUL_STREAM_RESET_HIGHLIGHTS_TRANSLATION() }
                                view.onClick.register {
                                    enabledPassiveSkillEffectFilters.value = mapOf()
                                }
                            }

                            view += XSpaceView(2)

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

                        val filterList = View {
                            view += YListView().configure {
                                view.sizingX = Sizing.FILL
                                view += TextView(text { SOUL_STREAM_PASSIVE_SKILL_EFFECT_TRANSLATION().blue.bold }).configure {
                                    view.sizingX = Sizing.FILL
                                    view.alignmentX = Alignment.CENTER
                                    view.shadow = false
                                }

                                passiveSkillEffectRegistry.entrySet().sortedBy { it.key.location() }.forEach { (_, effect) ->
                                    fun <T : Any> addFilters(effect: PassiveSkillEffect<T>) {
                                        val samples = motifRegistry.entrySet().flatMap { it.value.passiveSkillSpecifications }.mapNotNull {
                                            if (it.effect == effect) effect.castOrNull(it.valueProvider(10.0)) else null
                                        }
                                        effect.getFilters(samples).sortedBy { it.identifier }.forEach { filter ->
                                            fun displayText() = if (filter.identifier in enabledPassiveSkillEffectFilters.value) filter.text.gold else filter.text

                                            view += TextView(filter.text).configure {
                                                view.sizingX = Sizing.FILL
                                                view.shadow = false
                                                enabledPassiveSkillEffectFilters.observeAndInitialize(onViewClosed) { _, _ ->
                                                    view.text.value = displayText().visualOrderText
                                                }
                                            }.tooltip { _, _ ->
                                                listOf(displayText())
                                            }.onClick {
                                                val filters = enabledPassiveSkillEffectFilters.value
                                                enabledPassiveSkillEffectFilters.value = if (filter.identifier in filters) filters - filter.identifier else filters + (filter.identifier to filter)
                                                true
                                            }
                                        }
                                    }
                                    addFilters(effect)
                                }
                            }.margin(0, 5, 0, 0)
                        }
                        view += createFlatScrollPane(createScrollView(filterList)).configure {
                            position.weight = 1.0
                        }
                    }.margin(0, 0, 0, 20)

                    view += XSpaceView(4)

                    // 中央ペイン
                    view += YListView().configure {
                        view.sizingY = Sizing.FILL

                        val columnCount = 9
                        val hotbarStart = columnCount * 3
                        val passiveSkillSlotsStart = hotbarStart + columnCount
                        val storageStart = passiveSkillSlotsStart + SoulStream.PASSIVE_SKILL_SLOT_COUNT

                        view += createSlotRow(passiveSkillSlotsStart until storageStart, SlotType.FAIRY)
                        view += YSpaceView(4)

                        view += XListView().configure {
                            position.weight = 1.0
                            view.sizingY = Sizing.FILL

                            val rows = View {
                                view += YListView().configure {
                                    (storageStart until menu.slots.size).chunked(columnCount).forEach {
                                        view += createSlotRow(it, SlotType.NORMAL)
                                    }
                                }
                            }
                            val scrollView = createScrollView(rows).also {
                                it.sizingX = Sizing.WRAP
                                it.scrollStep = SlotType.NORMAL.viewTexture.size.y
                            }
                            val scrollbarView = YInventoryScrollbarView().also { it.scrollStep = scrollView.scrollStep }
                            view += scrollView
                            view += scrollbarView
                            connectScrollbar(scrollView, scrollbarView).removeOn(onViewClosed)
                        }

                        view += YSpaceView(4)

                        (0 until hotbarStart).chunked(columnCount).forEach {
                            view += createSlotRow(it, SlotType.NORMAL)
                        }

                        view += YSpaceView(4)
                        view += createSlotRow(hotbarStart until passiveSkillSlotsStart, SlotType.NORMAL)
                        view += YSpaceView(5)
                        view += TextView(title).configure {
                            position.ignoreLayoutX = true
                            view.sizingX = Sizing.FILL
                            view.alignmentX = Alignment.CENTER
                            view.color = ColorPair.DARK_GRAY
                            view.shadow = false
                        }
                        view += YSpaceView(5)
                    }

                    view += XSpaceView(4)

                    // 右ペイン
                    view += YListView().configure {
                        position.ignoreLayoutX = true
                        position.weight = 1.0
                        view.sizingX = Sizing.FILL
                        view.sizingY = Sizing.FILL

                        view += LayeredImageButtonView(IntPoint(14, 14)).configure {
                            view.backgroundSprites = BUTTON_14_BACKGROUND
                            view.foregroundSprites = LayeredImageButtonView.Sprites(
                                MirageFairy2024.identifier("button_14/collection_enabled_foreground"),
                                MirageFairy2024.identifier("button_14/collection_disabled_foreground"),
                                MirageFairy2024.identifier("button_14/collection_enabled_foreground"),
                                MirageFairy2024.identifier("button_14/collection_disabled_foreground"),
                            )

                            fun updateButton() {
                                val enabled = player.collectionEnabled.getOrDefault()
                                view.selected.value = enabled
                                view.tooltip.value = text {
                                    (if (enabled) COLLECTION_ENABLED_TRANSLATION() else COLLECTION_DISABLED_TRANSLATION()) + " ("() + collectionToggleKeyText + ")"()
                                }
                            }

                            AttachmentChangedEvent.eventRegistry.observe(onViewClosed) { identifier ->
                                if (identifier == COLLECTION_ENABLED_ATTACHMENT_TYPE.identifier()) updateButton()
                            }
                            updateButton()
                            view.onClick.register {
                                val enabled = !view.selected.value
                                view.selected.value = enabled
                                if (enabled != player.collectionEnabled.getOrDefault()) setCollectionEnabled(enabled)
                            }
                        }

                        view += YSpaceView(2)

                        var effects = collectPassiveSkillTexts()
                        val effectList = DynamicView(createEffectList(effects))
                        val scrollView = createScrollView(effectList)
                        view += createFlatScrollPane(scrollView).configure {
                            position.weight = 1.0
                        }

                        refreshEffects = {
                            val newEffects = collectPassiveSkillTexts()
                            if (effects != newEffects) {
                                effects = newEffects
                                effectList.content.value = createEffectList(effects)
                                scrollView.updateScrollBounds()
                                scrollView.clampScrollOffset()
                            }
                        }
                    }.margin(0, 0, 0, 20)
                }.margin(4)
            }
        }
    }

    private fun collectPassiveSkillTexts(): List<Component> {
        val player = menu.playerInventory.player
        val providers = player.findPassiveSkillProviders()
        val result = PassiveSkillResult()
        result.collect(providers.passiveSkills, player, ManaBoostPassiveSkillEffect.Value(mapOf()), true)
        result.collect(providers.passiveSkills, player, result[ManaBoostPassiveSkillEffect], false)
        return result.map.keys.sortedBy { passiveSkillEffectRegistry.getKey(it) }.flatMap { effect ->
            fun <T : Any> getTexts(effect: PassiveSkillEffect<T>) = effect.getTexts(result[effect])
            getTexts(effect)
        }
    }

    private fun createEffectList(effects: List<Component>): View {
        return View {
            view += YListView().configure {
                view.sizingX = Sizing.FILL
                if (effects.isEmpty()) {
                    view += TextView(text { SOUL_STREAM_NO_PASSIVE_SKILL_EFFECTS_TRANSLATION().red }).configure {
                        view.sizingX = Sizing.FILL
                        view.shadow = false
                    }
                } else {
                    effects.forEach {
                        val effectText = it.gold
                        view += TextView(effectText).configure {
                            view.sizingX = Sizing.FILL
                            view.shadow = false
                        }.tooltip(effectText)
                    }
                }
            }.margin(0, 5, 0, 0)
        }
    }

    fun clampScrollOffsets() {
        scrollViews.forEach {
            it.clampScrollOffset()
        }
    }

    fun updateEffects() {
        refreshEffects?.invoke()
    }

    fun dispose() {
        refreshEffects = null
        onViewClosed.fire()
        scrollViews.clear()
    }

}
