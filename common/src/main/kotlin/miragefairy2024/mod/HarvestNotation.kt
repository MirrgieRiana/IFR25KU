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

class HarvestNotation(val seed: ItemStack, val crops: List<Crop>) {
    /**
     * 収穫物と、それを生じさせる判定の名前の組なのだ～🌱
     * 判定という概念を持たない収穫物では、[productionType] が null なのだ～🌱
     */
    class Crop(val itemStack: ItemStack, val productionType: Component?) {
        companion object {
            val CODEC: Codec<Crop> = RecordCodecBuilder.create { instance ->
                instance.group(
                    ItemStack.CODEC.fieldOf("ItemStack").forGetter { it.itemStack },
                    ComponentSerialization.CODEC.optionalFieldOf("ProductionType").forGetter { it.productionType.toOptional() },
                ).apply(instance) { itemStack, productionType -> Crop(itemStack, productionType.orNull) }
            }
        }
    }

    /** 登録の時点ではまだアイテムが存在しないため、[Crop] を作る材料を持ち回るのだ～🌱 */
    class CropConfiguration(val item: () -> Item, val productionType: Component?)

    companion object {
        val CODEC: Codec<HarvestNotation> = RecordCodecBuilder.create { instance ->
            instance.group(
                ItemStack.CODEC.fieldOf("Seed").forGetter { it.seed },
                Crop.CODEC.listOf().fieldOf("Crops").forGetter { it.crops },
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
fun (() -> Item).registerHarvestNotation(vararg drops: HarvestNotation.CropConfiguration) = this.registerHarvestNotation(drops.asIterable())

context(ModContext)
fun (() -> Item).registerHarvestNotation(drops: Iterable<HarvestNotation.CropConfiguration>) = ModEvents.onInitialize {
    HarvestNotation.register(this().getIdentifier(), HarvestNotation(this().createItemStack(), drops.map { HarvestNotation.Crop(it.item().createItemStack(), it.productionType) }))
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
    override fun getOutputs(recipeEntry: RecipeEntry<HarvestNotation>) = recipeEntry.recipe.crops.map { it.itemStack }

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
            recipeEntry.recipe.crops.forEach { crop ->
                view += OutputSlotView(crop.itemStack).configure {
                    if (crop.productionType != null) view.additionalTooltip = listOf(crop.productionType)
                }
            }
        }
    }
}
