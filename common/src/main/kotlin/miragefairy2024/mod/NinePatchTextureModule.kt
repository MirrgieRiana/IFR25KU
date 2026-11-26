package miragefairy2024.mod

import miragefairy2024.DataGenerationEvents
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.times

/**
 * @param repeatX 上下の辺と中央を、true なら X 軸方向に反復描画し、false なら伸長描画するのだ～🌱
 * @param repeatY 左右の辺と中央を、true なら Y 軸方向に反復描画し、false なら伸長描画するのだ～🌱
 */
class NinePatchTextureCard(
    path: String,
    val textureWidth: Int,
    val textureHeight: Int,
    val xStartSize: Int,
    val xMiddleSize: Int,
    val xEndSize: Int,
    val yStartSize: Int,
    val yMiddleSize: Int,
    val yEndSize: Int,
    val repeatX: Boolean,
    val repeatY: Boolean,
) {
    companion object {
        /** 枠の絵なのだ～🌱 */
        val PANEL = NinePatchTextureCard("panel", 16, 16, 5, 5, false)

        val FAIRY_QUEST_CARD_MESSAGE = NinePatchTextureCard("fairy_quest_card_message", 66, 66, 22, 22, true)
        val FAIRY_QUEST_CARD_LINE = NinePatchTextureCard("fairy_quest_card_line", 11, 11, 0, 11, 0, 0, 11, 0, true, true)
        val TRAIT_BACKGROUND = NinePatchTextureCard("trait_background", 66, 66, 22, 22, true)
        val IFR_ENCYCLOPEDIA_BACKGROUND = NinePatchTextureCard("ifr_encyclopedia_background", 66, 66, 22, 22, true)
        val INVENTORY_SCROLLBAR_BACKGROUND = NinePatchTextureCard("inventory_scrollbar_background", 16, 16, 1, 13, 1, 1, 13, 1, false, false)
        val INVENTORY_SCROLLBAR_SCROLLER = NinePatchTextureCard("inventory_scrollbar_scroller", 16, 16, 2, 11, 2, 2, 11, 2, false, true)
        val INVENTORY_SCROLLBAR_SCROLLER_DISABLED = NinePatchTextureCard("inventory_scrollbar_scroller_disabled", 16, 16, 2, 11, 2, 2, 11, 2, false, true)

        /** owo 向けに単一のパッチ寸法と反復設定を出力する Card なのだ～🌱 */
        val owoEntries = listOf(FAIRY_QUEST_CARD_MESSAGE, TRAIT_BACKGROUND, IFR_ENCYCLOPEDIA_BACKGROUND)
    }

    constructor(path: String, textureWidth: Int, textureHeight: Int, patchWidth: Int, patchHeight: Int, repeat: Boolean) : this(
        path, textureWidth, textureHeight,
        patchWidth, patchWidth, patchWidth,
        patchHeight, patchHeight, patchHeight,
        repeat, repeat,
    )

    val identifier = MirageFairy2024.identifier(path)
    val texture = "textures/gui/" * identifier * ".png"
}

context(_: ModContext)
fun initNinePatchTextureModule() {
    NinePatchTextureCard.owoEntries.forEach { card ->
        DataGenerationEvents.onGenerateNinePatchTexture {
            it(card.identifier, card)
        }
    }
}
