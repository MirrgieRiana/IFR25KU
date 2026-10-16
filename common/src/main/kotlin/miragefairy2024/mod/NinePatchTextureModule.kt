package miragefairy2024.mod

import miragefairy2024.MirageFairy2024
import miragefairy2024.util.times

class NinePatchTextureCard(path: String) {
    companion object {
        val FAIRY_QUEST_CARD_MESSAGE = NinePatchTextureCard("fairy_quest_card_message")
        val TRAIT_BACKGROUND = NinePatchTextureCard("trait_background")
        val IFR_ENCYCLOPEDIA_BACKGROUND = NinePatchTextureCard("ifr_encyclopedia_background")
        val PANEL = NinePatchTextureCard("panel")
    }

    val identifier = MirageFairy2024.identifier(path)
    val texture = "textures/gui/" * identifier * ".png"
}
