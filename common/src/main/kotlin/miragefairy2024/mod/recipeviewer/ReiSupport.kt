package miragefairy2024.mod.recipeviewer

import me.shedaniel.rei.api.common.category.CategoryIdentifier
import me.shedaniel.rei.api.common.display.DisplaySerializer
import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry
import me.shedaniel.rei.api.common.display.basic.BasicDisplay
import me.shedaniel.rei.api.common.entry.comparison.ItemComparatorRegistry
import miragefairy2024.InitializationEventRegistry
import miragefairy2024.ModContext
import miragefairy2024.util.CompoundTag
import miragefairy2024.util.get
import miragefairy2024.util.times
import miragefairy2024.util.toEntryIngredient
import miragefairy2024.util.toEntryStack
import miragefairy2024.util.wrapper
import mirrg.kotlin.helium.Single
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.resources.RegistryOps

object ReiEvents {
    val onRegisterDisplaySerializer = InitializationEventRegistry<(DisplaySerializerRegistry) -> Unit>()
    val onRegisterItemComparators = InitializationEventRegistry<(ItemComparatorRegistry) -> Unit>()
}

context(ModContext)
fun initReiSupport() {
    ReiEvents.onRegisterDisplaySerializer {
        RecipeViewerEvents.recipeViewerCategoryCards.forEach { card ->
            ReiSupport.get(card).registerDisplaySerializer(it)
        }
    }
}

class ReiSupport<R> private constructor(val card: RecipeViewerCategoryCard<R>) {
    companion object {
        private val table = mutableMapOf<RecipeViewerCategoryCard<*>, ReiSupport<*>>()
        fun <R> get(card: RecipeViewerCategoryCard<R>): ReiSupport<R> {
            @Suppress("UNCHECKED_CAST")
            return table.getOrPut(card) { ReiSupport(card) } as ReiSupport<R>
        }
    }

    // Singleを取り除くとREI無しで起動するとクラッシュする
    val categoryIdentifier: Single<CategoryIdentifier<SupportedDisplay<R>>> by lazy { Single(CategoryIdentifier.of("plugins/" * card.getId())) }

    val displaySerializer: Single<DisplaySerializer<SupportedDisplay<R>>> by lazy {
        Single(object : DisplaySerializer<SupportedDisplay<R>> {
            override fun save(tag: CompoundTag, display: SupportedDisplay<R>): CompoundTag {
                val ops = RegistryOps.create(NbtOps.INSTANCE, BasicDisplay.registryAccess())
                val recipeTag = card.getRecipeCodec().encodeStart(ops, display.recipe).orThrow
                return CompoundTag("Recipe" to recipeTag)
            }

            override fun read(tag: CompoundTag): SupportedDisplay<R> {
                val ops = RegistryOps.create(NbtOps.INSTANCE, BasicDisplay.registryAccess())
                val recipeTag = tag.wrapper["Recipe"].get()
                val recipe = card.getRecipeCodec().decode(ops, recipeTag).orThrow.first
                return SupportedDisplay(this@ReiSupport, recipe)
            }
        })
    }

    fun registerDisplaySerializer(registry: DisplaySerializerRegistry) {
        registry.register(categoryIdentifier.first, displaySerializer.first)
    }

}

class SupportedDisplay<R>(val support: ReiSupport<R>, val recipe: R) : BasicDisplay(
    support.card.getInputs(recipe).map { it.ingredient.toEntryIngredient() },
    support.card.getOutputs(recipe).map { it.toEntryStack().toEntryIngredient() },
) {
    override fun getCategoryIdentifier() = support.categoryIdentifier.first
}
