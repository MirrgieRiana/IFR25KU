package miragefairy2024.client

import dev.emi.emi.api.EmiRegistry
import dev.emi.emi.api.recipe.EmiInfoRecipe
import dev.emi.emi.api.stack.EmiIngredient
import miragefairy2024.mod.RecipeEvents

fun registerEmi(registry: EmiRegistry) {
    RecipeEvents.onRegisterInformationEntry.fire { listener ->
        listener {
            registry.addRecipe(EmiInfoRecipe(it.first.map { ingredient -> EmiIngredient.of(ingredient) }, it.second, it.third))
        }
    }
}
