package miragefairy2024.mod.recipeviewer

import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry
import me.shedaniel.rei.api.common.display.basic.BasicDisplay
import me.shedaniel.rei.api.common.entry.comparison.ItemComparatorRegistry
import miragefairy2024.InitializationEventRegistry
import miragefairy2024.ModContext
import miragefairy2024.mod.HarvestNotation
import miragefairy2024.mod.rei.ReiCategoryCard
import miragefairy2024.util.compound
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
    HarvestReiCategoryCard.init()
}

object HarvestReiCategoryCard : ReiCategoryCard<HarvestReiCategoryCard.Display>("harvest", "Harvest", "収穫") {
    override val serializer: Single<BasicDisplay.Serializer<Display>> by lazy {
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

    class Display(val recipe: HarvestNotation) : BasicDisplay(
        listOf(recipe.seed.toEntryStack().toEntryIngredient()),
        recipe.crops.map { it.toEntryStack().toEntryIngredient() },
    ) {
        override fun getCategoryIdentifier() = identifier.first
    }
}
