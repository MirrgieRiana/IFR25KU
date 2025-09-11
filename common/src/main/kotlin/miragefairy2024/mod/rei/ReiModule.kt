package miragefairy2024.mod.rei

import me.shedaniel.rei.api.common.category.CategoryIdentifier
import me.shedaniel.rei.api.common.display.basic.BasicDisplay
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import mirrg.kotlin.helium.Single

abstract class ReiCategoryCard<D : BasicDisplay>(
    val path: String,
    enName: String,
    jaName: String,
) {
    companion object {
        val entries = listOf(
            TraitEncyclopediaReiCategoryCard,
            HarvestReiCategoryCard,
            FairyQuestRecipeReiCategoryCard,
            CommonMotifRecipeReiCategoryCard,
            ItemFairyDreamRecipeReiCategoryCard,
            BlockFairyDreamRecipeReiCategoryCard,
            EntityTypeFairyDreamRecipeReiCategoryCard,
            FairyFamilyReiCategoryCard,
            FermentationBarrelReiCategoryCard,
            AuraReflectorFurnaceReiCategoryCard,
        )
    }

    val translation = Translation({ "category.rei.${MirageFairy2024.identifier(path).toLanguageKey()}" }, enName, jaName)

    // Singleを取り除くとREI無しで起動するとクラッシュする
    val identifier: Single<CategoryIdentifier<D>> by lazy { Single(CategoryIdentifier.of(MirageFairy2024.MOD_ID, "plugins/$path")) }
    abstract val serializer: Single<BasicDisplay.Serializer<D>>
}

val COMMON_MOTIF_RECIPE_ALWAYS_TRANSLATION = Translation({ "gui.${MirageFairy2024.identifier("common_motif_recipe").toLanguageKey()}.always" }, "Always", "常時")

context(ModContext)
fun initReiModule() {
    ReiCategoryCard.entries.forEach { card ->
        card.translation.enJa()
    }

    COMMON_MOTIF_RECIPE_ALWAYS_TRANSLATION.enJa()
}
