package miragefairy2024.mod.recipeviewer

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import miragefairy2024.ModContext
import miragefairy2024.util.EnJa
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.invoke
import miragefairy2024.util.plusAssign
import miragefairy2024.util.text
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient

abstract class RecipeViewerCategoryCard<R> {

    abstract fun getId(): ResourceLocation

    abstract fun getName(): EnJa
    val translation = Translation({ getId().toLanguageKey("category.rei") }, getName().en, getName().ja)
    val displayName = text { translation() }

    abstract fun getIcon(): ItemStack
    abstract fun getWorkstations(): List<ItemStack>

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

        var viewCache: View? = null
    }

    abstract fun getRecipeCodec(): Codec<R>
    val recipeEntryCodec: Codec<RecipeEntry<R>> = RecipeEntry.getCodec(getRecipeCodec())

    class Input(val ingredient: Ingredient, val isCatalyst: Boolean)

    abstract fun getInputs(recipeEntry: RecipeEntry<R>): List<Input>
    abstract fun getOutputs(recipeEntry: RecipeEntry<R>): List<ItemStack>

    protected abstract fun createRecipeEntries(): Iterable<RecipeEntry<R>>
    val recipeEntries by lazy { createRecipeEntries() }

    protected abstract fun createView(recipeEntry: RecipeEntry<R>): View
    fun getView(rendererProxy: RendererProxy, recipeEntry: RecipeEntry<R>): View {
        val oldView = recipeEntry.viewCache
        if (oldView == null) {
            val newView = createView(recipeEntry)
            newView.layout(rendererProxy)
            recipeEntry.viewCache = newView
            return newView
        } else {
            return oldView
        }
    }

    context(ModContext)
    open fun init() {
        RecipeViewerEvents.recipeViewerCategoryCards += this
        translation.enJa()
    }

}
