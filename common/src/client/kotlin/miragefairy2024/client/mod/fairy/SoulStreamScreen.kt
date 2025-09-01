package miragefairy2024.client.mod.fairy

import com.mojang.blaze3d.platform.InputConstants
import com.mojang.blaze3d.systems.RenderSystem
import io.wispforest.owo.ui.base.BaseOwoHandledScreen
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.container.ScrollContainer
import io.wispforest.owo.ui.core.Color
import io.wispforest.owo.ui.core.HorizontalAlignment
import io.wispforest.owo.ui.core.Insets
import io.wispforest.owo.ui.core.OwoUIAdapter
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.Surface
import io.wispforest.owo.ui.core.VerticalAlignment
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.client.mixins.api.RenderingEvent
import miragefairy2024.client.util.ClickableContainer
import miragefairy2024.client.util.KeyMappingCard
import miragefairy2024.client.util.LayeredImageButton
import miragefairy2024.client.util.LayeredImageToggleButton
import miragefairy2024.client.util.SlotType
import miragefairy2024.client.util.WidgetSprites
import miragefairy2024.client.util.inventoryNameLabel
import miragefairy2024.client.util.registerHandledScreen
import miragefairy2024.client.util.sendToServer
import miragefairy2024.client.util.slotContainer
import miragefairy2024.client.util.tooltipContainer
import miragefairy2024.client.util.verticalScroll
import miragefairy2024.client.util.verticalSpace
import miragefairy2024.mod.AttachmentChangedEvent
import miragefairy2024.mod.fairy.COLLECTION_DISABLED_TRANSLATION
import miragefairy2024.mod.fairy.COLLECTION_ENABLED_ATTACHMENT_TYPE
import miragefairy2024.mod.fairy.COLLECTION_ENABLED_TRANSLATION
import miragefairy2024.mod.fairy.FairyItem
import miragefairy2024.mod.fairy.OPEN_SOUL_STREAM_KEY_TRANSLATION
import miragefairy2024.mod.fairy.OpenSoulStreamChannel
import miragefairy2024.mod.fairy.SOUL_STREAM_GLOBAL_SEARCH_TRANSLATION
import miragefairy2024.mod.fairy.SOUL_STREAM_NO_PASSIVE_SKILL_EFFECTS_TRANSLATION
import miragefairy2024.mod.fairy.SOUL_STREAM_PASSIVE_SKILL_EFFECT_TRANSLATION
import miragefairy2024.mod.fairy.SOUL_STREAM_RESET_HIGHLIGHTS_TRANSLATION
import miragefairy2024.mod.fairy.SetCollectionEnabledChannel
import miragefairy2024.mod.fairy.SoulStreamScreenHandler
import miragefairy2024.mod.fairy.collectionEnabled
import miragefairy2024.mod.fairy.getFairyMotif
import miragefairy2024.mod.fairy.motifRegistry
import miragefairy2024.mod.fairy.soulStreamScreenHandlerType
import miragefairy2024.mod.passiveskill.PassiveSkillEffect
import miragefairy2024.mod.passiveskill.PassiveSkillEffectFilter
import miragefairy2024.mod.passiveskill.PassiveSkillResult
import miragefairy2024.mod.passiveskill.collect
import miragefairy2024.mod.passiveskill.effects.ManaBoostPassiveSkillEffect
import miragefairy2024.mod.passiveskill.findPassiveSkillProviders
import miragefairy2024.mod.passiveskill.passiveSkillEffectRegistry
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
import miragefairy2024.util.size
import miragefairy2024.util.text
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.ImageButton
import net.minecraft.client.gui.components.WidgetSprites
import net.minecraft.client.gui.screens.inventory.InventoryScreen
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Inventory
import org.lwjgl.glfw.GLFW
import kotlin.math.sin

val soulStreamKeyMappingCard = KeyMappingCard(
    OPEN_SOUL_STREAM_KEY_TRANSLATION.keyGetter(),
    GLFW.GLFW_KEY_K,
    KeyMapping.CATEGORY_INVENTORY,
) {
    lastMousePositionInInventory = null
    OpenSoulStreamChannel.sendToServer(Unit)
}

var lastMousePositionInInventory: Pair<Double, Double>? = null

var enabledPassiveSkillEffectFilters = ObservableValue<Map<ResourceLocation, PassiveSkillEffectFilter<*>>>(mapOf())
var enableGlobalFairyHighlight = ObservableValue(false)

private val FILTER_OVERLAY_TEXTURE = MirageFairy2024.identifier("textures/gui/sprites/filter_overlay.png")

val BUTTON_14_BACKGROUND = WidgetSprites(
    MirageFairy2024.identifier("button_14/background_on"),
    MirageFairy2024.identifier("button_14/background"),
    MirageFairy2024.identifier("button_14/background_on_focused"),
    MirageFairy2024.identifier("button_14/background_focused"),
)

context(ModContext)
fun initSoulStreamClientModule() {

    // GUI登録
    soulStreamScreenHandlerType.registerHandledScreen { gui, inventory, title -> SoulStreamScreen(gui, inventory, title) }

    // ソウルストリームのキーバインド
    soulStreamKeyMappingCard.init()

    // インベントリ画面にソウルストリームのボタンを設置
    ScreenEvents.AFTER_INIT.register { _, screen, _, _ ->
        if (screen is InventoryScreen) {

            val onMouseClick = mutableListOf<() -> Unit>()

            // 中央揃えコンテナ
            val uiAdapter = OwoUIAdapter.create(screen, Containers::stack)
            uiAdapter.rootComponent.apply {
                alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)

                // 位置決定用パネル
                child(Containers.stack(Sizing.content(), Sizing.content()).apply {
                    alignment(HorizontalAlignment.RIGHT, VerticalAlignment.TOP)

                    fun updatePosition() {
                        if (!screen.recipeBookComponent.isVisible) {
                            sizing(Sizing.fixed(146), Sizing.fixed(46))
                        } else {
                            sizing(Sizing.fixed(300), Sizing.fixed(46))
                        }
                    }
                    updatePosition()
                    onMouseClick += {
                        updatePosition()
                        uiAdapter.inflateAndMount()
                    }

                    // ボタン
                    val buttonWidgetSprites = WidgetSprites(MirageFairy2024.identifier("soul_stream_button"), MirageFairy2024.identifier("soul_stream_button_highlighted"))
                    child(ImageButton(0, 0, 20, 20, buttonWidgetSprites) {
                        lastMousePositionInInventory = Pair(Minecraft.getInstance().mouseHandler.xpos(), Minecraft.getInstance().mouseHandler.ypos())
                        screen.onClose()
                        OpenSoulStreamChannel.sendToServer(Unit)
                    }.apply {
                        tooltip(text { OPEN_SOUL_STREAM_KEY_TRANSLATION() + "("() + Component.keybind(OPEN_SOUL_STREAM_KEY_TRANSLATION.keyGetter()) + ")"() })
                    })

                })

            }
            uiAdapter.inflateAndMount()

            ScreenMouseEvents.afterMouseClick(screen).register { _, _, _, _ ->
                onMouseClick.forEach {
                    it()
                }
            }

        }
    }

    // 妖精検索
    RenderingEvent.RENDER_ITEM_DECORATIONS.register { graphics, font, stack, x, y, text ->
        val screen = Minecraft.getInstance().screen
        if (!(enableGlobalFairyHighlight.value || screen is SoulStreamScreen)) return@register
        val effectFilters = enabledPassiveSkillEffectFilters.value.values
        if (effectFilters.isEmpty()) return@register
        if (stack.item !is FairyItem) return@register
        val motif = stack.getFairyMotif() ?: return@register
        val matched = motif.passiveSkillSpecifications.any { specification ->
            effectFilters.any { filter ->
                fun <T : Any> f(filter: PassiveSkillEffectFilter<T>): Boolean {
                    val value = (if (specification.effect == filter.effect) filter.effect.castOrNull(specification.valueProvider(10.0)) else null) ?: return false
                    return filter.predicate(value)
                }
                f(filter)
            }
        }
        if (!matched) return@register

        val nanos = System.nanoTime()
        val phase = ((nanos % 2_000_000_000L).toDouble() / 2_000_000_000.0) * (2.0 * Math.PI)
        val alpha = (0.75 + 0.25 * sin(phase)).toFloat()

        graphics.fill(x, y, x + 16, y + 16, 0x88FFFF00.toInt())

        graphics.push()
        graphics.translate(0.0, 0.0, 200.0)
        RenderSystem.enableBlend()
        graphics.setColor(1f, 1f, 1f, alpha)
        graphics.blit(FILTER_OVERLAY_TEXTURE, x, y, 0F, 0F, 16, 16, 16, 16)
        graphics.setColor(1f, 1f, 1f, 1f)
        RenderSystem.disableBlend()
        graphics.pop()
    }

}

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
                    surface(Surface.tiled(SlotType.FAIRY.texture, 18, 18))
                    repeat(9) { index ->
                        child(slotContainer(slotAsComponent(9 * 3 + 9 + index), type = null))
                    }
                })

                child(verticalSpace(4))

                child(verticalScroll(Sizing.fill(100), Sizing.expand(), 18).apply {
                    surface(Surface.tiled(SlotType.NORMAL.texture, 18, 18))
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
                    surface(Surface.tiled(SlotType.NORMAL.texture, 18, 18))
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
                    surface(Surface.tiled(SlotType.NORMAL.texture, 18, 18))
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
                        fun update() {
                            value.value = Minecraft.getInstance().player?.collectionEnabled?.getOrDefault() ?: true
                            tooltip(text { if (value.value) COLLECTION_ENABLED_TRANSLATION() else COLLECTION_DISABLED_TRANSLATION() })
                        }
                        AttachmentChangedEvent.eventRegistry.observe(onClose) { identifier ->
                            if (identifier == COLLECTION_ENABLED_ATTACHMENT_TYPE.identifier()) {
                                update()
                            }
                        }
                        update()
                        value.register { _, _ ->
                            SetCollectionEnabledChannel.sendToServer(value.value)
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
