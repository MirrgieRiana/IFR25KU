package miragefairy2024.mod.fairybuilding

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.tree.HAIMEVISKA_LOGS_BLOCK_TAG
import miragefairy2024.util.EnJa
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.generator
import miragefairy2024.util.registerChild
import miragefairy2024.util.toBlockTag
import net.minecraft.resources.ResourceLocation

enum class FairyBuildingModelCard(val identifier: ResourceLocation) {
    LANTERN(MirageFairy2024.identifier("block/fairy_building/lantern")),
    LANTERN_OFF(MirageFairy2024.identifier("block/fairy_building/lantern_off")),
}

val fairyBuildingCards: List<FairyBuildingCard<*, *, *>> = listOf(
    FairyHouseCard,
    FairyCollectorCard,
)

private val identifier = MirageFairy2024.identifier("fairy_building")
val FOLIA_TRANSLATION = Translation({ "gui.${identifier.toLanguageKey()}.folia" }, "Folia", "フォリア")
val SPECIFIED_FAIRY_SLOT_TRANSLATION = Translation({ "gui.${identifier.toLanguageKey()}.specified_fairy_slot" }, "Only %s Family", "%s系統のみ")

val FAIRY_BUILDING_BLOCK_TAG = MirageFairy2024.identifier("fairy_building").toBlockTag()

context(ModContext)
fun initFairyBuildingModule() {
    fairyBuildingCards.forEach { card ->
        card.init()
    }

    FOLIA_TRANSLATION.enJa()
    SPECIFIED_FAIRY_SLOT_TRANSLATION.enJa()

    FAIRY_BUILDING_BLOCK_TAG.enJa(EnJa("Fairy Building", "妖精の建物"))
    HAIMEVISKA_LOGS_BLOCK_TAG.generator.registerChild(FAIRY_BUILDING_BLOCK_TAG)
}
