package miragefairy2024.mod.recipeviewer

import me.shedaniel.rei.api.common.category.CategoryIdentifier
import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry
import me.shedaniel.rei.api.common.display.basic.BasicDisplay
import me.shedaniel.rei.api.common.entry.comparison.ItemComparatorRegistry
import miragefairy2024.InitializationEventRegistry
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.HarvestNotation
import miragefairy2024.mod.HarvestNotationRecipeViewerCategoryCard
import miragefairy2024.util.Translation
import miragefairy2024.util.compound
import miragefairy2024.util.enJa
import miragefairy2024.util.get
import miragefairy2024.util.list
import miragefairy2024.util.toEntryIngredient
import miragefairy2024.util.toEntryStack
import miragefairy2024.util.toItemStack
import miragefairy2024.util.toNbt
import miragefairy2024.util.wrapper
import mirrg.kotlin.helium.Single
import mirrg.kotlin.helium.castOrThrow
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag

object ReiEvents {
    val onRegisterDisplaySerializer = InitializationEventRegistry<(DisplaySerializerRegistry) -> Unit>()
    val onRegisterItemComparators = InitializationEventRegistry<(ItemComparatorRegistry) -> Unit>()
}

context(ModContext)
fun initReiSupport() {
    ReiSupport.get(HarvestNotationRecipeViewerCategoryCard).init()
}

class ReiSupport<R> private constructor(val card: RecipeViewerCategoryCard<R>) {
    companion object {
        private val table = mutableMapOf<RecipeViewerCategoryCard<*>, ReiSupport<*>>()
        fun <R> get(card: RecipeViewerCategoryCard<R>): ReiSupport<R> {
            @Suppress("UNCHECKED_CAST")
            return table.getOrPut(card) { ReiSupport(card) } as ReiSupport<R>
        }
    }

    val id = MirageFairy2024.identifier("harvest")
    val enName = "Harvest"
    val jaName = "収穫"
    val translation = Translation({ "category.rei.${id.toLanguageKey()}" }, enName, jaName)

    // Singleを取り除くとREI無しで起動するとクラッシュする
    val identifier: Single<CategoryIdentifier<SupportedDisplay<R>>> by lazy { Single(CategoryIdentifier.of(id.namespace, "plugins/${id.path}")) }

    val serializer: Single<BasicDisplay.Serializer<SupportedDisplay<R>>> by lazy {
        Single(BasicDisplay.Serializer.ofRecipeLess({ _, _, tag ->
            SupportedDisplay(
                this,
                HarvestNotation(
                    tag.wrapper["Seed"].compound.get()!!.toItemStack(BasicDisplay.registryAccess())!!,
                    tag.wrapper["Crops"].list.get()!!.map { it.castOrThrow<CompoundTag>().toItemStack(BasicDisplay.registryAccess())!! },
                )
            )
        }, { display, tag ->
            tag.wrapper["Seed"].set(display.recipe.seed.toNbt(BasicDisplay.registryAccess()))
            tag.wrapper["Crops"].set(display.recipe.crops.mapTo(ListTag()) { it.toNbt(BasicDisplay.registryAccess()) })
        }))
    }

    context(ModContext)
    fun init() {
        translation.enJa()
        ReiEvents.onRegisterDisplaySerializer {
            it.register(identifier.first, serializer.first)
        }
    }
}

class SupportedDisplay<R>(val support: ReiSupport<R>, val recipe: HarvestNotation) : BasicDisplay(
    listOf(recipe.seed.toEntryStack().toEntryIngredient()),
    recipe.crops.map { it.toEntryStack().toEntryIngredient() },
) {
    override fun getCategoryIdentifier() = support.identifier.first
}
