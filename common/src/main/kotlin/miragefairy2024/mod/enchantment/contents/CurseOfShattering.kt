package miragefairy2024.mod.enchantment.contents

import miragefairy2024.ModContext
import miragefairy2024.mixins.api.EquippedItemBrokenCallback
import miragefairy2024.mod.enchantment.EnchantmentCard
import miragefairy2024.mod.tool.ToolBreakDamageTypeCard
import miragefairy2024.util.get
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.enchantment.EnchantmentHelper

context(ModContext)
fun initCurseOfShattering() {
    EquippedItemBrokenCallback.EVENT.register { entity, _, slot ->
        if (entity.level().isClientSide) return@register
        val itemStack = entity.getItemBySlot(slot)
        itemStack.grow(1)
        val originalItemStack = itemStack.copy()
        itemStack.shrink(1)
        val enchantLevel = EnchantmentHelper.getItemEnchantmentLevel(entity.level().registryAccess()[Registries.ENCHANTMENT, EnchantmentCard.CURSE_OF_SHATTERING.key], originalItemStack)
        if (enchantLevel == 0) return@register
        entity.hurt(entity.level().damageSources().source(ToolBreakDamageTypeCard.registryKey), 2F * enchantLevel.toFloat())
    }
}
