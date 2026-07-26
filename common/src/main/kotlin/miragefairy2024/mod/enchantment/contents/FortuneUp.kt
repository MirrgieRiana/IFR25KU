package miragefairy2024.mod.enchantment.contents

import miragefairy2024.ModContext
import miragefairy2024.mod.enchantment.EnchantmentCard
import miragefairy2024.platformProxy
import mirrg.kotlin.java.hydrogen.orNull
import net.minecraft.world.item.enchantment.Enchantments

context(ModContext)
fun initFortuneUp() {
    platformProxy!!.registerModifyItemEnchantmentsHandler { _, mutableItemEnchantments, enchantmentLookup ->
        val fortuneEnchantment = enchantmentLookup[Enchantments.FORTUNE].orNull ?: return@registerModifyItemEnchantmentsHandler
        val fortuneLevel = mutableItemEnchantments.getLevel(fortuneEnchantment)
        if (fortuneLevel == 0) return@registerModifyItemEnchantmentsHandler
        val fortuneUpEnchantment = enchantmentLookup[EnchantmentCard.FORTUNE_UP.key].orNull ?: return@registerModifyItemEnchantmentsHandler
        val fortuneUpLevel = mutableItemEnchantments.getLevel(fortuneUpEnchantment)
        mutableItemEnchantments.set(fortuneEnchantment, fortuneLevel + fortuneUpLevel)
    }
}
