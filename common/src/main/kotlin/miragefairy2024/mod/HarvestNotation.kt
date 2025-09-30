package miragefairy2024.mod

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.mod.materials.MaterialCard
import miragefairy2024.mod.recipeviewer.CatalystSlotView
import miragefairy2024.mod.recipeviewer.HorizontalListView
import miragefairy2024.mod.recipeviewer.HorizontalSpaceView
import miragefairy2024.mod.recipeviewer.OutputSlotView
import miragefairy2024.mod.recipeviewer.RecipeViewerCategoryCard
import miragefairy2024.mod.recipeviewer.VerticalListView
import miragefairy2024.mod.recipeviewer.VerticalSpaceView
import miragefairy2024.mod.recipeviewer.View
import miragefairy2024.mod.recipeviewer.plusAssign
import miragefairy2024.util.EnJa
import miragefairy2024.util.createItemStack
import miragefairy2024.util.toIngredient
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

val harvestNotations = mutableListOf<HarvestNotation>()

class HarvestNotation(val seed: ItemStack, val crops: List<ItemStack>) {
    companion object {
        val CODEC: Codec<HarvestNotation> = RecordCodecBuilder.create { instance ->
            instance.group(
                ItemStack.CODEC.fieldOf("Seed").forGetter { it.seed },
                ItemStack.CODEC.listOf().fieldOf("Crops").forGetter { it.crops },
            ).apply(instance, ::HarvestNotation)
        }
    }
}

context(ModContext)
fun (() -> Item).registerHarvestNotation(vararg drops: () -> Item) = this.registerHarvestNotation(drops.asIterable())

context(ModContext)
fun (() -> Item).registerHarvestNotation(drops: Iterable<() -> Item>) = ModEvents.onInitialize {
    harvestNotations += HarvestNotation(this().createItemStack(), drops.map { it().createItemStack() })
}

context(ModContext)
fun initHarvestNotationModule() {
    HarvestNotationRecipeViewerCategoryCard.init()
}

object HarvestNotationRecipeViewerCategoryCard : RecipeViewerCategoryCard<HarvestNotation>() {
    override fun getId() = MirageFairy2024.identifier("harvest")
    override fun getName() = EnJa("Harvest", "収穫")
    override fun getIcon() = MaterialCard.VEROPEDA_BERRIES.item().createItemStack()
    override fun getRecipeCodec() = HarvestNotation.CODEC
    override fun getInputs(recipeEntry: RecipeEntry<HarvestNotation>) = listOf(Input(recipeEntry.recipe.seed.toIngredient(), true))
    override fun getOutputs(recipeEntry: RecipeEntry<HarvestNotation>) = recipeEntry.recipe.crops

    override fun createRecipeEntries(): Iterable<RecipeEntry<HarvestNotation>> {
        return harvestNotations.withIndex().map { (index, harvestNotation) ->
            RecipeEntry(MirageFairy2024.identifier("/harvest/$index"), harvestNotation) // TODO
        }
    }

    override fun createView(recipeEntry: RecipeEntry<HarvestNotation>): View {
        return VerticalListView<View>().also { vertical ->
            vertical += VerticalSpaceView(1)
            vertical += HorizontalListView<View>().also { horizontal ->
                horizontal += HorizontalSpaceView(1)
                horizontal += CatalystSlotView(recipeEntry.recipe.seed.toIngredient())
                horizontal += HorizontalSpaceView(4)
                recipeEntry.recipe.crops.forEach { crop ->
                    horizontal += OutputSlotView(crop)
                }
                horizontal += HorizontalSpaceView(1)
            }
            vertical += VerticalSpaceView(1)
        }
    }
}
