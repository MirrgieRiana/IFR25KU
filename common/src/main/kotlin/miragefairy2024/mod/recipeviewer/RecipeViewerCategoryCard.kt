package miragefairy2024.mod.recipeviewer

import miragefairy2024.ModContext
import miragefairy2024.util.EnJa
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.invoke
import miragefairy2024.util.text
import net.minecraft.resources.ResourceLocation

abstract class RecipeViewerCategoryCard<R> {

    abstract fun getId(): ResourceLocation

    abstract fun getName(): EnJa
    val translation = Translation({ "category.rei.${getId().toLanguageKey()}" }, getName().en, getName().ja)
    val displayName = text { translation() }

    context(ModContext)
    open fun init() {
        RecipeViewerEvents.recipeViewerCategoryCards += this
        translation.enJa()
    }

}
