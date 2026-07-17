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
                    is BiomeConditionCommonMotifRecipe -> when (recipe.biomeCondition) {
                        is BiomeCondition.BiomeKey -> "biome"
                        is BiomeCondition.BiomeTag -> "biome_tag"
                    }
                }
            },
            { type: String ->
                when (type) {
                    "always" -> AlwaysCommonMotifRecipe.CODEC
                    "biome" -> BiomeConditionCommonMotifRecipe.BIOME_CODEC
                    "biome_tag" -> BiomeConditionCommonMotifRecipe.BIOME_TAG_CODEC
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

class BiomeConditionCommonMotifRecipe(motif: Motif, val biomeCondition: BiomeCondition) : CommonMotifRecipe(motif) {
    companion object {
        val BIOME_CODEC: MapCodec<BiomeConditionCommonMotifRecipe> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                motifRegistry.byNameCodec().fieldOf("Motif").forGetter { it.motif },
                ResourceKey.codec(Registries.BIOME).fieldOf("Biome").forGetter { (it.biomeCondition as BiomeCondition.BiomeKey).biome },
            ).apply(instance) { motif, biome -> BiomeConditionCommonMotifRecipe(motif, BiomeCondition.BiomeKey(biome)) }
        }
        val BIOME_TAG_CODEC: MapCodec<BiomeConditionCommonMotifRecipe> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                motifRegistry.byNameCodec().fieldOf("Motif").forGetter { it.motif },
                TagKey.codec(Registries.BIOME).fieldOf("BiomeTag").forGetter { (it.biomeCondition as BiomeCondition.BiomeTag).biomeTag },
            ).apply(instance) { motif, biomeTag -> BiomeConditionCommonMotifRecipe(motif, BiomeCondition.BiomeTag(biomeTag)) }
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

    override fun createRecipeEntries(registryAccess: RegistryAccess): Iterable<RecipeEntry<CommonMotifRecipe>> {
        return COMMON_MOTIF_RECIPES
            .map {
                val prefix = when (it) {
                    is AlwaysCommonMotifRecipe -> "1_always"
                    is BiomeConditionCommonMotifRecipe -> when (val condition = it.biomeCondition) {
                        is BiomeCondition.BiomeKey -> "2_biome/" + condition.biome.location().pathString
                        is BiomeCondition.BiomeTag -> "3_biome_tag/" + condition.biomeTag.location().pathString
                    }
                }
                val syntheticIdentifier = "$prefix/" * it.motif.getIdentifier()!!
                Pair(it, syntheticIdentifier)
            }
            .sortedBy { it.second }
            .map { (recipe, id) -> RecipeEntry(registryAccess, id, recipe, true) }
    }

    override fun createView(recipeEntry: RecipeEntry<CommonMotifRecipe>) = View {
        view += XListView().configure {
            view.sizingX = Sizing.FILL
            val recipeText = when (val recipe = recipeEntry.recipe) {
                is AlwaysCommonMotifRecipe -> text { COMMON_MOTIF_RECIPE_ALWAYS_TRANSLATION() }
                is BiomeConditionCommonMotifRecipe -> when (val condition = recipe.biomeCondition) {
                    is BiomeCondition.BiomeKey -> text { translate(condition.biome.location().toLanguageKey("biome")) }
                    is BiomeCondition.BiomeTag -> text { condition.biomeTag.location().path() }
                }
            }
            view += TextView(recipeText).configure {
                position.alignmentY = Alignment.CENTER
                position.weight = 1.0
                view.sizingX = Sizing.FILL
                view.color = ColorPair.DARK_GRAY
                view.shadow = false
                view.scroll = true
                when (val recipe = recipeEntry.recipe) {
                    is AlwaysCommonMotifRecipe -> Unit
                    is BiomeConditionCommonMotifRecipe -> when (val condition = recipe.biomeCondition) {
                        is BiomeCondition.BiomeKey -> Unit
                        is BiomeCondition.BiomeTag -> view.tooltip = listOf(text { condition.biomeTag.location().string() })
                    }
                }
            }
            view += XSpaceView(2)
            view += OutputSlotView(recipeEntry.recipe.motif.createFairyItemStack())
        }
    }
}
