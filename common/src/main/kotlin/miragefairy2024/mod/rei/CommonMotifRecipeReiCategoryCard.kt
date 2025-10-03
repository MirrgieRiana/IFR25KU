package miragefairy2024.mod.rei

import me.shedaniel.rei.api.common.display.basic.BasicDisplay
import miragefairy2024.mod.fairy.AlwaysCommonMotifRecipe
import miragefairy2024.mod.fairy.BiomeCommonMotifRecipe
import miragefairy2024.mod.fairy.BiomeTagCommonMotifRecipe
import miragefairy2024.mod.fairy.CommonMotifRecipe
import miragefairy2024.mod.fairy.createFairyItemStack
import miragefairy2024.mod.fairy.getIdentifier
import miragefairy2024.mod.fairy.motifRegistry
import miragefairy2024.util.get
import miragefairy2024.util.string
import miragefairy2024.util.toBiomeTag
import miragefairy2024.util.toEntryIngredient
import miragefairy2024.util.toIdentifier
import miragefairy2024.util.with
import miragefairy2024.util.wrapper
import mirrg.kotlin.helium.Single
import net.minecraft.core.registries.Registries

object CommonMotifRecipeReiCategoryCard : ReiCategoryCard<CommonMotifRecipeReiCategoryCard.Display>("common_motif_recipe", "Common Fairy", "コモン妖精") {
    override val serializer: Single<BasicDisplay.Serializer<Display>> by lazy {
        Single(BasicDisplay.Serializer.ofRecipeLess({ _, _, tag ->
            Display(run {
                val motif = motifRegistry.get(tag.wrapper["Motif"].string.get()!!.toIdentifier())!!
                when (val type = tag.wrapper["Type"].string.get()) {
                    "always" -> AlwaysCommonMotifRecipe(motif)
                    "biome" -> BiomeCommonMotifRecipe(motif, tag.wrapper["Biome"].string.get()!!.let { Registries.BIOME with it.toIdentifier() })
                    "biome_tag" -> BiomeTagCommonMotifRecipe(motif, tag.wrapper["BiomeTag"].string.get()!!.toIdentifier().toBiomeTag())
                    else -> throw IllegalArgumentException(type)
                }
            })
        }, { display, tag ->
            tag.wrapper["Motif"].string.set(display.recipe.motif.getIdentifier()!!.string)
            when (display.recipe) {
                is AlwaysCommonMotifRecipe -> {
                    tag.wrapper["Type"].string.set("always")
                }

                is BiomeCommonMotifRecipe -> {
                    tag.wrapper["Type"].string.set("biome")
                    tag.wrapper["Biome"].string.set(display.recipe.biome.location().string)
                }

                is BiomeTagCommonMotifRecipe -> {
                    tag.wrapper["Type"].string.set("biome_tag")
                    tag.wrapper["BiomeTag"].string.set(display.recipe.biomeTag.location().string)
                }
            }
        }))
    }

    class Display(val recipe: CommonMotifRecipe) : BasicDisplay(
        listOf(),
        listOf(recipe.motif.createFairyItemStack()).map { it.toEntryIngredient() },
    ) {
        override fun getCategoryIdentifier() = identifier.first
    }
}
