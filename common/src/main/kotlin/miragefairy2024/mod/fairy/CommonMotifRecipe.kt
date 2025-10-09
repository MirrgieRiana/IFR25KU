package miragefairy2024.mod.fairy

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.materials.MIRAGE_FLOUR_TAG
import miragefairy2024.mod.recipeviewer.RecipeViewerCategoryCard
import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.ColorPair
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.views.OutputSlotView
import miragefairy2024.mod.recipeviewer.views.TextView
import miragefairy2024.mod.recipeviewer.views.XListView
import miragefairy2024.mod.recipeviewer.views.XSpaceView
import miragefairy2024.mod.recipeviewer.views.configure
import miragefairy2024.mod.recipeviewer.views.plusAssign
import miragefairy2024.util.EnJa
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.invoke
import miragefairy2024.util.pathString
import miragefairy2024.util.string
import miragefairy2024.util.text
import miragefairy2024.util.times
import miragefairy2024.util.toIngredient
import miragefairy2024.util.translate
import net.minecraft.core.RegistryAccess
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.TagKey
import net.minecraft.world.level.biome.Biome

val COMMON_MOTIF_RECIPES = mutableListOf<CommonMotifRecipe>()

sealed class CommonMotifRecipe(val motif: Motif) {
    companion object {
        val CODEC: Codec<CommonMotifRecipe> = Codec.STRING.dispatch(
            "Type",
            { recipe: CommonMotifRecipe ->
                when (recipe) {
                    is AlwaysCommonMotifRecipe -> "always"
                    is BiomeCommonMotifRecipe -> "biome"
                    is BiomeTagCommonMotifRecipe -> "biome_tag"
                }
            },
            { type: String ->
                when (type) {
                    "always" -> AlwaysCommonMotifRecipe.CODEC
                    "biome" -> BiomeCommonMotifRecipe.CODEC
                    "biome_tag" -> BiomeTagCommonMotifRecipe.CODEC
                    else -> throw IllegalArgumentException("Unknown CommonMotifRecipe type: $type")
                }
            }
        )
    }
}

class AlwaysCommonMotifRecipe(motif: Motif) : CommonMotifRecipe(motif) {
    companion object {
        val CODEC: MapCodec<AlwaysCommonMotifRecipe> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                motifRegistry.byNameCodec().fieldOf("Motif").forGetter { it.motif },
            ).apply(instance, ::AlwaysCommonMotifRecipe)
        }
    }
}

class BiomeCommonMotifRecipe(motif: Motif, val biome: ResourceKey<Biome>) : CommonMotifRecipe(motif) {
    companion object {
        val CODEC: MapCodec<BiomeCommonMotifRecipe> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                motifRegistry.byNameCodec().fieldOf("Motif").forGetter { it.motif },
                ResourceKey.codec(Registries.BIOME).fieldOf("Biome").forGetter { it.biome }
            ).apply(instance, ::BiomeCommonMotifRecipe)
        }
    }
}

class BiomeTagCommonMotifRecipe(motif: Motif, val biomeTag: TagKey<Biome>) : CommonMotifRecipe(motif) {
    companion object {
        val CODEC: MapCodec<BiomeTagCommonMotifRecipe> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                motifRegistry.byNameCodec().fieldOf("Motif").forGetter { it.motif },
                TagKey.codec(Registries.BIOME).fieldOf("BiomeTag").forGetter { it.biomeTag }
            ).apply(instance, ::BiomeTagCommonMotifRecipe)
        }
    }
}

val COMMON_MOTIF_RECIPE_ALWAYS_TRANSLATION = Translation({ "gui.${MirageFairy2024.identifier("common_motif_recipe").toLanguageKey()}.always" }, "Always", "常時")

context(ModContext)
fun initCommonMotifRecipe() {
    CommonMotifRecipeRecipeViewerCategoryCard.init()

    COMMON_MOTIF_RECIPE_ALWAYS_TRANSLATION.enJa()
}

object CommonMotifRecipeRecipeViewerCategoryCard : RecipeViewerCategoryCard<CommonMotifRecipe>() {
    override fun getId() = MirageFairy2024.identifier("common_motif_recipe")
    override fun getName() = EnJa("Common Fairy", "コモン妖精")
    override fun getIcon() = MotifCard.WATER.createFairyItemStack()
    override fun getWorkstations() = MIRAGE_FLOUR_TAG.toIngredient().items.toList()
    override fun getRecipeCodec(registryAccess: RegistryAccess) = CommonMotifRecipe.CODEC
    override fun getOutputs(recipeEntry: RecipeEntry<CommonMotifRecipe>) = listOf(recipeEntry.recipe.motif.createFairyItemStack())

    override fun createRecipeEntries(): Iterable<RecipeEntry<CommonMotifRecipe>> {
        return COMMON_MOTIF_RECIPES
            .map {
                val prefix = when (it) {
                    is AlwaysCommonMotifRecipe -> "1_always"
                    is BiomeCommonMotifRecipe -> "2_biome/" + it.biome.location().pathString
                    is BiomeTagCommonMotifRecipe -> "3_biome_tag/" + it.biomeTag.location().pathString
                }
                val syntheticIdentifier = "$prefix/" * it.motif.getIdentifier()!!
                Pair(it, syntheticIdentifier)
            }
            .sortedBy { it.second }
            .map { (recipe, id) -> RecipeEntry(id, recipe, true) }
    }

    override fun createView(recipeEntry: RecipeEntry<CommonMotifRecipe>) = View {
        view += XListView().configure {
            val recipeText = when (val recipe = recipeEntry.recipe) {
                is AlwaysCommonMotifRecipe -> text { COMMON_MOTIF_RECIPE_ALWAYS_TRANSLATION() }
                is BiomeCommonMotifRecipe -> text { translate(recipe.biome.location().toLanguageKey("biome")) }
                is BiomeTagCommonMotifRecipe -> text { recipe.biomeTag.location().path() }
            }
            view += TextView(recipeText).configure {
                position = Alignment.CENTER
                view.minWidth = 130
                view.color = ColorPair.DARK_GRAY
                view.shadow = false
                when (val recipe = recipeEntry.recipe) {
                    is AlwaysCommonMotifRecipe -> Unit
                    is BiomeCommonMotifRecipe -> Unit
                    is BiomeTagCommonMotifRecipe -> view.tooltip = listOf(text { recipe.biomeTag.location().string() })
                }
            }
            view += XSpaceView(2)
            view += OutputSlotView(recipeEntry.recipe.motif.createFairyItemStack())
        }
    }
}
