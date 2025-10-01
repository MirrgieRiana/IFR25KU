package miragefairy2024.mod.recipeviewer

import miragefairy2024.ModContext
import net.minecraft.resources.ResourceLocation

abstract class RecipeViewerCategoryCard<R> {

    abstract fun getId(): ResourceLocation

    context(ModContext)
    open fun init() {
        RecipeViewerEvents.recipeViewerCategoryCards += this
    }

}
