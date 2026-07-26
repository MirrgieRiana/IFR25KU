package miragefairy2024.mod.enchantment.contents

import miragefairy2024.ModContext
import miragefairy2024.mixins.api.BlockCallback
import miragefairy2024.mod.enchantment.EnchantmentCard
import miragefairy2024.util.get
import mirrg.kotlin.java.hydrogen.orNull
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.item.crafting.SingleRecipeInput
import net.minecraft.world.item.enchantment.EnchantmentHelper

context(ModContext)
fun initSmelting() {
    BlockCallback.GET_DROPS_BY_ENTITY.register { state, level, _, _, _, tool, drops ->
        val smeltingLevel = EnchantmentHelper.getItemEnchantmentLevel(level.registryAccess()[Registries.ENCHANTMENT, EnchantmentCard.SMELTING.key], tool)
        if (smeltingLevel == 0) return@register drops
        if (!tool.isCorrectToolForDrops(state)) return@register drops
        drops.map {
            val recipe = level.recipeManager.getRecipeFor(RecipeType.SMELTING, SingleRecipeInput(it), level).orNull ?: return@map it
            val result = recipe.value.getResultItem(level.registryAccess())
            if (result.isEmpty) return@map it
            result.copyWithCount(it.count)
        }
    }
}
