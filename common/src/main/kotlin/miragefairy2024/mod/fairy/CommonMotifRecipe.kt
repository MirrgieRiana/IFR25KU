package miragefairy2024.mod.fairy

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.materials.MIRAGE_FLOUR_TAG
import miragefairy2024.mod.recipeviewer.RecipeViewerCategoryCard
import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.ColorPair
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.views.OutputSlotView
import miragefairy2024.mod.recipeviewer.views.TextView
import miragefairy2024.mod.recipeviewer.views.View
import miragefairy2024.mod.recipeviewer.views.XListView
import miragefairy2024.mod.recipeviewer.views.XSpaceView
import miragefairy2024.mod.recipeviewer.views.configure
import miragefairy2024.mod.recipeviewer.views.plusAssign
import miragefairy2024.util.BiomeCondition
import miragefairy2024.util.EnJa
import miragefairy2024.util.enJa
import miragefairy2024.util.times
import miragefairy2024.util.toIngredient
import net.minecraft.core.RegistryAccess

val COMMON_MOTIF_RECIPES = mutableListOf<CommonMotifRecipe>()

class CommonMotifRecipe(val motif: Motif, val biomeCondition: BiomeCondition) {
    companion object {
        val CODEC: Codec<CommonMotifRecipe> = RecordCodecBuilder.create { instance ->
            instance.group(
                motifRegistry.byNameCodec().fieldOf("Motif").forGetter { it.motif },
                BiomeCondition.CODEC.fieldOf("BiomeCondition").forGetter { it.biomeCondition },
            ).apply(instance, ::CommonMotifRecipe)
        }
    }
}

context(ModContext)
fun initCommonMotifRecipe() {
    CommonMotifRecipeRecipeViewerCategoryCard.init()

    BiomeCondition.Always.TRANSLATION.enJa()
}

object CommonMotifRecipeRecipeViewerCategoryCard : RecipeViewerCategoryCard<CommonMotifRecipe>() {
    override fun getId() = MirageFairy2024.identifier("common_motif_recipe")
    override fun getName() = EnJa("Common Fairy", "コモン妖精")
    override fun getIcon() = MotifCard.WATER.createFairyItemStack()
    override fun getWorkstations() = MIRAGE_FLOUR_TAG.toIngredient().items.toList()
    override fun getRecipeCodec(registryAccess: RegistryAccess) = CommonMotifRecipe.CODEC
    override fun getOutputs(recipeEntry: RecipeEntry<CommonMotifRecipe>) = listOf(recipeEntry.recipe.motif.createFairyItemStack())

    override fun createRecipeEntries(registryAccess: RegistryAccess): Iterable<RecipeEntry<CommonMotifRecipe>> {
        return COMMON_MOTIF_RECIPES
            .map {
                val syntheticIdentifier = "${it.biomeCondition.sortKey}/" * it.motif.getIdentifier()!!
                Pair(it, syntheticIdentifier)
            }
            .sortedBy { it.second }
            .map { (recipe, id) -> RecipeEntry(registryAccess, id, recipe, true) }
    }

    override fun createView(recipeEntry: RecipeEntry<CommonMotifRecipe>) = View {
        view += XListView().configure {
            view.sizingX = Sizing.FILL
            val recipeText = recipeEntry.recipe.biomeCondition.getDisplayName()
            view += TextView(recipeText).configure {
                position.alignmentY = Alignment.CENTER
                position.weight = 1.0
                view.sizingX = Sizing.FILL
                view.color = ColorPair.DARK_GRAY
                view.shadow = false
                view.scroll = true
                val tooltip = recipeEntry.recipe.biomeCondition.getTooltip()
                if (tooltip.isNotEmpty()) view.tooltip = tooltip
            }
            view += XSpaceView(2)
            view += OutputSlotView(recipeEntry.recipe.motif.createFairyItemStack())
        }
    }
}
