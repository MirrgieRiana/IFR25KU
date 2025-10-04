package miragefairy2024.mod.machine

import miragefairy2024.MirageFairy2024
import miragefairy2024.util.EMPTY_ITEM_STACK
import miragefairy2024.util.IngredientStack
import miragefairy2024.util.createItemStack
import miragefairy2024.util.isIn
import miragefairy2024.util.isNotEmpty
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

object FermentationBarrelRecipeCard : SimpleMachineRecipeCard<FermentationBarrelRecipe>() {
    override val identifier = MirageFairy2024.identifier("fermentation_barrel")
    override fun getIcon() = FermentationBarrelCard.item().createItemStack()
    override val recipeClass = FermentationBarrelRecipe::class.java
    override fun createRecipe(group: String, inputs: List<IngredientStack>, output: ItemStack, duration: Int): FermentationBarrelRecipe {
        return FermentationBarrelRecipe(this, group, inputs, output, duration)
    }
}

class FermentationBarrelRecipe(
    card: FermentationBarrelRecipeCard,
    group: String,
    inputs: List<IngredientStack>,
    output: ItemStack,
    duration: Int,
) : SimpleMachineRecipe(
    card,
    group,
    inputs,
    output,
    duration,
) {
    override fun getCustomizedRemainder(itemStack: ItemStack): ItemStack {
        val remainder = super.getCustomizedRemainder(itemStack)
        if (remainder.isNotEmpty) return remainder

        if (itemStack isIn Items.POTION) return Items.GLASS_BOTTLE.createItemStack()

        return EMPTY_ITEM_STACK
    }
}
