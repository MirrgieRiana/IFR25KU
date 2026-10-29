package miragefairy2024.client.mod.fairy

import com.mojang.blaze3d.platform.InputConstants
import com.mojang.blaze3d.systems.RenderSystem
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.client.mixins.api.RenderingEvent
import miragefairy2024.client.mod.recipeviewer.vanilla.ViewContainerScreen
import miragefairy2024.client.util.KeyMappingCard
import miragefairy2024.client.util.invoke
import miragefairy2024.client.util.sendToServer
import miragefairy2024.mod.fairy.FairyItem
import miragefairy2024.mod.fairy.OPEN_SOUL_STREAM_KEY_TRANSLATION
import miragefairy2024.mod.fairy.OpenSoulStreamChannel
import miragefairy2024.mod.fairy.SoulStreamScreenHandler
import miragefairy2024.mod.fairy.enableGlobalFairyHighlight
import miragefairy2024.mod.fairy.enabledPassiveSkillEffectFilters
import miragefairy2024.mod.fairy.getFairyMotif
import miragefairy2024.mod.passiveskill.PassiveSkillEffectFilter
import miragefairy2024.util.invoke
import miragefairy2024.util.plus
import miragefairy2024.util.register
import miragefairy2024.util.text
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents
import net.fabricmc.fabric.api.client.screen.v1.Screens
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.ImageButton
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.gui.components.WidgetSprites
import net.minecraft.client.gui.screens.inventory.InventoryScreen
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

private val FILTER_OVERLAY_TEXTURE = MirageFairy2024.identifier("textures/gui/sprites/filter_overlay.png")

context(ModContext)
fun initSoulStreamClientModule() {
    soulStreamKeyMappingCard.init()

    ScreenEvents.AFTER_INIT.register { _, screen, _, _ ->
        if (screen is InventoryScreen) {
            val buttonWidgetSprites = WidgetSprites(MirageFairy2024.identifier("soul_stream_button"), MirageFairy2024.identifier("soul_stream_button_highlighted"))
            val button = ImageButton(0, 0, 20, 20, buttonWidgetSprites) {
                lastMousePositionInInventory = Pair(Minecraft.getInstance().mouseHandler.xpos(), Minecraft.getInstance().mouseHandler.ypos())
                screen.onClose()
                OpenSoulStreamChannel.sendToServer(Unit)
            }.also {
                it.tooltip = Tooltip.create(text { OPEN_SOUL_STREAM_KEY_TRANSLATION() + "("() + soulStreamKeyMappingCard() + ")"() })
            }
            fun updatePosition() {
                val panelWidth = if (screen.recipeBookComponent.isVisible) 300 else 146
                button.setPosition((screen.width + panelWidth) / 2 - 20, (screen.height - 46) / 2)
            }
            updatePosition()
            Screens.getButtons(screen).add(button)
            ScreenMouseEvents.afterMouseClick(screen).register { _, _, _, _ ->
                updatePosition()
            }
            ScreenEvents.beforeRender(screen).register { _, _, _, _, _ ->
                updatePosition()
            }
        }

        if (screen is ViewContainerScreen<*> && screen.menu is SoulStreamScreenHandler) {
            val mousePosition = lastMousePositionInInventory
            lastMousePositionInInventory = null
            var firstRender = true
            ScreenEvents.beforeRender(screen).register { _, _, _, _, _ ->
                if (firstRender) {
                    firstRender = false
                    if (mousePosition != null) {
                        InputConstants.grabOrReleaseMouse(Minecraft.getInstance().window.window, InputConstants.CURSOR_NORMAL, mousePosition.first, mousePosition.second)
                    }
                }
            }
        }
    }

    // 妖精検索
    RenderingEvent.RENDER_ITEM_DECORATIONS.register { graphics, font, stack, x, y, text ->
        val screen = Minecraft.getInstance().screen
        if (!(enableGlobalFairyHighlight.value || screen is ViewContainerScreen<*> && screen.menu is SoulStreamScreenHandler)) return@register
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

        graphics.pose().pushPose()
        graphics.pose().translate(0.0F, 0.0F, 200.0F)
        RenderSystem.enableBlend()
        graphics.setColor(1.0F, 1.0F, 1.0F, alpha)
        graphics.blit(FILTER_OVERLAY_TEXTURE, x, y, 0.0F, 0.0F, 16, 16, 16, 16)
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F)
        RenderSystem.disableBlend()
        graphics.pose().popPose()
    }

}
