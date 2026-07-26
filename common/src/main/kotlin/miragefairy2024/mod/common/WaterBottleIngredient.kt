package miragefairy2024.mod.common

import com.mojang.serialization.MapCodec
import miragefairy2024.MirageFairy2024
import miragefairy2024.util.isIn
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer
import net.minecraft.core.component.DataComponents
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.PotionContents
import net.minecraft.world.item.alchemy.Potions

object WaterBottleIngredient : CustomIngredient {
    val ID = MirageFairy2024.identifier("water_bottle")
    val SERIALIZER = object : CustomIngredientSerializer<WaterBottleIngredient> {
        override fun getIdentifier() = ID
        override fun getCodec(allowEmpty: Boolean): MapCodec<WaterBottleIngredient> = MapCodec.unit(WaterBottleIngredient)
        override fun getPacketCodec(): StreamCodec<RegistryFriendlyByteBuf, WaterBottleIngredient> = StreamCodec.unit(WaterBottleIngredient)
    }

    override fun requiresTesting() = true

    override fun test(stack: ItemStack): Boolean {
        if (stack isIn Items.POTION) {
            val potionContents = stack.get(DataComponents.POTION_CONTENTS) ?: return false
            if (potionContents isIn Potions.WATER) {
                return true
            }
        }
        return false
    }

    override fun getMatchingStacks() = listOf(PotionContents.createItemStack(Items.POTION, Potions.WATER))
    override fun getSerializer() = SERIALIZER
}
