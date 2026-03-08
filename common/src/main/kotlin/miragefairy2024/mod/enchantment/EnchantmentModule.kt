package miragefairy2024.mod.enchantment

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.enchantment.contents.initCurseOfShattering
import miragefairy2024.mod.enchantment.contents.initFortuneUp
import miragefairy2024.mod.enchantment.contents.initMultiMine
import miragefairy2024.mod.enchantment.contents.initSmelting
import miragefairy2024.mod.enchantment.contents.initStickyMining
import miragefairy2024.util.EnJa
import miragefairy2024.util.enJa
import miragefairy2024.util.generator
import miragefairy2024.util.registerChild
import miragefairy2024.util.toItemTag
import net.minecraft.tags.ItemTags

val MAGIC_WEAPON_ITEM_TAG = MirageFairy2024.identifier("magic_weapon").toItemTag()
val SCYTHE_ITEM_TAG = MirageFairy2024.identifier("scythe").toItemTag()
val BUILDERS_ROD_ITEM_TAG = MirageFairy2024.identifier("builders_rod").toItemTag()
val NONE_ITEM_TAG = MirageFairy2024.identifier("none").toItemTag()
val AREA_MINING_ENCHANTABLE_ITEM_TAG = MirageFairy2024.identifier("enchantable/area_mining").toItemTag()

context(ModContext)
fun initEnchantmentModule() {

    EnchantmentCard.entries.forEach { card ->
        card.init()
    }

    initFortuneUp()
    initSmelting()
    initStickyMining()
    initMultiMine()
    initCurseOfShattering()

    MAGIC_WEAPON_ITEM_TAG.enJa(EnJa("Magic Weapon", "魔法武器"))
    SCYTHE_ITEM_TAG.enJa(EnJa("Scythe", "大鎌"))
    BUILDERS_ROD_ITEM_TAG.enJa(EnJa("Builder's Rod", "ビルダーズロッド"))
    NONE_ITEM_TAG.enJa(EnJa("None", "なし"))
    AREA_MINING_ENCHANTABLE_ITEM_TAG.enJa(EnJa("Area Mining Enchantable", "範囲採掘エンチャント可能"))

    ItemTags.SWORDS.generator.registerChild(SCYTHE_ITEM_TAG)
    ItemTags.MINING_LOOT_ENCHANTABLE.generator.registerChild(SCYTHE_ITEM_TAG)

    ItemTags.DURABILITY_ENCHANTABLE.generator.registerChild(BUILDERS_ROD_ITEM_TAG)
    AREA_MINING_ENCHANTABLE_ITEM_TAG.generator.registerChild(BUILDERS_ROD_ITEM_TAG)
    ItemTags.MINING_ENCHANTABLE.generator.registerChild(BUILDERS_ROD_ITEM_TAG) // これをしないと金床で側方範囲採掘が付かない

}
