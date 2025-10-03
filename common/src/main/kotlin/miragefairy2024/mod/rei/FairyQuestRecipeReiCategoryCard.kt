package miragefairy2024.mod.rei

import me.shedaniel.rei.api.common.display.basic.BasicDisplay
import miragefairy2024.mod.fairyquest.FairyQuestCardCard
import miragefairy2024.mod.fairyquest.FairyQuestRecipe
import miragefairy2024.mod.fairyquest.fairyQuestRecipeRegistry
import miragefairy2024.mod.fairyquest.setFairyQuestRecipe
import miragefairy2024.util.createItemStack
import miragefairy2024.util.string
import miragefairy2024.util.toEntryIngredient
import miragefairy2024.util.toIdentifier
import mirrg.kotlin.helium.Single

object FairyQuestRecipeReiCategoryCard : ReiCategoryCard<FairyQuestRecipeReiCategoryCard.Display>("fairy_quest_recipe", "Fairy Quest", "フェアリークエスト") {
    override val serializer: Single<BasicDisplay.Serializer<Display>> by lazy {
        Single(BasicDisplay.Serializer.ofRecipeLess({ _, _, tag ->
            Display(fairyQuestRecipeRegistry.get(tag.getString("Id").toIdentifier())!!)
        }, { display, tag ->
            fairyQuestRecipeRegistry.getKey(display.recipe)?.let { tag.putString("Id", it.string) }
        }))
    }

    class Display(val recipe: FairyQuestRecipe) : BasicDisplay(
        listOf(
            FairyQuestCardCard.item().createItemStack().also { it.setFairyQuestRecipe(recipe) }.toEntryIngredient(),
            *recipe.inputs.map { input -> input().toEntryIngredient() }.toTypedArray(),
        ),
        recipe.outputs.map { it().toEntryIngredient() },
    ) {
        override fun getCategoryIdentifier() = identifier.first
    }
}
