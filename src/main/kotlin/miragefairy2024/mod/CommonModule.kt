package miragefairy2024.mod

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.ItemGroupCard
import miragefairy2024.util.createItemStack

val mirageFairy2024ItemGroupCard = ItemGroupCard(
    MirageFairy2024.identifier("miragefairy2024"), "MF24KU", "MF24KU",
) { MaterialCard.PHANTOM_DROP.item.createItemStack() }

context(ModContext)
fun initCommonModule() {
    mirageFairy2024ItemGroupCard.init()
}
