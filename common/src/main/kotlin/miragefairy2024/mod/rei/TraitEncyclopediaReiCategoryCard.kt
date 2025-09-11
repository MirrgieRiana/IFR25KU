package miragefairy2024.mod.rei

import me.shedaniel.rei.api.common.display.basic.BasicDisplay
import me.shedaniel.rei.api.common.entry.EntryIngredient
import miragefairy2024.mod.magicplant.Trait
import miragefairy2024.mod.magicplant.getIdentifier
import miragefairy2024.mod.magicplant.toTrait
import miragefairy2024.util.string
import miragefairy2024.util.toIdentifier
import mirrg.kotlin.helium.Single

object TraitEncyclopediaReiCategoryCard : ReiCategoryCard<TraitEncyclopediaReiCategoryCard.Display>("trait_encyclopedia", "Trait Encyclopedia", "特性図鑑") {
    override val serializer: Single<BasicDisplay.Serializer<Display>> by lazy {
        Single(BasicDisplay.Serializer.ofRecipeLess({ inputs, _, tag ->
            Display(
                inputs,
                tag.getString("Trait").toIdentifier().toTrait()!!,
            )
        }, { display, tag ->
            tag.putString("Trait", display.trait.getIdentifier().string)
        }))
    }

    class Display(inputs: List<EntryIngredient>, val trait: Trait) : BasicDisplay(inputs, listOf()) {
        override fun getCategoryIdentifier() = identifier.first
    }
}
