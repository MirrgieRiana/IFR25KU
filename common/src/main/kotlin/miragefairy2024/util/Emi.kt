package miragefairy2024.util

import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.EmiStack
import net.minecraft.world.item.ItemStack

fun ItemStack.toEmiStack(): EmiStack = EmiStack.of(this)

fun ItemStack.toEmiIngredient(): EmiIngredient = EmiIngredient.of(this.toIngredient())
fun IngredientStack.toEmiIngredient(): EmiIngredient = EmiIngredient.of(this.ingredient, this.count.toLong())
