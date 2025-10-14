package miragefairy2024.mod.magicplant

import com.mojang.serialization.Codec
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.magicplant.contents.magicplants.MirageFlowerCard
import miragefairy2024.mod.recipeviewer.RecipeViewerCategoryCard
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.views.FixedWidgetView
import miragefairy2024.mod.recipeviewer.views.View
import miragefairy2024.mod.recipeviewer.views.plusAssign
import miragefairy2024.util.EnJa
import miragefairy2024.util.createItemStack
import miragefairy2024.util.sortedEntrySet
import miragefairy2024.util.toIngredientStack
import net.minecraft.core.RegistryAccess
import net.minecraft.world.item.ItemStack

context(ModContext)
fun initTraitEncyclopedia() {
    TraitEncyclopediaRecipeViewerCategoryCard.init()
}

object TraitEncyclopediaRecipeViewerCategoryCard : RecipeViewerCategoryCard<Trait>() {
    override fun getId() = MirageFairy2024.identifier("trait_encyclopedia")
    override fun getName() = EnJa("Trait Encyclopedia", "特性図鑑")

    // TODO IconItem
    override fun getIcon() = MirageFlowerCard.item().createItemStack()
    override fun getRecipeCodec(registryAccess: RegistryAccess): Codec<Trait> = traitRegistry.byNameCodec()
    override fun getInputs(recipeEntry: RecipeEntry<Trait>) = getProducerMagicPlantSeedItemStacks(recipeEntry.recipe).map { Input(it.toIngredientStack(), true) }

    override fun createRecipeEntries(): Iterable<RecipeEntry<Trait>> {
        return traitRegistry.sortedEntrySet.map { (id, trait) ->
            RecipeEntry(id.location(), trait, true)
        }
    }

    override fun createView(recipeEntry: RecipeEntry<Trait>) = View {
        view += TraitEncyclopediaView(18 * 9 + 5, 140, recipeEntry.recipe)
    }

    fun getProducerMagicPlantSeedItemStacks(trait: Trait): List<ItemStack> {
        return magicPlantCards
            // TODO デフォルト特性
            .filter { trait in it.randomTraitChances }
            .map { card -> card.item().createItemStack().also { it.setTraitStacks(TraitStacks.of(TraitStack(trait, 1))) } }
    }
}

class TraitEncyclopediaView(width: Int, height: Int, val trait: Trait) : FixedWidgetView(IntPoint(width, height))
