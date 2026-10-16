package miragefairy2024.client.mod.fairy

import com.mojang.blaze3d.platform.InputConstants
import com.mojang.blaze3d.systems.RenderSystem
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.client.mixins.api.RenderingEvent
import miragefairy2024.client.mod.recipeviewer.screen.LayeredImageButtonView
import miragefairy2024.client.mod.recipeviewer.screen.MENU_SLOT_BACKGROUND_SIZE
import miragefairy2024.client.mod.recipeviewer.screen.ViewContainerScreen
import miragefairy2024.client.mod.recipeviewer.screen.menuSlotView
import miragefairy2024.client.util.KeyMappingCard
import miragefairy2024.client.util.SlotType
import miragefairy2024.client.util.WidgetSprites
import miragefairy2024.client.util.invoke
import miragefairy2024.client.util.registerHandledScreen
import miragefairy2024.client.util.sendToServer
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
import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.ColorPair
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.views.FilledRectangleView
import miragefairy2024.mod.recipeviewer.views.ScrollView
import miragefairy2024.mod.recipeviewer.views.ScrollbarStyle
import miragefairy2024.mod.recipeviewer.views.StackView
import miragefairy2024.mod.recipeviewer.views.TextView
import miragefairy2024.mod.recipeviewer.views.TooltipBackgroundView
import miragefairy2024.mod.recipeviewer.views.View
import miragefairy2024.mod.recipeviewer.views.XListView
import miragefairy2024.mod.recipeviewer.views.XSpaceView
import miragefairy2024.mod.recipeviewer.views.YListView
import miragefairy2024.mod.recipeviewer.views.YSpaceView
import miragefairy2024.mod.recipeviewer.views.configure
import miragefairy2024.mod.recipeviewer.views.margin
import miragefairy2024.mod.recipeviewer.views.maxContentSizeX
import miragefairy2024.mod.recipeviewer.views.minContentSizeX
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
import miragefairy2024.util.size
import miragefairy2024.util.text
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.screen.v1.Screens
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.ImageButton
import net.minecraft.client.gui.components.Tooltip
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

private const val SCREEN_MARGIN = 4

/** 3個の欄の間隔なのだ～🌱 */
private const val PANE_GAP = 4

/** 左右の欄の中の間隔なのだ～🌱 */
private const val SIDE_PANE_GAP = 2

/** 左右の欄の下に残す、レシピビューワー向けの余白なのだ～🌱 */
private const val RECIPE_VIEWER_MARGIN = 20

/** 真ん中の欄の横幅は、持ち物の1行と、スクロールバーの分なのだ～🌱 */
private const val MAIN_WIDTH = MENU_SLOT_BACKGROUND_SIZE * 9 + MENU_SLOT_BACKGROUND_SIZE

private const val MAIN_SCROLLBAR_THICKNESS = MENU_SLOT_BACKGROUND_SIZE

private const val SIDE_SCROLLBAR_THICKNESS = 5

private const val BUTTON_SIZE = 14

private const val TOOLTIP_MARGIN = 4

/** 持ち物の名前の左右の余白なのだ～🌱 */
private const val INVENTORY_NAME_MARGIN_X = 1

/** 真ん中の欄の、持ち物の上下に空ける間隔なのだ～🌱 */
private const val INVENTORY_SPACE = 4

/** 真ん中の欄の、題名の上下に空ける間隔なのだ～🌱 */
private const val TITLE_SPACE = 5

private const val PLAYER_INVENTORY_WIDTH = 9

private const val PLAYER_INVENTORY_HEIGHT = 3

private const val BACKGROUND_ARGB = 0xFFC6C6C6.toInt()

private const val SIDE_SCROLLBAR_ARGB = 0xA0FFFFFF.toInt()

/** 妖精を探し当てたときに重ねる色なのだ～🌱 */
private const val FAIRY_HIGHLIGHT_ARGB = 0x88FFFF00.toInt()

/** 妖精を探し当てたときの点滅の周期なのだ～🌱 */
private const val FAIRY_HIGHLIGHT_PERIOD_NANOS = 2_000_000_000L

/** 探し当てたことを示す絵を、アイテムの絵より手前へ出す深さなのだ～🌱 */
private const val FAIRY_HIGHLIGHT_DEPTH = 200.0

/** 効果欄を作り直す間隔なのだ～🌱 */
private const val UPDATE_INTERVAL_NANOS = 100_000_000L

/** 持たせた特性の値を、効果の一覧へ並べるときの基準なのだ～🌱 */
private const val PASSIVE_SKILL_SAMPLE_MANA = 10.0

private const val INVENTORY_BUTTON_SIZE = 20

/**
 * インベントリ画面の中央から、ソウルストリームを開くボタンの右端までの距離なのだ～🌱
 * レシピ本が開いているときは、持ち物の枠が右へ寄るため、その分だけ遠いのだ～🌱
 */
private const val INVENTORY_BUTTON_OFFSET_X = 146 / 2
private const val INVENTORY_BUTTON_OFFSET_X_WITH_RECIPE_BOOK = 300 / 2

/** インベントリ画面の中央から、ソウルストリームを開くボタンの上端までの距離なのだ～🌱 */
private const val INVENTORY_BUTTON_OFFSET_Y = 46 / 2

context(ModContext)
fun initSoulStreamClientModule() {

    // GUI登録
    soulStreamScreenHandlerType.registerHandledScreen { gui, inventory, title -> SoulStreamScreen(gui, inventory, title) }

    // ソウルストリームのキーバインド
    soulStreamKeyMappingCard.init()

    // インベントリ画面にソウルストリームのボタンを設置
    ScreenEvents.AFTER_INIT.register { _, screen, _, _ ->
        if (screen is InventoryScreen) {

            // レシピ本の開閉で位置が変わるため、毎フレーム現在の位置を読むのだ～🌱
            val buttonSprites = WidgetSprites(MirageFairy2024.identifier("soul_stream_button"), MirageFairy2024.identifier("soul_stream_button_highlighted"))
            val button = object : ImageButton(0, 0, INVENTORY_BUTTON_SIZE, INVENTORY_BUTTON_SIZE, buttonSprites, {
                lastMousePositionInInventory = Pair(Minecraft.getInstance().mouseHandler.xpos(), Minecraft.getInstance().mouseHandler.ypos())
                screen.onClose()
                OpenSoulStreamChannel.sendToServer(Unit)
            }) {
                override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
                    val offsetX = if (screen.recipeBookComponent.isVisible) INVENTORY_BUTTON_OFFSET_X_WITH_RECIPE_BOOK else INVENTORY_BUTTON_OFFSET_X
                    setPosition(screen.width / 2 + offsetX - INVENTORY_BUTTON_SIZE, screen.height / 2 - INVENTORY_BUTTON_OFFSET_Y)
                    super.renderWidget(guiGraphics, mouseX, mouseY, partialTick)
                }
            }
            button.tooltip = Tooltip.create(text { OPEN_SOUL_STREAM_KEY_TRANSLATION() + "("() + soulStreamKeyMappingCard() + ")"() })
            Screens.getButtons(screen).add(button)

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
                    val value = (if (specification.effect == filter.effect) filter.effect.castOrNull(specification.valueProvider(PASSIVE_SKILL_SAMPLE_MANA)) else null) ?: return false
                    return filter.predicate(value)
                }
                f(filter)
            }
        }
        if (!matched) return@register

        val nanos = System.nanoTime()
        val phase = ((nanos % FAIRY_HIGHLIGHT_PERIOD_NANOS).toDouble() / FAIRY_HIGHLIGHT_PERIOD_NANOS.toDouble()) * (2.0 * Math.PI)
        val alpha = (0.75 + 0.25 * sin(phase)).toFloat()

        graphics.fill(x, y, x + 16, y + 16, FAIRY_HIGHLIGHT_ARGB)

        graphics.pose().pushPose()
        graphics.pose().translate(0.0, 0.0, FAIRY_HIGHLIGHT_DEPTH)
        RenderSystem.enableBlend()
        graphics.setColor(1f, 1f, 1f, alpha)
        graphics.blit(FILTER_OVERLAY_TEXTURE, x, y, 0F, 0F, 16, 16, 16, 16)
        graphics.setColor(1f, 1f, 1f, 1f)
        RenderSystem.disableBlend()
        graphics.pose().popPose()
    }

}

class SoulStreamScreen(handler: SoulStreamScreenHandler, playerInventory: Inventory, title: Component) : ViewContainerScreen<SoulStreamScreenHandler>(handler, playerInventory, title) {

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

    override fun createRootView(): View {
        passiveSkillTexts = collectPassiveSkillTexts()

        /** 持ち物の名前なのだ～🌱 */
        fun inventoryNameView(name: Component) = TextView(name).also {
            it.sizingX = Sizing.FILL
            it.alignmentX = Alignment.CENTER
            it.color = ColorPair.DARK_GRAY
            it.shadow = false
        }

        /** スロットを横に並べた1行なのだ～🌱 */
        fun slotRowView(slotType: SlotType, count: Int, slotIndexFunction: (Int) -> Int) = View {
            view += XListView().configure {
                repeat(count) { c ->
                    view += menuSlotView(menu.slots[slotIndexFunction(c)], slotType)
                }
            }
        }

        val rootView = View {
            view += StackView().configure {
                view.sizingX = Sizing.FILL
                view.sizingY = Sizing.FILL

                view += FilledRectangleView().also { it.color.value = BACKGROUND_ARGB }

                view += XListView().configure {
                    view.sizingX = Sizing.FILL
                    view.sizingY = Sizing.FILL

                    // 左の欄なのだ～🌱 真ん中の欄を除いた余りを、右の欄と半分ずつ分けるのだ～🌱
                    view += YListView().configure {
                        // 中身の幅を横の積み上げへ入れないことで、余りだけを分ける形になるのだ～🌱
                        position.ignoreLayoutX = true
                        position.weight = 1.0
                        view.sizingX = Sizing.FILL
                        view.sizingY = Sizing.FILL

                        view += XListView().configure {
                            view.sizingX = Sizing.FILL

                            // ハイライトを全部消すボタンなのだ～🌱
                            view += LayeredImageButtonView(IntPoint(BUTTON_SIZE, BUTTON_SIZE), BUTTON_14_BACKGROUND, WidgetSprites(MirageFairy2024.identifier("button_14/clear_foreground"))).also {
                                it.onPress.register {
                                    enabledPassiveSkillEffectFilters.value = mapOf()
                                }
                            }.configure { }.tooltip(text { SOUL_STREAM_RESET_HIGHLIGHTS_TRANSLATION() })

                            view += XSpaceView(SIDE_PANE_GAP)

                            // 持ち物の外の妖精も探すかを切り替えるボタンなのだ～🌱
                            view += LayeredImageButtonView(IntPoint(BUTTON_SIZE, BUTTON_SIZE), BUTTON_14_BACKGROUND, WidgetSprites(MirageFairy2024.identifier("button_14/global_search_foreground"))).also { buttonView ->
                                buttonView.toggle = true
                                enableGlobalFairyHighlight.observeAndInitialize(onClose) { _, _ ->
                                    buttonView.value.value = enableGlobalFairyHighlight.value
                                }
                                buttonView.value.register { _, newValue ->
                                    enableGlobalFairyHighlight.value = newValue
                                }
                            }.configure { }.tooltip(text { SOUL_STREAM_GLOBAL_SEARCH_TRANSLATION() })
                        }

                        view += YSpaceView(SIDE_PANE_GAP)

                        // 探す条件の一覧なのだ～🌱
                        // TODO 発動条件での絞り込みも並べるべきなのだ～🌱
                        // でも、満腹度は上下と数値で個別の項目が要って、doubleで持つものは識別子を作りにくいのだ～🌱
                        // 道具の系統は、該当する妖精が1体しかない条件がずらっと並ぶのだ～🌱
                        // だから、折り畳める形にしないと、並べても使いにくいのだ～🌱
                        view += StackView().configure {
                            position.weight = 1.0
                            view.sizingX = Sizing.FILL
                            view.sizingY = Sizing.FILL

                            view += TooltipBackgroundView()

                            view += ScrollView(SIDE_SCROLLBAR_THICKNESS, ScrollbarStyle.Flat(SIDE_SCROLLBAR_ARGB)).configure {
                                view += YListView().configure {
                                    view.sizingX = Sizing.FILL

                                    view += TextView(text { SOUL_STREAM_PASSIVE_SKILL_EFFECT_TRANSLATION().blue.bold }).also {
                                        it.sizingX = Sizing.FILL
                                        it.alignmentX = Alignment.CENTER
                                        it.shadow = false
                                    }

                                    passiveSkillEffectRegistry.entrySet().sortedBy { it.key.location() }.forEach { (_, effect) ->
                                        fun <T : Any> f(effect: PassiveSkillEffect<T>) {
                                            val samples = motifRegistry.entrySet()
                                                .flatMap { it.value.passiveSkillSpecifications }
                                                .mapNotNull { if (it.effect == effect) effect.castOrNull(it.valueProvider(PASSIVE_SKILL_SAMPLE_MANA)) else null }
                                            effect.getFilters(samples).sortedBy { it.identifier }.forEach { filter ->
                                                // 探す条件に選ばれている間は、金色で見せるのだ～🌱
                                                val textView = TextView(filter.text).also {
                                                    it.sizingX = Sizing.FILL
                                                    it.shadow = false
                                                }
                                                enabledPassiveSkillEffectFilters.observeAndInitialize(onClose) { _, _ ->
                                                    textView.text.value = (if (filter.identifier in enabledPassiveSkillEffectFilters.value) filter.text.gold else filter.text).visualOrderText
                                                }
                                                view += textView.configure { }.tooltip { _, _ ->
                                                    listOf(if (filter.identifier in enabledPassiveSkillEffectFilters.value) filter.text.gold else filter.text)
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
                                }.margin(0, SIDE_SCROLLBAR_THICKNESS, 0, 0)
                            }.margin(TOOLTIP_MARGIN)
                        }
                    }.margin(0, 0, 0, RECIPE_VIEWER_MARGIN)

                    view += XSpaceView(PANE_GAP)

                    // 真ん中の欄なのだ～🌱
                    view += YListView().configure {
                        view.sizingY = Sizing.FILL

                        // 妖精の枠のスロットを並べた1行なのだ～🌱
                        view += slotRowView(SlotType.FAIRY, PLAYER_INVENTORY_WIDTH) { c -> PLAYER_INVENTORY_WIDTH * (PLAYER_INVENTORY_HEIGHT + 1) + c }

                        view += YSpaceView(INVENTORY_SPACE)

                        // 妖精を預ける欄なのだ～🌱
                        view += ScrollView(MAIN_SCROLLBAR_THICKNESS, ScrollbarStyle.Vanilla).configure {
                            position.weight = 1.0

                            view += YListView().configure {
                                (PLAYER_INVENTORY_WIDTH until menu.soulStream.size).chunked(PLAYER_INVENTORY_WIDTH).forEach { indices ->
                                    view += View {
                                        view += XListView().configure {
                                            indices.forEach { index ->
                                                view += menuSlotView(menu.slots[PLAYER_INVENTORY_WIDTH * (PLAYER_INVENTORY_HEIGHT + 1) + index], SlotType.NORMAL)
                                            }
                                        }
                                    }
                                }
                            }.margin(0, MAIN_SCROLLBAR_THICKNESS, 0, 0)
                        }

                        view += YSpaceView(INVENTORY_SPACE)

                        // プレイヤーの持ち物なのだ～🌱
                        repeat(PLAYER_INVENTORY_HEIGHT) { r ->
                            view += slotRowView(SlotType.NORMAL, PLAYER_INVENTORY_WIDTH) { c -> PLAYER_INVENTORY_WIDTH * r + c }
                        }

                        view += YSpaceView(INVENTORY_SPACE)

                        view += slotRowView(SlotType.NORMAL, PLAYER_INVENTORY_WIDTH) { c -> PLAYER_INVENTORY_WIDTH * PLAYER_INVENTORY_HEIGHT + c }

                        // 題名は、レシピビューワー向けに空けた余白の中へ置くのだ～🌱
                        view += YSpaceView(TITLE_SPACE)

                        view += inventoryNameView(title).configure { position.ignoreLayoutX = true }.margin(INVENTORY_NAME_MARGIN_X, 0)

                        view += YSpaceView(TITLE_SPACE)
                    }.minContentSizeX(MAIN_WIDTH).maxContentSizeX(MAIN_WIDTH)

                    view += XSpaceView(PANE_GAP)

                    // 右の欄なのだ～🌱
                    view += YListView().configure {
                        position.ignoreLayoutX = true
                        position.weight = 1.0
                        view.sizingX = Sizing.FILL
                        view.sizingY = Sizing.FILL

                        view += XListView().configure {
                            view.sizingX = Sizing.FILL

                            // 妖精の収集効果を切り替えるボタンなのだ～🌱
                            view += LayeredImageButtonView(
                                IntPoint(BUTTON_SIZE, BUTTON_SIZE),
                                BUTTON_14_BACKGROUND,
                                WidgetSprites(
                                    MirageFairy2024.identifier("button_14/collection_enabled_foreground"),
                                    MirageFairy2024.identifier("button_14/collection_disabled_foreground"),
                                    MirageFairy2024.identifier("button_14/collection_enabled_foreground"),
                                    MirageFairy2024.identifier("button_14/collection_disabled_foreground"),
                                ),
                            ).also { buttonView ->
                                buttonView.toggle = true
                                val player = Minecraft.getInstance().player
                                if (player != null) {
                                    fun update() {
                                        buttonView.value.value = player.collectionEnabled.getOrDefault()
                                    }
                                    AttachmentChangedEvent.eventRegistry.observe(onClose) { identifier ->
                                        if (identifier == COLLECTION_ENABLED_ATTACHMENT_TYPE.identifier()) {
                                            update()
                                        }
                                    }
                                    update()
                                    buttonView.value.register { _, newValue ->
                                        if (player.collectionEnabled.getOrDefault() != newValue) {
                                            SetCollectionEnabledChannel.sendToServer(newValue)
                                        }
                                    }
                                }
                            }.configure { }.tooltip { _, _ ->
                                val enabled = Minecraft.getInstance().player?.collectionEnabled?.getOrDefault() ?: false
                                listOf(text { (if (enabled) COLLECTION_ENABLED_TRANSLATION() else COLLECTION_DISABLED_TRANSLATION()) + " ("() + toggleCollectionEnabledKeyMappingCard() + ")"() })
                            }
                        }

                        view += YSpaceView(SIDE_PANE_GAP)

                        // 今効いている効果の一覧なのだ～🌱
                        view += StackView().configure {
                            position.weight = 1.0
                            view.sizingX = Sizing.FILL
                            view.sizingY = Sizing.FILL

                            view += TooltipBackgroundView()

                            // スクロールバーは中身へ重ねるのだ～🌱
                            view += ScrollView(SIDE_SCROLLBAR_THICKNESS, ScrollbarStyle.Flat(SIDE_SCROLLBAR_ARGB)).configure {
                                view += YListView().configure {
                                    view.sizingX = Sizing.FILL

                                    if (passiveSkillTexts.isEmpty()) {
                                        view += TextView(text { SOUL_STREAM_NO_PASSIVE_SKILL_EFFECTS_TRANSLATION().red }).also {
                                            it.sizingX = Sizing.FILL
                                            it.shadow = false
                                        }
                                    } else {
                                        passiveSkillTexts.forEach { effectText ->
                                            view += TextView(effectText.gold).also {
                                                it.sizingX = Sizing.FILL
                                                it.shadow = false
                                            }.configure { }.tooltip(effectText.gold)
                                        }
                                    }
                                }.margin(0, SIDE_SCROLLBAR_THICKNESS, 0, 0)
                            }.margin(TOOLTIP_MARGIN)
                        }
                    }.margin(0, 0, 0, RECIPE_VIEWER_MARGIN)
                }.margin(SCREEN_MARGIN)
            }
        }

        return rootView
    }

    /** 効果欄に並べる文なのだ～🌱 中身が変わったときだけ、Viewのツリーを作り直すのだ～🌱 */
    private var passiveSkillTexts = listOf<Component>()

    private fun collectPassiveSkillTexts(): List<Component> {
        val player = Minecraft.getInstance().player ?: return listOf()
        val passiveSkillProviders = player.findPassiveSkillProviders()
        val result = PassiveSkillResult()
        result.collect(passiveSkillProviders.passiveSkills, player, ManaBoostPassiveSkillEffect.Value(mapOf()), true)
        val manaBoostValue = result[ManaBoostPassiveSkillEffect]
        result.collect(passiveSkillProviders.passiveSkills, player, manaBoostValue, false)
        return result.map.keys.sortedBy { passiveSkillEffectRegistry.getKey(it) }.flatMap { effect ->
            fun <T : Any> f(effect: PassiveSkillEffect<T>) = effect.getTexts(result[effect])
            f(effect)
        }
    }

    private var lastUpdateEventNanoTime = 0L
    override fun containerTick() {
        super.containerTick()

        val now = System.nanoTime()
        if (now - lastUpdateEventNanoTime >= UPDATE_INTERVAL_NANOS) {
            lastUpdateEventNanoTime = now
            if (collectPassiveSkillTexts() != passiveSkillTexts) rebuildWidgets()
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
