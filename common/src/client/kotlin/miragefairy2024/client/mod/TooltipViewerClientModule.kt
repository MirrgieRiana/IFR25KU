package miragefairy2024.client.mod

import com.mojang.blaze3d.platform.InputConstants
import miragefairy2024.ModContext
import miragefairy2024.client.mod.recipeviewer.vanilla.ViewScreen
import miragefairy2024.client.util.KeyMappingCard
import miragefairy2024.mod.OPEN_TOOLTIP_VIEWER_KEY_TRANSLATION
import miragefairy2024.mod.TOOLTIP_VIEWER_KEY_TRANSLATION
import miragefairy2024.mod.TooltipViewerViewWrapper
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.util.ObservableValue
import miragefairy2024.util.get
import miragefairy2024.util.invoke
import miragefairy2024.util.text
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics

val tooltipViewerKeyMappingCard = KeyMappingCard(
    OPEN_TOOLTIP_VIEWER_KEY_TRANSLATION.keyGetter(),
    InputConstants.UNKNOWN.value,
    KeyMapping.CATEGORY_INVENTORY,
) {
    Minecraft.getInstance().setScreen(TooltipViewerScreen())
}

context(ModContext)
fun initTooltipViewerClientModule() {
    tooltipViewerKeyMappingCard.init()
}

class TooltipViewerScreen : ViewScreen(text { TOOLTIP_VIEWER_KEY_TRANSLATION() }) {

    private val filter = ObservableValue("")

    override fun createRootView(): View {
        val player = minecraft!!.player
        val itemStacks = if (player != null) (0 until 9).map { player.inventory[it] } else emptyList()
        return TooltipViewerViewWrapper(itemStacks, filter) { getTooltipFromItem(minecraft!!, it) }.view
    }

    override fun renderBackground(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        renderTransparentBackground(guiGraphics)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (super.keyPressed(keyCode, scanCode, modifiers)) return true
        if (minecraft!!.options.keyInventory.matches(keyCode, scanCode)) {
            onClose()
            return true
        }
        if (tooltipViewerKeyMappingCard.keyMapping.matches(keyCode, scanCode)) {
            onClose()
            return true
        }
        return false
    }

}
