package miragefairy2024.mod.tool

import miragefairy2024.ModContext
import miragefairy2024.mod.tool.effects.initToolEffectType
import miragefairy2024.mod.tool.items.initFairyToolItem

context(_: ModContext)
fun initToolModule() {
    initDamageType()
    initToolMaterial()
    initToolCard()
    initToolConfiguration()
    initToolEffectType()
    initFairyToolItem()
}
