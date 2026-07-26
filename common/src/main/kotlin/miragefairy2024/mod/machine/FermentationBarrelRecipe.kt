package miragefairy2024.mod.machine

import miragefairy2024.MirageFairy2024
import miragefairy2024.util.createItemStack
import net.minecraft.world.item.ItemStack

object FermentationBarrelRecipeCard : SimpleMachineRecipeCard<FermentationBarrelRecipe>() {
    override val identifier = MirageFairy2024.identifier("fermentation_barrel")
    override fun getIcon() = FermentationBarrelCard.item().createItemStack()
    override val recipeClass = FermentationBarrelRecipe::class.java
    override fun createRecipe(group: String, inputs: List<SimpleMachineRecipe.Input>, outputs: List<ItemStack>, duration: Int): FermentationBarrelRecipe {
        return FermentationBarrelRecipe(this, group, inputs, outputs, duration)
    }
}

class FermentationBarrelRecipe(
    card: FermentationBarrelRecipeCard,
    group: String,
    inputs: List<Input>,
    outputs: List<ItemStack>,
    duration: Int,
) : SimpleMachineRecipe(
    card,
    group,
    inputs,
    outputs,
    duration,
)
