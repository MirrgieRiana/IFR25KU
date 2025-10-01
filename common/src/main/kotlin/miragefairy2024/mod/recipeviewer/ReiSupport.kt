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
    ReiSupport.instance.init()
}

class ReiSupport<R> private constructor(val card: RecipeViewerCategoryCard<R>) {
    companion object {
        val instance by lazy { ReiSupport(HarvestNotationRecipeViewerCategoryCard) }
    }

    val path = "harvest"
    val enName = "Harvest"
    val jaName = "収穫"
    val translation = Translation({ "category.rei.${MirageFairy2024.identifier(path).toLanguageKey()}" }, enName, jaName)

    // Singleを取り除くとREI無しで起動するとクラッシュする
    val identifier: Single<CategoryIdentifier<Display>> by lazy { Single(CategoryIdentifier.of(MirageFairy2024.MOD_ID, "plugins/$path")) }

    val serializer: Single<BasicDisplay.Serializer<Display>> by lazy {
        Single(BasicDisplay.Serializer.ofRecipeLess({ _, _, tag ->
            Display(
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

    inner class Display(val recipe: HarvestNotation) : BasicDisplay(
        listOf(recipe.seed.toEntryStack().toEntryIngredient()),
        recipe.crops.map { it.toEntryStack().toEntryIngredient() },
    ) {
        override fun getCategoryIdentifier() = identifier.first
    }

    context(ModContext)
    fun init() {
        translation.enJa()
        ReiEvents.onRegisterDisplaySerializer {
            it.register(identifier.first, serializer.first)
        }
    }
}
