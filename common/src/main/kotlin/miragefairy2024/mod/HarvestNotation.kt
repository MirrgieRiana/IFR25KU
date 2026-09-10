package miragefairy2024.mod

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.mod.materials.MaterialCard
import miragefairy2024.mod.recipeviewer.RecipeViewerCategoryCard
import miragefairy2024.mod.recipeviewer.views.ArrowView
import miragefairy2024.mod.recipeviewer.views.CatalystSlotView
import miragefairy2024.mod.recipeviewer.views.OutputSlotView
import miragefairy2024.mod.recipeviewer.views.View
import miragefairy2024.mod.recipeviewer.views.XListView
import miragefairy2024.mod.recipeviewer.views.XSpaceView
import miragefairy2024.mod.recipeviewer.views.configure
import miragefairy2024.mod.recipeviewer.views.plusAssign
import miragefairy2024.util.EnJa
import miragefairy2024.util.createItemStack
import miragefairy2024.util.getIdentifier
import miragefairy2024.util.toIngredientStack
import mirrg.kotlin.java.hydrogen.orNull
import mirrg.kotlin.java.hydrogen.toOptional
import net.minecraft.core.RegistryAccess
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentSerialization
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

/** 収穫物には、それが生じる判定の名前が添うのだ～🌱 判定という概念を持たない収穫物は、名前が null なのだ～🌱 */
class HarvestNotation(val seed: ItemStack, val crops: List<Pair<ItemStack, Component?>>) {
    companion object {
        private val CROP_CODEC: Codec<Pair<ItemStack, Component?>> = RecordCodecBuilder.create { instance ->
            instance.group(
                ItemStack.CODEC.fieldOf("ItemStack").forGetter { it.first },
                ComponentSerialization.CODEC.optionalFieldOf("ProductionType").forGetter { it.second.toOptional() },
            ).apply(instance) { itemStack, productionType -> Pair(itemStack, productionType.orNull) }
        }

        val CODEC: Codec<HarvestNotation> = RecordCodecBuilder.create { instance ->
            instance.group(
                ItemStack.CODEC.fieldOf("Seed").forGetter { it.seed },
                CROP_CODEC.listOf().fieldOf("Crops").forGetter { it.crops },
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
fun (() -> Item).registerHarvestNotation(vararg drops: Pair<() -> Item, Component?>) = this.registerHarvestNotation(drops.asIterable())

context(ModContext)
fun (() -> Item).registerHarvestNotation(drops: Iterable<Pair<() -> Item, Component?>>) = ModEvents.onInitialize {
    HarvestNotation.register(this().getIdentifier(), HarvestNotation(this().createItemStack(), drops.map { Pair(it.first().createItemStack(), it.second) }))
}

context(ModContext)
fun initHarvestNotationModule() {
    HarvestNotationRecipeViewerCategoryCard.init()
}

object HarvestNotationRecipeViewerCategoryCard : RecipeViewerCategoryCard<HarvestNotation>() {
    override fun getId() = MirageFairy2024.identifier("harvest_notation")
    override fun getName() = EnJa("Harvest", "収穫")
    override fun getIcon() = MaterialCard.VEROPEDA_BERRIES.item().createItemStack()
    override fun getRecipeCodec(registryAccess: RegistryAccess) = HarvestNotation.CODEC
    override fun getInputs(recipeEntry: RecipeEntry<HarvestNotation>) = listOf(Input(recipeEntry.recipe.seed.toIngredientStack(), true))
    override fun getOutputs(recipeEntry: RecipeEntry<HarvestNotation>) = recipeEntry.recipe.crops.map { it.first }

    override fun createRecipeEntries(registryAccess: RegistryAccess): Iterable<RecipeEntry<HarvestNotation>> {
        return HarvestNotation.getAll().map { (id, harvestNotation) ->
            RecipeEntry(registryAccess, id, harvestNotation, true)
        }
    }

    override fun createView(recipeEntry: RecipeEntry<HarvestNotation>) = View {
        view += XListView().configure {
            view += CatalystSlotView(recipeEntry.recipe.seed.toIngredientStack())
            view += XSpaceView(2)
            view += ArrowView()
            view += XSpaceView(2)
            recipeEntry.recipe.crops.forEach { (crop, productionType) ->
                view += OutputSlotView(crop).configure {
                    if (productionType != null) view.additionalTooltip = listOf(productionType)
                }
            }
        }
    }
}
