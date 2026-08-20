package miragefairy2024.mod.mantle

import miragefairy2024.ModContext

context(ModContext)
fun initMantleModule() {
    initMantleToolLevel()
    initMantleMaterialCards()
    initMantleBlockCards()
    initMantleDimension()
    FairyQuestGateFeatureCard.init()
}
