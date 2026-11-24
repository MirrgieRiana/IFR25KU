package miragefairy2024.mod.fairy

import miragefairy2024.mod.recipeviewer.views.View

class SoulStreamScreenContent {

    val view = View {
        TODO()
/*
class SoulStreamScreen(handler: SoulStreamScreenHandler, playerInventory: Inventory, title: Component) : BaseOwoHandledScreen<FlowLayout, SoulStreamScreenHandler>(handler, playerInventory, title) {

    // カーソルをインベントリ画面での位置に戻す
    private var isFirst = true
    override fun render(vanillaContext: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        if (isFirst) {
            isFirst = false
            if (lastMousePositionInInventory != null) {
                InputConstants.grabOrReleaseMouse(this.minecraft!!.window.window, InputConstants.CURSOR_NORMAL, lastMousePositionInInventory!!.first, lastMousePositionInInventory!!.second)
            }
        }
        super.render(vanillaContext, mouseX, mouseY, delta)
    }

    override fun createAdapter(): OwoUIAdapter<FlowLayout> = OwoUIAdapter.create(this, Containers::horizontalFlow)
    override fun build(rootComponent: FlowLayout) {
        rootComponent.apply {
            surface(Surface.flat(0xFFC6C6C6.toInt()))
            allowOverflow(true) // expandの端数処理の不具合で1pxだけはみ出すことがある
            padding(Insets.of(4))
            gap(4)

                    // 左ペイン
                    child(Containers.verticalFlow(Sizing.expand(50), Sizing.fill()).apply {
                        padding(Insets.of(0, 20, 0, 0)) // レシピMOD用に下部を保護
                        gap(2)

                        // 左ペインボタン
                        child(Containers.horizontalFlow(Sizing.fill(), Sizing.content()).apply {
                            gap(2)

                            // ハイライトリセットボタン
                            child(LayeredImageButton(14, 14, BUTTON_14_BACKGROUND, run {
                                WidgetSprites(MirageFairy2024.identifier("button_14/clear_foreground"))
                            }).apply {
                                tooltip(text { SOUL_STREAM_RESET_HIGHLIGHTS_TRANSLATION() })

                                onClick.register {
                                    enabledPassiveSkillEffectFilters.value = mapOf()
                                }
                            })

                            // グローバル検索トグルボタン
                            child(LayeredImageToggleButton(14, 14, BUTTON_14_BACKGROUND, run {
                                WidgetSprites(MirageFairy2024.identifier("button_14/global_search_foreground"))
                            }).apply {
                                tooltip(text { SOUL_STREAM_GLOBAL_SEARCH_TRANSLATION() })

                                enableGlobalFairyHighlight.observeAndInitialize(onClose) { _, _ ->
                                    value.value = enableGlobalFairyHighlight.value
                                }
                                value.register { _, _ ->
                                    enableGlobalFairyHighlight.value = value.value
                                }
                            })

                        })

                        // ハイライトフィルタ
                        child(tooltipContainer(Sizing.fill(), Sizing.expand(100)).apply {
                            child(verticalScroll(Sizing.fill(), Sizing.fill(), 5).apply {
                                scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(0xA0FFFFFF.toInt())))
                                child().apply {

                                    // TODO 条件フィルタ
                                    // 条件は実装しても不毛なことになりそう
                                    // 例えば満腹度は上下と数値で個別にエントリを作る必要がある
                                    // doubleで管理されるものもあり、identifierを作りにくい
                                    // ツール系は該当する妖精が1体しかない条件がずらっと並ぶことになる
                                    // 実装しても微妙なのが多すぎて折り畳み可能にしないといけないかも

                                    child().child(Components.label(text { SOUL_STREAM_PASSIVE_SKILL_EFFECT_TRANSLATION().blue.bold }).horizontalTextAlignment(HorizontalAlignment.CENTER).horizontalSizing(Sizing.fill()))
                                    child().child(Containers.verticalFlow(Sizing.fill(), Sizing.content()).apply {
                                        passiveSkillEffectRegistry.entrySet().sortedBy { it.key.location() }.forEach { (_, effect) ->
                                            fun <T : Any> f(effect: PassiveSkillEffect<T>) {
                                                val samples = motifRegistry.entrySet()
                                                    .flatMap { it.value.passiveSkillSpecifications }
                                                    .mapNotNull { if (it.effect == effect) effect.castOrNull(it.valueProvider(10.0)) else null }
                                                effect.getFilters(samples).sortedBy { it.identifier }.forEach { filter ->
                                                    lateinit var updateLabel: () -> Unit
                                                    child(ClickableContainer(Sizing.fill(), Sizing.content()).apply {
                                                        onClick.register {
                                                            if (filter.identifier in enabledPassiveSkillEffectFilters.value) {
                                                                enabledPassiveSkillEffectFilters.value = enabledPassiveSkillEffectFilters.value - filter.identifier
                                                            } else {
                                                                enabledPassiveSkillEffectFilters.value = enabledPassiveSkillEffectFilters.value + (filter.identifier to filter)
                                                            }
                                                            true
                                                        }
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
                                                }
                                            }
                                            f(effect)
                                        }
                                    })

                                }
                            })
                        })

                    })

                    // メインコンテナ
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
                                    }
                                })
                            }
                        })

                        child(verticalSpace(4))

                        //child(inventoryNameLabel(menu.playerInventory.name))

                        //child(verticalSpace(1))

                        // プレイヤーインベントリ
                        child(Containers.verticalFlow(Sizing.content(), Sizing.content()).apply {
                            surface(Surface.tiled(SlotType.NORMAL.viewTexture.id, 18, 18))
                            repeat(3) { r ->
                                child(Containers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
                                    repeat(9) { c ->
                                        child(slotContainer(slotAsComponent(9 * r + c), type = null))
                                    }
                                })
                            }
                        })
                        child(verticalSpace(4))
                        child(Containers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
                            surface(Surface.tiled(SlotType.NORMAL.viewTexture.id, 18, 18))
                            repeat(9) { c ->
                                child(slotContainer(slotAsComponent(9 * 3 + c), type = null))
                            }
                        })

                        // GUIタイトルはレシピMOD用の保護領域に持ってくる
                        child(verticalSpace(5))
                        child(inventoryNameLabel(title).horizontalTextAlignment(HorizontalAlignment.CENTER))
                        child(verticalSpace(5))

                    })

                    // 右ペイン
                    child(Containers.verticalFlow(Sizing.expand(50), Sizing.fill()).apply {
                        padding(Insets.of(0, 20, 0, 0)) // レシピMOD用に下部を保護
                        gap(2)

                        // 右ペインボタン
                        child(Containers.horizontalFlow(Sizing.fill(), Sizing.content()).apply {
                            gap(2)

                            // 収集効果の有効化切り替えボタン
                            child(LayeredImageToggleButton(14, 14, BUTTON_14_BACKGROUND, run {
                                WidgetSprites(
                                    MirageFairy2024.identifier("button_14/collection_enabled_foreground"),
                                    MirageFairy2024.identifier("button_14/collection_disabled_foreground"),
                                    MirageFairy2024.identifier("button_14/collection_enabled_foreground"),
                                    MirageFairy2024.identifier("button_14/collection_disabled_foreground"),
                                )
                            }).apply {
                                val player = Minecraft.getInstance().player ?: return@apply
                                fun update() {
                                    value.value = player.collectionEnabled.getOrDefault()
                                    tooltip(text { (if (value.value) COLLECTION_ENABLED_TRANSLATION() else COLLECTION_DISABLED_TRANSLATION()) + " ("() + toggleCollectionEnabledKeyMappingCard() + ")"() })
                                }
                                AttachmentChangedEvent.eventRegistry.observe(onClose) { identifier ->
                                    if (identifier == COLLECTION_ENABLED_ATTACHMENT_TYPE.identifier()) {
                                        update()
                                    }
                                }
                                update()
                                value.register { _, _ ->
                                    if (player.collectionEnabled.getOrDefault() != value.value) {
                                        SetCollectionEnabledChannel.sendToServer(value.value)
                                    }
                                }
                            })

                        })

                        // 効果欄
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
                                    }

                                    onUpdateListeners += {
                                        refresh()
                                    }
                                    refresh()
                                }
                            })
                        })

                    })

        }
    }

    override fun renderLabels(context: GuiGraphics, mouseX: Int, mouseY: Int) = Unit

    private val onUpdateListeners = mutableListOf<() -> Unit>()
    private var lastUpdateEventNanoTime = 0L
    override fun containerTick() {
        super.containerTick()

        val now = System.nanoTime()
        if (now - lastUpdateEventNanoTime >= 100_000_000) {
            lastUpdateEventNanoTime = now
            onUpdateListeners.forEach {
                it()
            }
        }
    }

    // キー入力で閉じる
    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (soulStreamKeyMappingCard.keyMapping.matches(keyCode, scanCode)) {
            onClose()
            return true
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    val onClose = EventRegistry<() -> Unit>()
    override fun onClose() {
        super.onClose()
        onClose.fire()
    }

}
*/
    }

}
