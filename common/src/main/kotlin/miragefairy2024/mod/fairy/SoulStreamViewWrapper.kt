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
import miragefairy2024.util.plus
import miragefairy2024.util.red
import miragefairy2024.util.register
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
    private val enabledPassiveSkillEffectFilters: ObservableValue<Map<ResourceLocation, PassiveSkillEffectFilter<*>>>,
    private val enableGlobalFairyHighlight: ObservableValue<Boolean>,
    private val collectionToggleKeyText: Component,
    private val setCollectionEnabled: (Boolean) -> Unit,
) {

    private val player: Player = menu.playerInventory.player
    private val onViewClosed = EventRegistry<() -> Unit>()
    private var refreshEffects: (() -> Unit)? = null

    fun createView(title: Component): View {
        dispose()

        return View {
            view += StackView().configure {
                view.sizingX = Sizing.FILL
                view.sizingY = Sizing.FILL

            view += FilledRectangleView().configure {
            view.color.value = 0xFFC6C6C6.toInt()
            }
        /*
        rootComponent.apply {
            surface(Surface.flat(0xFFC6C6C6.toInt()))
            allowOverflow(true) // expandの端数処理の不具合で1pxだけはみ出すことがある
            padding(Insets.of(4))
            gap(4)
        */
        view += XListView().configure {
                    view.sizingX = Sizing.FILL
                    view.sizingY = Sizing.FILL

                    // 左ペイン
                    view += YListView().configure {
                    position.weight = 1.0
                        position.ignoreLayoutX = true
                    view.sizingX = Sizing.FILL
                    view.sizingY = Sizing.FILL
                    /*
                    child(Containers.verticalFlow(Sizing.expand(50), Sizing.fill()).apply {
                        padding(Insets.of(0, 20, 0, 0)) // レシピMOD用に下部を保護
                        gap(2)
                    */

                            /*
                        // 左ペインボタン
                        child(Containers.horizontalFlow(Sizing.fill(), Sizing.content()).apply {
                            gap(2)
                            */
                            val scrollView = YScrollContainerView()

                        // 左ペインボタン
                        view += XListView().configure {
                            // ハイライトリセットボタン
                            /*
                            child(LayeredImageButton(14, 14, BUTTON_14_BACKGROUND, run {
                                WidgetSprites(MirageFairy2024.identifier("button_14/clear_foreground"))
                            }).apply {
                                tooltip(text { SOUL_STREAM_RESET_HIGHLIGHTS_TRANSLATION() })

                                onClick.register {
                                    enabledPassiveSkillEffectFilters.value = mapOf()
                            */
                            view += LayeredImageButtonView(IntPoint(14, 14)).configure {
                            view.backgroundSprites = BUTTON_14_BACKGROUND
                                view.foregroundSprites = LayeredImageButtonView.Sprites.of(MirageFairy2024.identifier("button_14/clear_foreground"))
                                view.tooltip.value = text { SOUL_STREAM_RESET_HIGHLIGHTS_TRANSLATION() }
                                view.onClick.register {
                                    enabledPassiveSkillEffectFilters.value = emptyMap()
                                }
                            /*
                            })
                            */
                            }
                            view += XSpaceView(2)

                            // グローバル検索トグルボタン
                            /*
                            child(LayeredImageToggleButton(14, 14, BUTTON_14_BACKGROUND, run {
                                WidgetSprites(MirageFairy2024.identifier("button_14/global_search_foreground"))
                            }).apply {
                                tooltip(text { SOUL_STREAM_GLOBAL_SEARCH_TRANSLATION() })

                                enableGlobalFairyHighlight.observeAndInitialize(onClose) { _, _ ->
                                    value.value = enableGlobalFairyHighlight.value
                            */
                            view += LayeredImageButtonView(IntPoint(14, 14)).configure {
                            view.backgroundSprites = BUTTON_14_BACKGROUND
                                view.foregroundSprites = LayeredImageButtonView.Sprites.of(MirageFairy2024.identifier("button_14/global_search_foreground"))
                                view.tooltip.value = text { SOUL_STREAM_GLOBAL_SEARCH_TRANSLATION() }
                                enableGlobalFairyHighlight.observeAndInitialize(onViewClosed) { _, enabled ->
                                    view.selected.value = enabled
                                }
                                /*
                                value.register { _, _ ->
                                    enableGlobalFairyHighlight.value = value.value
                                */
                                view.onClick.register {
                                    enableGlobalFairyHighlight.value = !enableGlobalFairyHighlight.value
                                }
                            /*
                            })

                        })
                            */
                            }
                        }
                        view += YSpaceView(2)

                        // ハイライトフィルタ
                        /*
                        child(tooltipContainer(Sizing.fill(), Sizing.expand(100)).apply {
                            child(verticalScroll(Sizing.fill(), Sizing.fill(), 5).apply {
                                scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(0xA0FFFFFF.toInt())))
                                child().apply {
                        */
                        view += StackView().configure {
                        view.sizingX = Sizing.FILL
                        view.sizingY = Sizing.FILL
                        position.weight = 1.0

                            view += TooltipBackgroundView()
                            view += XListView().configure {
                                view.sizingX = Sizing.FILL
                                view.sizingY = Sizing.FILL

                            view += scrollView.configure {
                                    position.weight = 1.0
                                    position.ignoreLayoutX = true
                                    view += YListView().configure {
                                    view.sizingX = Sizing.FILL

                                    // TODO 条件フィルタ
                                    // 条件は実装しても不毛なことになりそう
                                    // 例えば満腹度は上下と数値で個別にエントリを作る必要がある
                                    // doubleで管理されるものもあり、identifierを作りにくい
                                    // ツール系は該当する妖精が1体しかない条件がずらっと並ぶことになる
                                    // 実装しても微妙なのが多すぎて折り畳み可能にしないといけないかも

                                    /*
                                    child().child(Components.label(text { SOUL_STREAM_PASSIVE_SKILL_EFFECT_TRANSLATION().blue.bold }).horizontalTextAlignment(HorizontalAlignment.CENTER).horizontalSizing(Sizing.fill()))
                                    child().child(Containers.verticalFlow(Sizing.fill(), Sizing.content()).apply {
                                    */
                                    view += TextView(text { SOUL_STREAM_PASSIVE_SKILL_EFFECT_TRANSLATION().blue.bold }).configure {
                                    view.sizingX = Sizing.FILL
                                    view.alignmentX = Alignment.CENTER
                                            view.shadow = false
                                    }
                                        passiveSkillEffectRegistry.entrySet().sortedBy { it.key.location() }.forEach { (_, effect) ->
                                            /*
                                            fun <T : Any> f(effect: PassiveSkillEffect<T>) {
                                            */
                                            fun <T : Any> addFilters(effect: PassiveSkillEffect<T>) {
                                                val samples = motifRegistry.entrySet()
                                                    .flatMap { it.value.passiveSkillSpecifications }
                                                    .mapNotNull { if (it.effect == effect) effect.castOrNull(it.valueProvider(10.0)) else null }
                                                effect.getFilters(samples).sortedBy { it.identifier }.forEach { filter ->
                                                            /*
                                                    lateinit var updateLabel: () -> Unit
                                                    child(ClickableContainer(Sizing.fill(), Sizing.content()).apply {
                                                        onClick.register {
                                                            if (filter.identifier in enabledPassiveSkillEffectFilters.value) {
                                                                enabledPassiveSkillEffectFilters.value = enabledPassiveSkillEffectFilters.value - filter.identifier
                                                            } else {
                                                                enabledPassiveSkillEffectFilters.value = enabledPassiveSkillEffectFilters.value + (filter.identifier to filter)
                                                            */
                                                            fun label(filters: Map<ResourceLocation, PassiveSkillEffectFilter<*>>) = if (filter.identifier in filters) filter.text.gold else filter.text

                                                    view += AlignView().configure {
                                                    view.sizingY = Sizing.WRAP

                                                        view += TextView().configure {
                                                            view.shadow = false
                                                            enabledPassiveSkillEffectFilters.observeAndInitialize(onViewClosed) { _, filters ->
                                                                view.text.value = label(filters).visualOrderText
                                                            }
                                                                    }.tooltip { _, _ -> listOf(label(enabledPassiveSkillEffectFilters.value)) }
                                                        }.onClick {
                                                            val filters = enabledPassiveSkillEffectFilters.value
                                                                enabledPassiveSkillEffectFilters.value = if (filter.identifier in filters) filters - filter.identifier else filters + (filter.identifier to filter)
                                                            true
                                                        }
                                                /*
                                                        child(Components.label(Component.empty()).apply {
                                                            updateLabel = {
                                                                if (filter.identifier in enabledPassiveSkillEffectFilters.value) {
                                                                    text(filter.text.gold)
                                                                    tooltip(filter.text.gold)
                                                                } else {
                                                                    text(filter.text)
                                                                    tooltip(filter.text)
                                                                }
                                                            }
                                                            enabledPassiveSkillEffectFilters.observeAndInitialize(onClose) { _, _ ->
                                                                updateLabel()
                                                            }
                                                        })
                                                    })
                                                */
                                                }
                                            }
                                            /*
                                            f(effect)
                                            */
                                            addFilters(effect)
                                        }
                                    /*
                                    })

                                    */
                                    }
                            }
                                val scrollbarView = YFlatScrollbarView().also {
                                it.scrollbarColor = 0xA0FFFFFF.toInt()
                                }
                                view += scrollbarView
                                connectScrollbar(scrollView, scrollbarView)
                            }.margin(4)
                        }
                        }.margin(0, 0, 0, RECIPE_VIEWER_MARGIN) // レシピMOD用に下部を保護
                                /*
                            })
                        })
                                */

            /*
                    })
            */
            view += XSpaceView(PANE_GAP)

                    // メインコンテナ
                    /*
                    child(Containers.verticalFlow(Sizing.fixed(18 * 9 + 18), Sizing.fill()).apply {

                        //child(inventoryNameLabel(title))

                        //child(verticalSpace(3))

                        child(Containers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
                            surface(Surface.tiled(SlotType.FAIRY.viewTexture.id, 18, 18))
                            repeat(9) { index ->
                                child(slotContainer(slotAsComponent(9 * 3 + 9 + index), type = null))
                            }
                        })

                        child(verticalSpace(4))

                        child(verticalScroll(Sizing.fill(100), Sizing.expand(), 18).apply {
                            surface(Surface.tiled(SlotType.NORMAL.viewTexture.id, 18, 18))
                            scrollbar(ScrollContainer.Scrollbar.vanilla())
                            scrollStep(18)
                            (9 until menu.soulStream.size).chunked(9).forEach { indices ->
                                child().child(Containers.horizontalFlow(Sizing.fill(100), Sizing.content()).apply {
                                    indices.forEach { index ->
                                        child(slotContainer(slotAsComponent(9 * 3 + 9 + index), type = null))
                    */
                    view += YListView().configure {
                    view.sizingY = Sizing.FILL

                        val columnCount = 9
                        val hotbarSlotIndex = columnCount * 3
                        val passiveSkillSlotIndex = hotbarSlotIndex + columnCount
                        val storageSlotIndex = passiveSkillSlotIndex + SoulStream.PASSIVE_SKILL_SLOT_COUNT

                        fun createSlotRow(slotIndices: Iterable<Int>, slotType: SlotType): View {
                            return View {
                                view += XListView().configure {
                                    slotIndices.forEach {
                                        view += menuSlotView(menu.slots[it], slotType)
                                    }
                                /*
                                })
                                */
                                }
                            }
                        /*
                        })

                        child(verticalSpace(4))

                        //child(inventoryNameLabel(menu.playerInventory.name))

                        //child(verticalSpace(1))
                        */
                        }

                        view += createSlotRow(passiveSkillSlotIndex until storageSlotIndex, SlotType.FAIRY)
                        view += YSpaceView(4)
                        view += XListView().configure {
                            view.sizingY = Sizing.FILL
                            position.weight = 1.0

                        val scrollView = YScrollContainerView().also {
                            it.scrollStep = SlotType.NORMAL.viewTexture.size.y
                        }
                        view += scrollView.configure {
                                view += YListView().configure {
                            (storageSlotIndex until menu.slots.size).chunked(columnCount).forEach {
                                view += createSlotRow(it, SlotType.NORMAL)
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

                        // プレイヤーインベントリ
                            /*
                        child(Containers.verticalFlow(Sizing.content(), Sizing.content()).apply {
                            surface(Surface.tiled(SlotType.NORMAL.viewTexture.id, 18, 18))
                            repeat(3) { r ->
                                child(Containers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
                                    repeat(9) { c ->
                                        child(slotContainer(slotAsComponent(9 * r + c), type = null))
                                    }
                                })
                            */
                            (0 until hotbarSlotIndex).chunked(columnCount).forEach {
                                view += createSlotRow(it, SlotType.NORMAL)
                            }
                        /*
                        })
                        child(verticalSpace(4))
                        child(Containers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
                            surface(Surface.tiled(SlotType.NORMAL.viewTexture.id, 18, 18))
                            repeat(9) { c ->
                                child(slotContainer(slotAsComponent(9 * 3 + c), type = null))
                            }
                        })
                        */
                        view += YSpaceView(4)
                        view += createSlotRow(hotbarSlotIndex until passiveSkillSlotIndex, SlotType.NORMAL)

                        // GUIタイトルはレシピMOD用の保護領域に持ってくる
                        /*
                        child(verticalSpace(5))
                        child(inventoryNameLabel(title).horizontalTextAlignment(HorizontalAlignment.CENTER))
                        child(verticalSpace(5))

                    })
                        */
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

            view += XSpaceView(PANE_GAP)

                    // 右ペイン
                    /*
                    child(Containers.verticalFlow(Sizing.expand(50), Sizing.fill()).apply {
                        padding(Insets.of(0, 20, 0, 0)) // レシピMOD用に下部を保護
                        gap(2)
                    */
                    view += YListView().configure {
                    position.weight = 1.0
                        position.ignoreLayoutX = true
                    view.sizingX = Sizing.FILL
                    view.sizingY = Sizing.FILL

                        // 右ペインボタン
                            /*
                        child(Containers.horizontalFlow(Sizing.fill(), Sizing.content()).apply {
                            gap(2)

                            */
                            // 収集効果の有効化切り替えボタン
                            /*
                            child(LayeredImageToggleButton(14, 14, BUTTON_14_BACKGROUND, run {
                                WidgetSprites(
                            */
                            view += LayeredImageButtonView(IntPoint(14, 14)).configure {
                            view.backgroundSprites = BUTTON_14_BACKGROUND
                                view.foregroundSprites = LayeredImageButtonView.Sprites(
                                    MirageFairy2024.identifier("button_14/collection_enabled_foreground"),
                                    MirageFairy2024.identifier("button_14/collection_disabled_foreground"),
                                    MirageFairy2024.identifier("button_14/collection_enabled_foreground"),
                                    MirageFairy2024.identifier("button_14/collection_disabled_foreground"),
                                )
                                /*
                            }).apply {
                                val player = Minecraft.getInstance().player ?: return@apply
                                fun update() {
                                    value.value = player.collectionEnabled.getOrDefault()
                                    tooltip(text { (if (value.value) COLLECTION_ENABLED_TRANSLATION() else COLLECTION_DISABLED_TRANSLATION()) + " ("() + toggleCollectionEnabledKeyMappingCard() + ")"() })
                                */
                                fun updateButton() {
                                    val enabled = player.collectionEnabled.getOrDefault()
                                    view.selected.value = enabled
                                    view.tooltip.value = text {
                                    val message = if (enabled) COLLECTION_ENABLED_TRANSLATION() else COLLECTION_DISABLED_TRANSLATION()
                                    message + " ("() + collectionToggleKeyText + ")"()
                                    }
                                }
                                /*
                                AttachmentChangedEvent.eventRegistry.observe(onClose) { identifier ->
                                */
                                AttachmentChangedEvent.eventRegistry.observe(onViewClosed) { identifier ->
                                    if (identifier == COLLECTION_ENABLED_ATTACHMENT_TYPE.identifier()) {
                                        /*
                                        update()
                                        */
                                        updateButton()
                                    }
                                }
                                /*
                                update()
                                value.register { _, _ ->
                                    if (player.collectionEnabled.getOrDefault() != value.value) {
                                        SetCollectionEnabledChannel.sendToServer(value.value)
                                    }
                                */
                                updateButton()
                                view.onClick.register {
                                val enabled = !view.selected.value
                                view.selected.value = enabled
                                    if (player.collectionEnabled.getOrDefault() != enabled) setCollectionEnabled(enabled)
                                }
                            /*
                            })

                        })
                            */
                            }
                        view += YSpaceView(2)

                        // 効果欄
                        /*
                        child(tooltipContainer(Sizing.fill(), Sizing.fill()).apply {
                            child(verticalScroll(Sizing.fill(), Sizing.fill(), 5, overlapped = true).apply {
                                scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(0xA0FFFFFF.toInt())))
                                child().apply {
                                    val container = this

                                    fun refresh() {
                                        container.clearChildren()

                                        val player = Minecraft.getInstance().player ?: return
                                        val passiveSkillProviders = player.findPassiveSkillProviders()
                                        val result = PassiveSkillResult()
                                        result.collect(passiveSkillProviders.passiveSkills, player, ManaBoostPassiveSkillEffect.Value(mapOf()), true)
                                        val manaBoostValue = result[ManaBoostPassiveSkillEffect]
                                        result.collect(passiveSkillProviders.passiveSkills, player, manaBoostValue, false)

                                        if (result.map.isEmpty()) {
                                            container.child(Components.label(text { SOUL_STREAM_NO_PASSIVE_SKILL_EFFECTS_TRANSLATION().red }))
                                        } else {
                                            result.map.keys.sortedBy { passiveSkillEffectRegistry.getKey(it) }.forEach { effect ->
                                                fun <T : Any> f(effect: PassiveSkillEffect<T>) {
                                                    val value = result[effect]
                                                    effect.getTexts(value).forEach {
                                                        container.child(Components.label(it.gold).tooltip(it.gold))
                                                    }
                                                }
                                                f(effect)
                                            }
                                        }
                        */
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
                                    /*

                                    onUpdateListeners += {
                                        refresh()
                                    */
                                    }
                            /*
                                    refresh()
                                }
                            })
                        })

                    })

                            */
                            }.margin(4)
                        }
                        }.margin(0, 0, 0, RECIPE_VIEWER_MARGIN) // レシピMOD用に下部を保護
            }.margin(PANE_MARGIN)
            }
        }
    }

    fun updateEffects() {
        refreshEffects?.invoke()
    }

    fun dispose() {
        onViewClosed.fire()
        refreshEffects = null
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
