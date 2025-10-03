package miragefairy2024.mod.recipeviewer

import me.shedaniel.rei.api.common.category.CategoryIdentifier
import me.shedaniel.rei.api.common.display.Display
import me.shedaniel.rei.api.common.display.DisplaySerializer
import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry
import me.shedaniel.rei.api.common.display.basic.BasicDisplay
import me.shedaniel.rei.api.common.entry.comparison.EntryComparator
import me.shedaniel.rei.api.common.entry.comparison.ItemComparatorRegistry
import miragefairy2024.ModContext
import miragefairy2024.ReusableInitializationEventRegistry
import miragefairy2024.util.CompoundTag
import miragefairy2024.util.get
import miragefairy2024.util.toEntryIngredient
import miragefairy2024.util.toEntryStack
import miragefairy2024.util.wrapper
import mirrg.kotlin.helium.Single
import mirrg.kotlin.java.hydrogen.toOptional
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.resources.RegistryOps
import java.util.Objects

object ReiEvents {
    val onRegisterDisplaySerializer = ReusableInitializationEventRegistry<(DisplaySerializerRegistry) -> Unit>()
    val onRegisterItemComparators = ReusableInitializationEventRegistry<(ItemComparatorRegistry) -> Unit>()
}

context(ModContext)
fun initReiSupport() {
    ReiEvents.onRegisterDisplaySerializer {
        RecipeViewerEvents.recipeViewerCategoryCards.freezeAndGet().forEach { card ->
            ReiSupport.get(card).registerDisplaySerializer(it)
        }
    }

    ReiEvents.onRegisterItemComparators { registry ->
        RecipeViewerEvents.itemIdentificationDataComponentTypesList.freezeAndGet().forEach { (item, dataComponentTypes) ->
            registry.register({ context, itemStack ->
                if (context.isExact) {
                    EntryComparator.itemComponents().hash(context, itemStack)
                } else {
                    Objects.hash(*dataComponentTypes().map { itemStack[it] }.toTypedArray()).toLong()
                }
            }, item())
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

    val categoryIdentifier: Single<CategoryIdentifier<SupportedDisplay<R>>> by lazy { // 非ロード環境用のSingle
        Single(CategoryIdentifier.of(card.getId()))
    }

    val displaySerializer: Single<DisplaySerializer<SupportedDisplay<R>>> by lazy { // 非ロード環境用のSingle
        Single(object : DisplaySerializer<SupportedDisplay<R>> {
            override fun save(tag: CompoundTag, display: SupportedDisplay<R>): CompoundTag {
                val ops = RegistryOps.create(NbtOps.INSTANCE, BasicDisplay.registryAccess())
                val recipeEntryTag = card.recipeEntryCodec.encodeStart(ops, display.recipeEntry).orThrow
                return CompoundTag("RecipeEntry" to recipeEntryTag)
            }

            override fun read(tag: CompoundTag): SupportedDisplay<R> {
                val ops = RegistryOps.create(NbtOps.INSTANCE, BasicDisplay.registryAccess())
                val recipeEntryTag = tag.wrapper["RecipeEntry"].get()
                val recipeEntry = card.recipeEntryCodec.decode(ops, recipeEntryTag).orThrow.first
                return SupportedDisplay(this@ReiSupport, recipeEntry)
            }
        })
    }

    fun registerDisplaySerializer(registry: DisplaySerializerRegistry) {
        registry.register(categoryIdentifier.first, displaySerializer.first)
    }

}

class SupportedDisplay<R>(val support: ReiSupport<R>, val recipeEntry: RecipeViewerCategoryCard.RecipeEntry<R>) : Display {
    override fun getInputEntries() = support.card.getInputs(recipeEntry).map { it.ingredient.toEntryIngredient() }
    override fun getOutputEntries() = support.card.getOutputs(recipeEntry).map { it.toEntryStack().toEntryIngredient() }
    override fun getCategoryIdentifier() = support.categoryIdentifier.first
    override fun getDisplayLocation() = recipeEntry.takeIf { !it.isSynthetic }?.id.toOptional()
}
