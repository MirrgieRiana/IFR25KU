package miragefairy2024.util

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient

data class IngredientStack(val ingredient: Ingredient, val count: Int) {
    companion object {
        val CODEC: Codec<IngredientStack> = RecordCodecBuilder.create { instance ->
            instance.group(
                Ingredient.CODEC.fieldOf("Ingredient").forGetter { it.ingredient },
                Codec.INT.fieldOf("Amount").forGetter { it.count },
            ).apply(instance, ::IngredientStack)
        }
        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, IngredientStack> = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC,
            { it.ingredient },
            ByteBufCodecs.VAR_INT,
            { it.count },
            ::IngredientStack,
        )

        val EMPTY = Ingredient.EMPTY.toIngredientStack(0)
    }
}

fun IngredientStack(ingredient: Ingredient) = IngredientStack(ingredient, 1)
fun IngredientStack(itemStack: ItemStack, amount: Int = 1) = IngredientStack(itemStack.toIngredient(), amount)

fun Ingredient.toIngredientStack(amount: Int = 1) = IngredientStack(this, amount)
fun ItemStack.toIngredientStack(amount: Int = 1) = IngredientStack(this, amount)
fun Item.toIngredientStack(amount: Int = 1) = this.defaultInstance.toIngredientStack(amount)
fun TagKey<Item>.toIngredientStack(amount: Int = 1) = IngredientStack(this.toIngredient(), amount)

@JvmName("toIngredientStackFromItems")
fun Iterable<Item>.toIngredientStack(amount: Int = 1) = this.toIngredient().toIngredientStack(amount)

@JvmName("toIngredientStackFromItemStacks")
fun Iterable<ItemStack>.toIngredientStack(amount: Int = 1) = this.toIngredient().toIngredientStack(amount)

fun IngredientStack.toItemStacks(): List<ItemStack> = ingredient.items.map { it.copyWithCount(count) }
