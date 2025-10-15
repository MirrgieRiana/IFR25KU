package miragefairy2024.mod.recipeviewer

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import miragefairy2024.ModContext
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.util.EnJa
import miragefairy2024.util.IngredientStack
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.invoke
import miragefairy2024.util.plusAssign
import miragefairy2024.util.text
import net.minecraft.core.RegistryAccess
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.ItemStack

abstract class RecipeViewerCategoryCard<R> {

    abstract fun getId(): ResourceLocation

    abstract fun getName(): EnJa
    val translation = Translation({ getId().toLanguageKey("category.rei") }, getName().en, getName().ja)
    val displayName = text { translation() }

    abstract fun getIcon(): ItemStack
    open fun getWorkstations(): List<ItemStack> = listOf()

    class RecipeEntry<R>(val id: ResourceLocation, val recipe: R, val isSynthetic: Boolean) {
        companion object {
            fun <R> getCodec(recipeCodec: Codec<R>): Codec<RecipeEntry<R>> = RecordCodecBuilder.create { instance ->
                instance.group(
                    ResourceLocation.CODEC.fieldOf("Id").forGetter { it.id },
                    recipeCodec.fieldOf("Recipe").forGetter { it.recipe },
                    Codec.BOOL.fieldOf("IsSynthetic").forGetter { it.isSynthetic },
                ).apply(instance, ::RecipeEntry)
            }
        }
    }

    abstract fun getRecipeCodec(registryAccess: RegistryAccess): Codec<R>
    fun getRecipeEntryCodec(registryAccess: RegistryAccess): Codec<RecipeEntry<R>> = RecipeEntry.getCodec(getRecipeCodec(registryAccess))

    class Input(val ingredientStack: IngredientStack, val isCatalyst: Boolean)

    open fun getInputs(recipeEntry: RecipeEntry<R>): List<Input> = listOf()
    open fun getOutputs(recipeEntry: RecipeEntry<R>): List<ItemStack> = listOf()

    open fun createRecipeEntries(): Iterable<RecipeEntry<R>> = listOf()

    open fun getScreenClickAreas(): List<Pair<ResourceKey<MenuType<*>>, IntRectangle>> = listOf()

    protected abstract fun createView(recipeEntry: RecipeEntry<R>): View
    fun getView(renderingProxy: RenderingProxy, recipeEntry: RecipeEntry<R>): View {
        val view = createView(recipeEntry)
        view.calculateActualSize(renderingProxy)
        return view
    }

    context(ModContext)
    open fun init() {
        RecipeViewerEvents.recipeViewerCategoryCards += this
        translation.enJa()
    }

}
