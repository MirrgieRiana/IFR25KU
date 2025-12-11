package miragefairy2024.mod.machine

import com.mojang.serialization.Codec
import miragefairy2024.ModContext
import miragefairy2024.mod.recipeviewer.RecipeViewerCategoryCard
import miragefairy2024.mod.recipeviewer.RecipeViewerCategoryCardRecipeManagerBridge
import miragefairy2024.mod.recipeviewer.RecipeViewerEvents
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.ViewTexture
import miragefairy2024.util.createItemStack
import miragefairy2024.util.plusAssign
import miragefairy2024.util.toIngredientStack
import net.minecraft.core.RegistryAccess

abstract class SimpleMachineRecipeViewerCategoryCard<R : SimpleMachineRecipe> : RecipeViewerCategoryCard<R>() {
    override fun getIcon() = getRecipeCard().getIcon()
    override fun getWorkstations() = listOf(getMachineCard().item().createItemStack())
    override fun getRecipeCodec(registryAccess: RegistryAccess): Codec<R> = getRecipeCard().serializer.codec().codec()
    override fun getInputs(recipeEntry: RecipeEntry<R>) = recipeEntry.recipe.inputs.map { input -> Input(input.ingredient.toIngredientStack(input.count), false) }
    override fun getOutputs(recipeEntry: RecipeEntry<R>) = recipeEntry.recipe.outputs
    abstract fun getRecipeCard(): SimpleMachineRecipeCard<R>
    abstract fun getMachineCard(): SimpleMachineCard<*, *, *, R>

    protected fun getTexture(bounds: IntRectangle) = ViewTexture(getMachineCard().backgroundTexture, getMachineCard().backgroundTextureSize, bounds)

    context(ModContext)
    override fun init() {
        super.init()
        RecipeViewerEvents.recipeViewerCategoryCardRecipeManagerBridges += RecipeViewerCategoryCardRecipeManagerBridge(getRecipeCard().recipeClass, getRecipeCard().type, this)
    }
}
