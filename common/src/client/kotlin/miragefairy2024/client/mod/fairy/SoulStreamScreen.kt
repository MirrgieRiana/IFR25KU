package miragefairy2024.client.mod.fairy

import com.mojang.blaze3d.platform.InputConstants
import com.mojang.blaze3d.systems.RenderSystem
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.client.mixins.api.RenderingEvent
import miragefairy2024.client.mod.recipeviewer.ViewRenderer
import miragefairy2024.client.mod.recipeviewer.ViewRendererRegistry
import miragefairy2024.client.mod.recipeviewer.vanilla.ViewContainerScreen
import miragefairy2024.client.util.KeyMappingCard
import miragefairy2024.client.util.WidgetSprites
import miragefairy2024.client.util.invoke
import miragefairy2024.client.util.registerHandledScreen
import miragefairy2024.client.util.sendToServer
import miragefairy2024.mod.fairy.FairyItem
import miragefairy2024.mod.fairy.OPEN_SOUL_STREAM_KEY_TRANSLATION
import miragefairy2024.mod.fairy.OpenSoulStreamChannel
import miragefairy2024.mod.fairy.SetCollectionEnabledChannel
import miragefairy2024.mod.fairy.SoulStreamFilterView
import miragefairy2024.mod.fairy.SoulStreamScreenHandler
import miragefairy2024.mod.fairy.getFairyMotif
import miragefairy2024.mod.fairy.soulStreamScreenHandlerType
import miragefairy2024.mod.passiveskill.PassiveSkillEffectFilter
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.util.ObservableValue
import miragefairy2024.util.invoke
import miragefairy2024.util.plus
import miragefairy2024.util.register
import miragefairy2024.util.text
import mirrg.kotlin.helium.Slot
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents
import net.fabricmc.fabric.api.client.screen.v1.Screens
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.ImageButton
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.gui.components.WidgetSprites
import net.minecraft.client.gui.screens.inventory.InventoryScreen
import net.minecraft.client.renderer.RenderType
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

context(_: ModContext)
fun initSoulStreamClientModule() {

    ViewRendererRegistry.register(SoulStreamFilterView::class.java, SoulStreamFilterViewRenderer)

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

class SoulStreamScreen(handler: SoulStreamScreenHandler, playerInventory: Inventory, title: Component) : ViewContainerScreen<SoulStreamScreenHandler>(handler, playerInventory, title) {

    private val viewWrapper = handler.initViewWrapper(enabledPassiveSkillEffectFilters, enableGlobalFairyHighlight, text { toggleCollectionEnabledKeyMappingCard() }) {
        SetCollectionEnabledChannel.sendToServer(it)
    }
    private var isFirstRender = true
    private var lastUpdateEventNanoTime = 0L

    override fun init() {
        viewWrapper.dispose()
        super.init()
        viewWrapper.clampScrollOffsets()
    }

    override fun removed() {
        viewWrapper.dispose()
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

    internal fun getRecipeViewerExclusionAreas() = viewWrapper.getRecipeViewerExclusionAreas()

    override fun containerTick() {
        super.containerTick()

        val now = System.nanoTime()
        if (now - lastUpdateEventNanoTime >= 100_000_000) {
            lastUpdateEventNanoTime = now
            viewWrapper.updateEffects()
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
