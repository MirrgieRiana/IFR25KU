package miragefairy2024.mod

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.mod.recipeviewer.RecipeViewerCategoryCard
import miragefairy2024.util.createItemStack
import miragefairy2024.util.getIdentifier
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
    override fun getId() = MirageFairy2024.identifier("harvest")
}
