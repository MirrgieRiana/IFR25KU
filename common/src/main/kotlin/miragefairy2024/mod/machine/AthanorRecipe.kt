package miragefairy2024.mod.machine

import miragefairy2024.MirageFairy2024
import miragefairy2024.util.IngredientStack
import miragefairy2024.util.createItemStack
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

object AthanorRecipeCard : SimpleMachineRecipeCard<AthanorRecipe>() {
    override val identifier = MirageFairy2024.identifier("athanor")
    override fun getIcon() = Items.FURNACE.createItemStack() // TODO AthanorCard.item().createItemStack()
    override val recipeClass = AthanorRecipe::class.java
    override fun createRecipe(group: String, inputs: List<IngredientStack>, outputs: List<ItemStack>, duration: Int): AthanorRecipe {
        return AthanorRecipe(this, group, inputs, outputs, duration)
    }
}

class AthanorRecipe(
    card: AthanorRecipeCard,
    group: String,
    inputs: List<IngredientStack>,
    outputs: List<ItemStack>,
    duration: Int,
) : SimpleMachineRecipe(
    card,
    group,
    inputs,
    outputs,
    duration,
)
