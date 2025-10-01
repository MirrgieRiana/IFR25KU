package miragefairy2024.mod.recipeviewer

import miragefairy2024.ModContext

abstract class RecipeViewerCategoryCard<R> {
    context(ModContext)
    open fun init() {
        RecipeViewerEvents.recipeViewerCategoryCards += this
    }
}
