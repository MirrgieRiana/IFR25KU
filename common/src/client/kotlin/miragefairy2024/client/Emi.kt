package miragefairy2024.client

import dev.emi.emi.api.EmiRegistry
import dev.emi.emi.api.recipe.EmiInfoRecipe
import dev.emi.emi.api.stack.EmiIngredient
import miragefairy2024.mod.RecipeEvents
import miragefairy2024.util.invoke
import miragefairy2024.util.plus
import miragefairy2024.util.text

fun registerEmi(registry: EmiRegistry) {
    RecipeEvents.informationEntries.forEach {
        registry.addRecipe(
            EmiInfoRecipe(
                listOf(EmiIngredient.of(it.input())),
                listOf(text { "== "() + it.title + " =="() }) + it.contents,
                it.id,
            )
        )
    }
}
