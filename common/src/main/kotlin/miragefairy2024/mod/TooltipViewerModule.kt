package miragefairy2024.mod

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa

val TOOLTIP_VIEWER_KEY_TRANSLATION = Translation({ "container.${MirageFairy2024.MOD_ID}.tooltip_viewer" }, "Tooltip Viewer", "ツールチップビューワー")
val TOOLTIP_VIEWER_EXAMPLE_KEY_TRANSLATION = Translation({ "gui.${MirageFairy2024.MOD_ID}.tooltip_viewer.example" }, "Example: %s", "例: %s")
val OPEN_TOOLTIP_VIEWER_KEY_TRANSLATION = Translation({ "key.${MirageFairy2024.MOD_ID}.open_tooltip_viewer" }, "Open Tooltip Viewer", "ツールチップビューワーを開く")

context(_: ModContext)
fun initTooltipViewerModule() {
    TOOLTIP_VIEWER_KEY_TRANSLATION.enJa()
    TOOLTIP_VIEWER_EXAMPLE_KEY_TRANSLATION.enJa()
    OPEN_TOOLTIP_VIEWER_KEY_TRANSLATION.enJa()
}
