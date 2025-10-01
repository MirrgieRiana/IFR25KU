package miragefairy2024.mod.rei

import me.shedaniel.rei.api.common.category.CategoryIdentifier
import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry
import me.shedaniel.rei.api.common.display.basic.BasicDisplay
import me.shedaniel.rei.api.common.entry.comparison.ItemComparatorRegistry
import miragefairy2024.InitializationEventRegistry
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.recipeviewer.HarvestReiCategoryCard
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import mirrg.kotlin.helium.Single

object ReiEvents {
    val onRegisterDisplaySerializer = InitializationEventRegistry<(DisplaySerializerRegistry) -> Unit>()
    val onRegisterItemComparators = InitializationEventRegistry<(ItemComparatorRegistry) -> Unit>()
}

abstract class ReiCategoryCard<D : BasicDisplay>(
    val path: String,
    enName: String,
    jaName: String,
) {
    val translation = Translation({ "category.rei.${MirageFairy2024.identifier(path).toLanguageKey()}" }, enName, jaName)

    // Singleを取り除くとREI無しで起動するとクラッシュする
    val identifier: Single<CategoryIdentifier<D>> by lazy { Single(CategoryIdentifier.of(MirageFairy2024.MOD_ID, "plugins/$path")) }
    abstract val serializer: Single<BasicDisplay.Serializer<D>>

    context(ModContext)
    fun init() {
        translation.enJa()
        ReiEvents.onRegisterDisplaySerializer {
            it.register(identifier.first, serializer.first)
        }
    }
}

val COMMON_MOTIF_RECIPE_ALWAYS_TRANSLATION = Translation({ "gui.${MirageFairy2024.identifier("common_motif_recipe").toLanguageKey()}.always" }, "Always", "常時")

context(ModContext)
fun initReiModule() {

    TraitEncyclopediaReiCategoryCard.init()
    HarvestReiCategoryCard.init()
    FairyQuestRecipeReiCategoryCard.init()
    CommonMotifRecipeReiCategoryCard.init()
    ItemFairyDreamRecipeReiCategoryCard.init()
    BlockFairyDreamRecipeReiCategoryCard.init()
    EntityTypeFairyDreamRecipeReiCategoryCard.init()
    FairyFamilyReiCategoryCard.init()
    FermentationBarrelReiCategoryCard.init()
    AuraReflectorFurnaceReiCategoryCard.init()

    COMMON_MOTIF_RECIPE_ALWAYS_TRANSLATION.enJa()
}
