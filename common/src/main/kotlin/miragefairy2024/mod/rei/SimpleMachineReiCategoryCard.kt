package miragefairy2024.mod.rei

import com.google.gson.JsonObject
import com.mojang.serialization.JsonOps
import me.shedaniel.rei.api.common.display.basic.BasicDisplay
import me.shedaniel.rei.api.common.entry.EntryIngredient
import miragefairy2024.mod.machine.AuraReflectorFurnaceCard
import miragefairy2024.mod.machine.AuraReflectorFurnaceRecipe
import miragefairy2024.mod.machine.AuraReflectorFurnaceRecipeCard
import miragefairy2024.mod.machine.FermentationBarrelCard
import miragefairy2024.mod.machine.FermentationBarrelRecipe
import miragefairy2024.mod.machine.FermentationBarrelRecipeCard
import miragefairy2024.mod.machine.SimpleMachineRecipe
import miragefairy2024.mod.machine.SimpleMachineRecipeCard
import miragefairy2024.util.createItemStack
import miragefairy2024.util.get
import miragefairy2024.util.string
import miragefairy2024.util.toEntryIngredient
import miragefairy2024.util.toEntryStack
import miragefairy2024.util.wrapper
import mirrg.kotlin.gson.hydrogen.toJsonElement
import mirrg.kotlin.helium.Single
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.ItemStack

abstract class SimpleMachineReiCategoryCard<R : SimpleMachineRecipe>(path: String, enName: String, jaName: String) : ReiCategoryCard<SimpleMachineReiCategoryCard.Display<R>>(path, enName, jaName) {
    override val serializer: Single<BasicDisplay.Serializer<Display<R>>> by lazy {
        Single(BasicDisplay.Serializer.ofRecipeLess({ _, _, tag ->
            val json = tag.wrapper["json"].string.get()!!
            Display(this, recipeCard.serializer.codec().codec().parse(JsonOps.INSTANCE, json.toJsonElement() as JsonObject).orThrow)
        }, { display, tag ->
            val jsonObject = recipeCard.serializer.codec().codec().encodeStart(JsonOps.INSTANCE, display.recipe)
            tag.wrapper["json"].string.set(jsonObject.toString())
        }))
    }

    abstract val recipeCard: SimpleMachineRecipeCard<R>
    abstract fun getMachine(): ItemStack

    open fun getInputIndices(recipe: R): List<Int> = recipe.inputs.indices.toList()

    open fun getInputs(recipe: R): List<EntryIngredient> {
        return recipe.inputs.map { input ->
            input.ingredient.items.map { it.copyWithCount(input.count).toEntryStack() }.toEntryIngredient()
        }
    }

    class Display<R : SimpleMachineRecipe>(private val card: SimpleMachineReiCategoryCard<R>, val recipe: R) : BasicDisplay(
        card.getInputs(recipe),
        listOf(
            recipe.output.toEntryIngredient(),
        ),
    ) {
        override fun getCategoryIdentifier() = card.identifier.first
    }
}

object FermentationBarrelReiCategoryCard : SimpleMachineReiCategoryCard<FermentationBarrelRecipe>("fermentation_barrel", "Fermentation Barrel", "醸造樽") {
    override val recipeCard = FermentationBarrelRecipeCard
    override fun getMachine() = FermentationBarrelCard.item().createItemStack()
}

object AuraReflectorFurnaceReiCategoryCard : SimpleMachineReiCategoryCard<AuraReflectorFurnaceRecipe>("aura_reflector_furnace", "Aura Reflector Furnace", "オーラ反射炉") {
    override val recipeCard = AuraReflectorFurnaceRecipeCard
    override fun getMachine() = AuraReflectorFurnaceCard.item().createItemStack()

    fun getFuelInputIndices(recipe: AuraReflectorFurnaceRecipe) = listOf(recipe.inputs.size)

    override fun getInputs(recipe: AuraReflectorFurnaceRecipe): List<EntryIngredient> {
        return super.getInputs(recipe) + listOf(AuraReflectorFurnaceRecipe.FUELS.map {
            BasicDisplay.registryAccess()[Registries.ITEM, it.key].value().createItemStack().toEntryStack()
        }.toEntryIngredient())
    }
}
