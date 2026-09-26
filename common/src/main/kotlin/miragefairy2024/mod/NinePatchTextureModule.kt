package miragefairy2024.mod

import miragefairy2024.DataGenerationEvents
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.times

class NinePatchTextureCard(
    path: String,
    val textureWidth: Int,
    val textureHeight: Int,
    val patchWidth: Int,
    val patchHeight: Int,
    val repeat: Boolean,
) {
    companion object {
        val FAIRY_QUEST_CARD_MESSAGE = NinePatchTextureCard("fairy_quest_card_message", 66, 66, 22, 22, true)
        val TRAIT_BACKGROUND = NinePatchTextureCard("trait_background", 66, 66, 22, 22, true)
        val IFR_ENCYCLOPEDIA_BACKGROUND = NinePatchTextureCard("ifr_encyclopedia_background", 66, 66, 22, 22, true)

        val entries = listOf(FAIRY_QUEST_CARD_MESSAGE, TRAIT_BACKGROUND, IFR_ENCYCLOPEDIA_BACKGROUND)
    }

    val identifier = MirageFairy2024.identifier(path)
    val texture = "textures/gui/" * identifier * ".png"
}

context(ModContext)
fun initNinePatchTextureModule() {
    NinePatchTextureCard.entries.forEach { card ->
        DataGenerationEvents.onGenerateNinePatchTexture {
            it(card.identifier, card)
        }
    }
}
