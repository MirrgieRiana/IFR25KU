package miragefairy2024.client.mod.fairy

import com.mojang.blaze3d.platform.InputConstants
import com.mojang.blaze3d.systems.RenderSystem
import kotlin.math.sin
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.client.mixins.api.RenderingEvent
import miragefairy2024.client.mod.recipeviewer.ViewRenderer
import miragefairy2024.client.mod.recipeviewer.vanilla.AbstractViewContainerScreen
import miragefairy2024.client.mod.recipeviewer.vanilla.VANILLA_VIEW_PLACER_REGISTRY
import miragefairy2024.client.mod.recipeviewer.vanilla.VanillaViewRendererWidget
import miragefairy2024.client.mod.recipeviewer.vanilla.place
import miragefairy2024.client.util.KeyMappingCard
import miragefairy2024.client.util.LayeredImageButton
import miragefairy2024.client.util.LayeredImageToggleButton
import miragefairy2024.client.util.WidgetSprites
import miragefairy2024.client.util.invoke
import miragefairy2024.client.util.registerHandledScreen
import miragefairy2024.client.util.sendToServer
import miragefairy2024.mod.AttachmentChangedEvent
import miragefairy2024.mod.common.SlotType
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
import miragefairy2024.mod.fairy.SoulStream
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
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.evaluateActualSize
import miragefairy2024.mod.recipeviewer.view.plus
import miragefairy2024.mod.recipeviewer.view.register
import miragefairy2024.mod.recipeviewer.view.sized
import miragefairy2024.mod.recipeviewer.views.AlignView
import miragefairy2024.mod.recipeviewer.views.DynamicView
import miragefairy2024.mod.recipeviewer.views.FilledRectangleView
import miragefairy2024.mod.recipeviewer.views.FixedWidgetView
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
import mirrg.kotlin.helium.Slot
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents
import net.fabricmc.fabric.api.client.screen.v1.Screens
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.ImageButton
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.gui.components.WidgetSprites
import net.minecraft.client.gui.screens.inventory.InventoryScreen
import net.minecraft.client.renderer.RenderType
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Inventory
import org.lwjgl.glfw.GLFW

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

private const val PANE_MARGIN = 4
private const val PANE_GAP = 4
private const val RECIPE_VIEWER_MARGIN = 20

val BUTTON_14_BACKGROUND = WidgetSprites(
    MirageFairy2024.identifier("button_14/background_on"),
    MirageFairy2024.identifier("button_14/background"),
    MirageFairy2024.identifier("button_14/background_on_focused"),
    MirageFairy2024.identifier("button_14/background_focused"),
)

context(_: ModContext)
fun initSoulStreamClientModule() {

    VANILLA_VIEW_PLACER_REGISTRY.register { context, view: SoulStreamButtonView, bounds ->
        view.button.setPosition(bounds.x, bounds.y)
        context place view.button
    }
    VANILLA_VIEW_PLACER_REGISTRY.register { context, view: SoulStreamFilterView, bounds ->
        context place VanillaViewRendererWidget(SoulStreamFilterViewRenderer, view, bounds)
    }

    soulStreamScreenHandlerType.registerHandledScreen(::SoulStreamScreen)

    // ソウルストリームのキーバインド
    soulStreamKeyMappingCard.init()

    // インベントリ画面にソウルストリームのボタンを設置
    ScreenEvents.AFTER_INIT.register { _, screen, _, _ ->
        if (screen is InventoryScreen) {

            // ボタン
            val buttonWidgetSprites = WidgetSprites(MirageFairy2024.identifier("soul_stream_button"), MirageFairy2024.identifier("soul_stream_button_highlighted"))
            val button: ImageButton = ImageButton(0, 0, 20, 20, buttonWidgetSprites) {
                lastMousePositionInInventory = Pair(Minecraft.getInstance().mouseHandler.xpos(), Minecraft.getInstance().mouseHandler.ypos())
                screen.onClose()
                OpenSoulStreamChannel.sendToServer(Unit)
            }.also {
                it.tooltip = Tooltip.create(text { OPEN_SOUL_STREAM_KEY_TRANSLATION() + "("() + soulStreamKeyMappingCard() + ")"() })
            }
            Screens.getButtons(screen).add(button)

            fun updatePosition() {
                val offsetX = if (screen.recipeBookComponent.isVisible) 130 else 53
                button.setPosition(screen.width / 2 + offsetX, screen.height / 2 - 23)
            }
            updatePosition()

            ScreenMouseEvents.afterMouseClick(screen).register { _, _, _, _ ->
                updatePosition()
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

class SoulStreamScreen(handler: SoulStreamScreenHandler, playerInventory: Inventory, title: Component) : AbstractViewContainerScreen<SoulStreamScreenHandler>(handler, playerInventory, title) {

    private var isFirstRender = true
    private val onViewClosed = EventRegistry<() -> Unit>()
    private var filterScrollView: YScrollContainerView? = null
    private var storageScrollView: YScrollContainerView? = null
    private var effectScrollView: YScrollContainerView? = null
    private var refreshEffects: (() -> Unit)? = null
    private var lastUpdateEventNanoTime = 0L
    private var panes = emptyList<View>()

    override fun init() {
        onViewClosed.fire()
        super.init()
        filterScrollView?.clampScrollOffset()
        storageScrollView?.clampScrollOffset()
        effectScrollView?.clampScrollOffset()
    }

    override fun removed() {
        onViewClosed.fire()
        refreshEffects = null
        panes = emptyList()
        super.removed()
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        if (isFirstRender) {
            isFirstRender = false
            val mousePosition = lastMousePositionInInventory
            if (mousePosition != null) {
                InputConstants.grabOrReleaseMouse(minecraft!!.window.window, InputConstants.CURSOR_NORMAL, mousePosition.first, mousePosition.second)
            }
        }
        super.render(guiGraphics, mouseX, mouseY, partialTick)
    }

    override fun getRootViewOffset() = IntPoint.ZERO

    // レシピビューワーが使うバニラのコンテナー矩形を画面全体に広げないのだ～🌱
    override fun setMenuSize(size: IntPoint) = Unit

    override fun hasClickedOutside(mouseX: Double, mouseY: Double, guiLeft: Int, guiTop: Int, mouseButton: Int): Boolean {
        if (!super.hasClickedOutside(mouseX, mouseY, guiLeft, guiTop, mouseButton)) return false
        // バニラはコンテナー矩形の外でスロットをクリックすると、スロット番号を投棄用の番号で上書きするのだ～🌱
        return menu.slots.none { slot ->
            slot.isActive && `mirageFairy2024$isHovering`(slot, mouseX, mouseY, isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY))
        }
    }

    internal fun getRecipeViewerExclusionAreas(): List<IntRectangle> {
        var x = PANE_MARGIN
        return panes.map { pane ->
            val size = pane.evaluateActualSize()
            val bounds = IntRectangle(x, PANE_MARGIN, size.x, size.y)
            x += size.x + PANE_GAP
            bounds
        }
    }

    override fun createRootView(): View {
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

                    val clearButton = LayeredImageButton(14, 14, BUTTON_14_BACKGROUND, WidgetSprites(MirageFairy2024.identifier("button_14/clear_foreground"))).also {
                        it.tooltip = Tooltip.create(text { SOUL_STREAM_RESET_HIGHLIGHTS_TRANSLATION() })
                        it.onClick.register { enabledPassiveSkillEffectFilters.value = mapOf() }
                    }
                    view += SoulStreamButtonView(clearButton)
                    view += XSpaceView(2)
                    val globalSearchButton = LayeredImageToggleButton(14, 14, BUTTON_14_BACKGROUND, WidgetSprites(MirageFairy2024.identifier("button_14/global_search_foreground"))).also {
                        it.tooltip = Tooltip.create(text { SOUL_STREAM_GLOBAL_SEARCH_TRANSLATION() })
                        enableGlobalFairyHighlight.observeAndInitialize(onViewClosed) { _, enabled -> it.value.value = enabled }
                        it.value.register { _, enabled -> enableGlobalFairyHighlight.value = enabled }
                    }
                    view += SoulStreamButtonView(globalSearchButton)

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

                val player = minecraft?.player
                if (player != null) {
                    val collectionButton = LayeredImageToggleButton(14, 14, BUTTON_14_BACKGROUND, WidgetSprites(
                        MirageFairy2024.identifier("button_14/collection_enabled_foreground"),
                        MirageFairy2024.identifier("button_14/collection_disabled_foreground"),
                        MirageFairy2024.identifier("button_14/collection_enabled_foreground"),
                        MirageFairy2024.identifier("button_14/collection_disabled_foreground"),
                    ))
                    fun updateCollectionButton() {
                        collectionButton.value.value = player.collectionEnabled.getOrDefault()
                        collectionButton.tooltip = Tooltip.create(text { (if (collectionButton.value.value) COLLECTION_ENABLED_TRANSLATION() else COLLECTION_DISABLED_TRANSLATION()) + " ("() + toggleCollectionEnabledKeyMappingCard() + ")"() })
                    }
                    AttachmentChangedEvent.eventRegistry.observe(onViewClosed) { identifier ->
                        if (identifier == COLLECTION_ENABLED_ATTACHMENT_TYPE.identifier()) updateCollectionButton()
                    }
                    updateCollectionButton()
                    collectionButton.value.register { _, enabled ->
                        if (player.collectionEnabled.getOrDefault() != enabled) SetCollectionEnabledChannel.sendToServer(enabled)
                    }
                    view += SoulStreamButtonView(collectionButton)
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
        val player = minecraft?.player ?: return emptyList()
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

    override fun containerTick() {
        super.containerTick()

        val now = System.nanoTime()
        if (now - lastUpdateEventNanoTime >= 100_000_000) {
            lastUpdateEventNanoTime = now
            refreshEffects?.invoke()
        }
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (soulStreamKeyMappingCard.keyMapping.matches(keyCode, scanCode)) {
            onClose()
            return true
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

}

private class SoulStreamButtonView(val button: AbstractWidget) : FixedWidgetView(IntPoint(button.width, button.height))

private class SoulStreamFilterView(val onClick: () -> Unit) : WrapperView(), PlaceableView {

    override val sizingX = Sizing.FILL

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): ViewController {
        return super.attachTo(offset, viewPlacer) + viewPlacer.place(this, offset.sized(evaluateActualSize()))
    }

}

private object SoulStreamFilterViewRenderer : ViewRenderer<SoulStreamFilterView, Unit> {

    override fun createState(view: SoulStreamFilterView) = Unit

    override fun isFocusable(view: SoulStreamFilterView, state: Slot<Unit>) = true

    override fun mouseClicked(view: SoulStreamFilterView, state: Slot<Unit>, bounds: IntRectangle, mouseX: Int, mouseY: Int, button: Int): Boolean {
        view.onClick()
        return true
    }

    override fun render(view: SoulStreamFilterView, state: Slot<Unit>, bounds: IntRectangle, graphics: GuiGraphics, mouseX: Int, mouseY: Int, isHovered: Boolean, delta: Float) {
        if (isHovered) graphics.fill(RenderType.guiOverlay(), bounds.x, bounds.y, bounds.x + bounds.sizeX, bounds.y + bounds.sizeY, 0x80FFFFFF.toInt())
    }

}
