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
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.evaluateActualSize
import miragefairy2024.mod.recipeviewer.view.plus
import miragefairy2024.mod.recipeviewer.view.sized
import miragefairy2024.mod.recipeviewer.views.AlignView
import miragefairy2024.mod.recipeviewer.views.DynamicView
import miragefairy2024.mod.recipeviewer.views.FilledRectangleView
import miragefairy2024.mod.recipeviewer.views.LayeredImageButtonView
import miragefairy2024.mod.recipeviewer.views.StackView
import miragefairy2024.mod.recipeviewer.views.TextView
import miragefairy2024.mod.recipeviewer.views.TooltipBackgroundView
import miragefairy2024.mod.recipeviewer.views.View
import miragefairy2024.mod.recipeviewer.views.WrapperView
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
import miragefairy2024.util.registerAndInitialize
import miragefairy2024.util.text
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Player

private const val PANE_MARGIN = 4
private const val PANE_GAP = 4
private const val RECIPE_VIEWER_MARGIN = 20

private val BUTTON_14_BACKGROUND = LayeredImageButtonView.Sprites(
    MirageFairy2024.identifier("button_14/background_on"),
    MirageFairy2024.identifier("button_14/background"),
    MirageFairy2024.identifier("button_14/background_on_focused"),
    MirageFairy2024.identifier("button_14/background_focused"),
)

class SoulStreamViewWrapper(
    private val menu: SoulStreamScreenHandler,
    private val title: Component,
    private val enabledPassiveSkillEffectFilters: ObservableValue<Map<ResourceLocation, PassiveSkillEffectFilter<*>>>,
    private val enableGlobalFairyHighlight: ObservableValue<Boolean>,
    private val collectionToggleKeyText: Component,
    private val setCollectionEnabled: (Boolean) -> Unit,
) {

    private val player: Player = menu.playerInventory.player
    private val onViewClosed = EventRegistry<() -> Unit>()
    private var filterScrollView: YScrollContainerView? = null
    private var storageScrollView: YScrollContainerView? = null
    private var effectScrollView: YScrollContainerView? = null
    private var refreshEffects: (() -> Unit)? = null
    private var panes = emptyList<View>()

    fun clampScrollOffsets() {
        filterScrollView?.clampScrollOffset()
        storageScrollView?.clampScrollOffset()
        effectScrollView?.clampScrollOffset()
    }

    fun updateEffects() {
        refreshEffects?.invoke()
    }

    fun dispose() {
        onViewClosed.fire()
        refreshEffects = null
        panes = emptyList()
    }

    fun getRecipeViewerExclusionAreas(): List<IntRectangle> {
        var x = PANE_MARGIN
        return panes.map { pane ->
            val size = pane.evaluateActualSize()
            val bounds = IntRectangle(x, PANE_MARGIN, size.x, size.y)
            x += size.x + PANE_GAP
            bounds
        }
    }

    fun createView(): View {
        val filterPane = createFilterPane()
        val inventoryPane = createInventoryPane()
        val effectPane = createEffectPane()
        panes = listOf(filterPane, inventoryPane, effectPane)

        return View {
            view += StackView().configure {
                view.sizingX = Sizing.FILL
                view.sizingY = Sizing.FILL

                view += FilledRectangleView().configure {
                    view.color.value = 0xFFC6C6C6.toInt()
                }
                view += XListView().configure {
                    view.sizingX = Sizing.FILL
                    view.sizingY = Sizing.FILL

                    // レシピビューワー用に左右の下部を空けるのだ～🌱
                    view += filterPane.configure {
                        position.weight = 1.0
                        position.ignoreLayoutX = true
                    }.margin(0, 0, 0, RECIPE_VIEWER_MARGIN)
                    view += XSpaceView(PANE_GAP)
                    view += inventoryPane
                    view += XSpaceView(PANE_GAP)
                    view += effectPane.configure {
                        position.weight = 1.0
                        position.ignoreLayoutX = true
                    }.margin(0, 0, 0, RECIPE_VIEWER_MARGIN)

                }.margin(PANE_MARGIN)

            }
        }
    }

    private fun createFilterPane(): View {
        return View {
            view += YListView().configure {
                view.sizingX = Sizing.FILL
                view.sizingY = Sizing.FILL

                view += XListView().configure {

                    view += LayeredImageButtonView(
                        IntPoint(14, 14),
                        BUTTON_14_BACKGROUND,
                        LayeredImageButtonView.Sprites(MirageFairy2024.identifier("button_14/clear_foreground")),
                        text { SOUL_STREAM_RESET_HIGHLIGHTS_TRANSLATION() },
                    ).configure {
                        view.onClick.register { enabledPassiveSkillEffectFilters.value = emptyMap() }
                    }
                    view += XSpaceView(2)
                    view += LayeredImageButtonView(
                        IntPoint(14, 14),
                        BUTTON_14_BACKGROUND,
                        LayeredImageButtonView.Sprites(MirageFairy2024.identifier("button_14/global_search_foreground")),
                        text { SOUL_STREAM_GLOBAL_SEARCH_TRANSLATION() },
                    ).configure {
                        enableGlobalFairyHighlight.observeAndInitialize(onViewClosed) { _, enabled -> view.selected.value = enabled }
                        view.onClick.register { enableGlobalFairyHighlight.value = !enableGlobalFairyHighlight.value }
                    }

                }
                view += YSpaceView(2)
                view += StackView().configure {
                    view.sizingX = Sizing.FILL
                    view.sizingY = Sizing.FILL
                    position.weight = 1.0

                    view += TooltipBackgroundView()
                    view += XListView().configure {
                        view.sizingX = Sizing.FILL
                        view.sizingY = Sizing.FILL

                        val scrollView = YScrollContainerView().also {
                            it.scrollOffsetY.value = filterScrollView?.scrollOffsetY?.value ?: 0.0
                            filterScrollView = it
                        }.configure {
                            position.weight = 1.0
                            position.ignoreLayoutX = true
                            view += createFilterList()
                        }
                        view += scrollView
                        val scrollbarView = YFlatScrollbarView().also {
                            it.scrollbarColor = 0xA0FFFFFF.toInt()
                        }
                        view += scrollbarView
                        connectScrollbar(scrollView.view, scrollbarView)

                    }.margin(4)

                }

            }
        }
    }

    private fun createFilterList(): View {
        return View {
            view += YListView().configure {
                view.sizingX = Sizing.FILL

                view += TextView(text { SOUL_STREAM_PASSIVE_SKILL_EFFECT_TRANSLATION().blue.bold }).configure {
                    view.sizingX = Sizing.FILL
                    view.alignmentX = Alignment.CENTER
                    view.shadow = false
                }
                passiveSkillEffectRegistry.entrySet().sortedBy { it.key.location() }.forEach { (_, effect) ->
                    fun <T : Any> addFilters(effect: PassiveSkillEffect<T>) {
                        val samples = motifRegistry.entrySet()
                            .flatMap { it.value.passiveSkillSpecifications }
                            .mapNotNull { if (it.effect == effect) effect.castOrNull(it.valueProvider(10.0)) else null }
                        effect.getFilters(samples).sortedBy { it.identifier }.forEach { filter ->
                            var label = filter.text
                            view += SoulStreamFilterView {
                                val filters = enabledPassiveSkillEffectFilters.value
                                enabledPassiveSkillEffectFilters.value = if (filter.identifier in filters) filters - filter.identifier else filters + (filter.identifier to filter)
                            }.configure {
                                view += TextView().configure {
                                    view.shadow = false
                                    val text = view.text
                                    enabledPassiveSkillEffectFilters.observeAndInitialize(onViewClosed) { _, filters ->
                                        label = if (filter.identifier in filters) filter.text.gold else filter.text
                                        text.value = label.visualOrderText
                                    }
                                }.tooltip { _, _ -> listOf(label) }
                            }
                        }
                    }
                    addFilters(effect)
                }

            }
        }
    }

    private fun createInventoryPane(): View {
        val columnCount = 9
        val inventorySlotCount = columnCount * 3
        val soulStreamSlotIndexOffset = inventorySlotCount + columnCount
        val storageSlotIndexOffset = soulStreamSlotIndexOffset + SoulStream.PASSIVE_SKILL_SLOT_COUNT
        return View {
            view += YListView().configure {
                view.sizingY = Sizing.FILL

                view += createSlotRow(soulStreamSlotIndexOffset until storageSlotIndexOffset, SlotType.FAIRY)
                view += YSpaceView(4)
                view += XListView().configure {
                    view.sizingY = Sizing.FILL
                    position.weight = 1.0

                    val scrollView = YScrollContainerView().also {
                        it.scrollStep = SlotType.NORMAL.viewTexture.size.y
                        it.scrollOffsetY.value = storageScrollView?.scrollOffsetY?.value ?: 0.0
                        storageScrollView = it
                    }
                    view += scrollView.configure {
                        view += YListView().configure {
                            (storageSlotIndexOffset until menu.slots.size).chunked(columnCount).forEach {
                                view += createSlotRow(it, SlotType.NORMAL)
                            }
                        }
                    }
                    val scrollbarView = YInventoryScrollbarView().also {
                        it.scrollStep = scrollView.scrollStep
                    }
                    view += scrollbarView
                    scrollView.scrollBounds.registerAndInitialize { _, bounds ->
                        scrollbarView.contentLength = bounds.contentLength
                        scrollbarView.viewportLength = bounds.viewportLength
                    }
                    scrollView.scrollOffsetY.registerAndInitialize { _, offset -> scrollbarView.scrollOffset.value = offset }
                    scrollbarView.scrollOffset.register { _, offset -> scrollView.scrollOffsetY.value = offset }

                }
                view += YSpaceView(4)
                (0 until inventorySlotCount).chunked(columnCount).forEach {
                    view += createSlotRow(it, SlotType.NORMAL)
                }
                view += YSpaceView(4)
                view += createSlotRow(inventorySlotCount until soulStreamSlotIndexOffset, SlotType.NORMAL)
                view += YSpaceView(5)
                view += TextView(title).configure {
                    position.ignoreLayoutX = true
                    view.sizingX = Sizing.FILL
                    view.color = ColorPair.DARK_GRAY
                    view.shadow = false
                    view.alignmentX = Alignment.CENTER
                }
                view += YSpaceView(5)

            }
        }
    }

    private fun createSlotRow(slotIndices: Iterable<Int>, slotType: SlotType): View {
        return View {
            view += XListView().configure {
                slotIndices.forEach {
                    view += menuSlotView(menu.slots[it], slotType)
                }
            }
        }
    }

    private fun createEffectPane(): View {
        return View {
            view += YListView().configure {
                view.sizingX = Sizing.FILL
                view.sizingY = Sizing.FILL

                val collectionEnabled = player.collectionEnabled.getOrDefault()
                fun collectionTooltip(enabled: Boolean): Component {
                    return text {
                        val message = if (enabled) COLLECTION_ENABLED_TRANSLATION() else COLLECTION_DISABLED_TRANSLATION()
                        message + " ("() + collectionToggleKeyText + ")"()
                    }
                }

                view += LayeredImageButtonView(
                    IntPoint(14, 14),
                    BUTTON_14_BACKGROUND,
                    LayeredImageButtonView.Sprites(
                        MirageFairy2024.identifier("button_14/collection_enabled_foreground"),
                        MirageFairy2024.identifier("button_14/collection_disabled_foreground"),
                        MirageFairy2024.identifier("button_14/collection_enabled_foreground"),
                        MirageFairy2024.identifier("button_14/collection_disabled_foreground"),
                    ),
                    collectionTooltip(collectionEnabled),
                ).configure {
                    view.selected.value = collectionEnabled
                    AttachmentChangedEvent.eventRegistry.observe(onViewClosed) { identifier ->
                        if (identifier == COLLECTION_ENABLED_ATTACHMENT_TYPE.identifier()) {
                            val enabled = player.collectionEnabled.getOrDefault()
                            view.selected.value = enabled
                            view.tooltip.value = collectionTooltip(enabled)
                        }
                    }
                    view.onClick.register {
                        val enabled = !view.selected.value
                        view.selected.value = enabled
                        if (player.collectionEnabled.getOrDefault() != enabled) setCollectionEnabled(enabled)
                    }
                }
                view += YSpaceView(2)
                view += StackView().configure {
                    view.sizingX = Sizing.FILL
                    view.sizingY = Sizing.FILL
                    position.weight = 1.0

                    view += TooltipBackgroundView()
                    view += StackView().configure {
                        view.sizingX = Sizing.FILL
                        view.sizingY = Sizing.FILL

                        var effects = collectPassiveSkillTexts()
                        val effectList = DynamicView(createEffectList(effects))
                        val scrollView = YScrollContainerView().also {
                            it += effectList
                            it.scrollOffsetY.value = effectScrollView?.scrollOffsetY?.value ?: 0.0
                            effectScrollView = it
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
                        refreshEffects = {
                            val newEffects = collectPassiveSkillTexts()
                            if (effects != newEffects) {
                                effects = newEffects
                                effectList.content.value = createEffectList(effects)
                                scrollView.updateScrollBounds()
                                scrollView.clampScrollOffset()
                            }
                        }

                    }.margin(4)

                }

            }
        }
    }

    private fun createEffectList(effects: List<Component>): View {
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

    private fun collectPassiveSkillTexts(): List<Component> {
        val passiveSkillProviders = player.findPassiveSkillProviders()
        val result = PassiveSkillResult()
        result.collect(passiveSkillProviders.passiveSkills, player, ManaBoostPassiveSkillEffect.Value(mapOf()), true)
        val manaBoostValue = result[ManaBoostPassiveSkillEffect]
        result.collect(passiveSkillProviders.passiveSkills, player, manaBoostValue, false)
        return result.map.keys.sortedBy { passiveSkillEffectRegistry.getKey(it) }.flatMap { effect ->
            fun <T : Any> getTexts(effect: PassiveSkillEffect<T>) = effect.getTexts(result[effect]).map { it.gold }
            getTexts(effect)
        }
    }

}

class SoulStreamFilterView(val onClick: () -> Unit) : WrapperView(), PlaceableView {

    override val sizingX = Sizing.FILL

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): ViewController {
        return super.attachTo(offset, viewPlacer) + viewPlacer.place(this, offset.sized(evaluateActualSize()))
    }

}
