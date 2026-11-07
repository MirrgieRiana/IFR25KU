package miragefairy2024.client.mod

import com.mojang.blaze3d.platform.InputConstants
import miragefairy2024.ModContext
import miragefairy2024.client.util.KeyMappingCard
import miragefairy2024.clientProxy
import miragefairy2024.mod.OPEN_TOOLTIP_VIEWER_KEY_TRANSLATION
import miragefairy2024.mod.TooltipViewerScreen
import net.minecraft.client.KeyMapping

val tooltipViewerKeyMappingCard = KeyMappingCard(
    OPEN_TOOLTIP_VIEWER_KEY_TRANSLATION.keyGetter(),
    InputConstants.UNKNOWN.value,
    KeyMapping.CATEGORY_INVENTORY,
) {
    clientProxy!!.openViewScreen(TooltipViewerScreen())
}

context(ModContext)
fun initTooltipViewerClientModule() {
    tooltipViewerKeyMappingCard.init()
}
