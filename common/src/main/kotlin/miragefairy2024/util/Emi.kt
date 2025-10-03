package miragefairy2024.util

import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.EmiStack
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient

fun ItemStack.toEmiStack(): EmiStack = EmiStack.of(this)

fun Ingredient.toEmiIngredient(): EmiIngredient = EmiIngredient.of(this)
fun ItemStack.toEmiIngredient(): EmiIngredient = this.toIngredient().toEmiIngredient()
fun IngredientStack.toEmiIngredient(): EmiIngredient = EmiIngredient.of(this.ingredient, this.count.toLong())
