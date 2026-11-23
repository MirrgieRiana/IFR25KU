package miragefairy2024.mod.structure

import miragefairy2024.ModContext
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa

val MAP_TRANSLATION = Translation({ "item.miragefairy2024.filled_map.template" }, "%s Map", "%sの地図")

context(_: ModContext)
fun initStructureModule() {
    MAP_TRANSLATION.enJa()

    initUnlimitedJigsaw()
    DripstoneCavesRuinCard.init()
    WeatheredAncientRemnantsCard.init()
}
