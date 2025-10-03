package miragefairy2024.mod

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.mod.materials.MaterialCard
import miragefairy2024.mod.recipeviewer.ArrowView
import miragefairy2024.mod.recipeviewer.CatalystSlotView
import miragefairy2024.mod.recipeviewer.OutputSlotView
import miragefairy2024.mod.recipeviewer.RecipeViewerCategoryCard
import miragefairy2024.mod.recipeviewer.View
import miragefairy2024.mod.recipeviewer.XListView
import miragefairy2024.mod.recipeviewer.XSpaceView
import miragefairy2024.mod.recipeviewer.plusAssign
import miragefairy2024.util.EnJa
import miragefairy2024.util.createItemStack
import miragefairy2024.util.getIdentifier
import miragefairy2024.util.pathString
import miragefairy2024.util.times
import miragefairy2024.util.toIngredientStack
import net.minecraft.core.RegistryAccess
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

class HarvestNotation(val seed: ItemStack, val crops: List<ItemStack>) {
    companion object {
        val CODEC: Codec<HarvestNotation> = RecordCodecBuilder.create { instance ->
            instance.group(
                ItemStack.CODEC.fieldOf("Seed").forGetter { it.seed },
                ItemStack.CODEC.listOf().fieldOf("Crops").forGetter { it.crops },
            ).apply(instance, ::HarvestNotation)
        }

        private val map = mutableMapOf<ResourceLocation, HarvestNotation>()

        fun register(id: ResourceLocation, harvestNotation: HarvestNotation) {
            check(id !in map) { "Duplicate registration: $id" }
            map[id] = harvestNotation
        }

        fun getAll(): Map<ResourceLocation, HarvestNotation> = map
    }
}

context(ModContext)
fun (() -> Item).registerHarvestNotation(vararg drops: () -> Item) = this.registerHarvestNotation(drops.asIterable())

context(ModContext)
fun (() -> Item).registerHarvestNotation(drops: Iterable<() -> Item>) = ModEvents.onInitialize {
    HarvestNotation.register(this().getIdentifier(), HarvestNotation(this().createItemStack(), drops.map { it().createItemStack() }))
}

context(ModContext)
fun initHarvestNotationModule() {
    HarvestNotationRecipeViewerCategoryCard.init()
}

object HarvestNotationRecipeViewerCategoryCard : RecipeViewerCategoryCard<HarvestNotation>() {
    override fun getId() = MirageFairy2024.identifier("harvest_notation")
    override fun getName() = EnJa("Harvest", "収穫")
    override fun getIcon() = MaterialCard.VEROPEDA_BERRIES.item().createItemStack()
    override fun getWorkstations() = listOf<ItemStack>()
    override fun getRecipeCodec(registryAccess: RegistryAccess) = HarvestNotation.CODEC
    override fun getInputs(recipeEntry: RecipeEntry<HarvestNotation>) = listOf(Input(recipeEntry.recipe.seed.toIngredientStack(), true))
    override fun getOutputs(recipeEntry: RecipeEntry<HarvestNotation>) = recipeEntry.recipe.crops

    override fun createRecipeEntries(): Iterable<RecipeEntry<HarvestNotation>> {
        return HarvestNotation.getAll().map { (id, harvestNotation) ->
            RecipeEntry("${getId().pathString}/" * id, harvestNotation, true)
        }
    }

    override fun createView(recipeEntry: RecipeEntry<HarvestNotation>) = View {
        this += XListView {
            this += CatalystSlotView(recipeEntry.recipe.seed.toIngredientStack())
            this += XSpaceView(2)
            this += ArrowView()
            this += XSpaceView(2)
            recipeEntry.recipe.crops.forEach { crop ->
                this += OutputSlotView(crop)
            }
        }
    }
}
