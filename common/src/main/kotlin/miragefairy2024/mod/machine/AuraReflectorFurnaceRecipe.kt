package miragefairy2024.mod.machine

import miragefairy2024.MirageFairy2024
import miragefairy2024.util.IngredientStack
import miragefairy2024.util.createItemStack
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

object AuraReflectorFurnaceRecipeCard : SimpleMachineRecipeCard<AuraReflectorFurnaceRecipe>() {
    override val identifier = MirageFairy2024.identifier("aura_reflector_furnace")
    override fun getIcon() = AuraReflectorFurnaceCard.item().createItemStack()
    override val recipeClass = AuraReflectorFurnaceRecipe::class.java
    override fun createRecipe(group: String, inputs: List<IngredientStack>, output: ItemStack, duration: Int): AuraReflectorFurnaceRecipe {
        return AuraReflectorFurnaceRecipe(this, group, inputs, output, duration)
    }
}

class AuraReflectorFurnaceRecipe(
    card: AuraReflectorFurnaceRecipeCard,
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
    companion object {
        val FUELS = mutableMapOf<ResourceKey<Item>, Int>()

        init {
            FUELS[BuiltInRegistries.ITEM.getResourceKey(Items.SOUL_SAND).get()] = 20 * 10
            FUELS[BuiltInRegistries.ITEM.getResourceKey(Items.SOUL_SOIL).get()] = 20 * 10
        }

        fun getFuelValue(item: Item) = FUELS[BuiltInRegistries.ITEM.getResourceKey(item).get()]
    }
}
