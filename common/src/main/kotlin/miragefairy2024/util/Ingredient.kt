package miragefairy2024.util

import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient

fun Item.toIngredient(): Ingredient = Ingredient.of(this)
fun ItemStack.toIngredient(): Ingredient = Ingredient.of(this)
fun TagKey<Item>.toIngredient(): Ingredient = Ingredient.of(this)
fun Iterable<Item>.toIngredient(): Ingredient = Ingredient.of(*this.toList().toTypedArray())

context(ModContext)
fun CustomIngredientSerializer<*>.register() {
    ModEvents.onInitialize {
        CustomIngredientSerializer.register(this)
    }
}
