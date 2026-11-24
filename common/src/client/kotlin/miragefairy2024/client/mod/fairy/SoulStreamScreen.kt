package miragefairy2024.client.mod.fairy

import com.mojang.blaze3d.platform.InputConstants
import com.mojang.blaze3d.systems.RenderSystem
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.client.mixins.api.RenderingEvent
import miragefairy2024.client.mod.recipeviewer.vanilla.ViewContainerScreen
import miragefairy2024.client.util.KeyMappingCard
import miragefairy2024.client.util.invoke
import miragefairy2024.client.util.registerHandledScreen
import miragefairy2024.client.util.sendToServer
import miragefairy2024.mod.fairy.FairyItem
import miragefairy2024.mod.fairy.OPEN_SOUL_STREAM_KEY_TRANSLATION
import miragefairy2024.mod.fairy.OpenSoulStreamChannel
import miragefairy2024.mod.fairy.SetCollectionEnabledChannel
import miragefairy2024.mod.fairy.SoulStreamScreenContent
import miragefairy2024.mod.fairy.SoulStreamScreenHandler
import miragefairy2024.mod.fairy.getFairyMotif
import miragefairy2024.mod.fairy.soulStreamScreenHandlerType
import miragefairy2024.mod.passiveskill.PassiveSkillEffectFilter
import miragefairy2024.util.ObservableValue
import miragefairy2024.util.invoke
import miragefairy2024.util.plus
import miragefairy2024.util.text
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

    // GUI 登録
    soulStreamScreenHandlerType.registerHandledScreen { gui, inventory, title -> SoulStreamScreen(gui, inventory, title) }

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

    private val screenContent = SoulStreamScreenContent(handler, enabledPassiveSkillEffectFilters, enableGlobalFairyHighlight, text { toggleCollectionEnabledKeyMappingCard() }) {
        SetCollectionEnabledChannel.sendToServer(it)
    }

    override fun createRootView() = screenContent.createView(title)

    // カーソルをインベントリ画面での位置に戻す
    private var isFirst = true
    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        if (isFirst) {
            isFirst = false
            val mousePosition = lastMousePositionInInventory
            if (mousePosition != null) {
                InputConstants.grabOrReleaseMouse(minecraft!!.window.window, InputConstants.CURSOR_NORMAL, mousePosition.first, mousePosition.second)
            }
        }
        super.render(guiGraphics, mouseX, mouseY, partialTick)
    }

    // キー入力で閉じる
    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (soulStreamKeyMappingCard.keyMapping.matches(keyCode, scanCode)) {
            onClose()
            return true
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun removed() {
        screenContent.dispose()
        super.removed()
    }

}
