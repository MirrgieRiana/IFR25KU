package miragefairy2024.mod.rei

import me.shedaniel.rei.api.common.display.basic.BasicDisplay
import miragefairy2024.mod.fairy.Motif
import miragefairy2024.mod.fairy.createFairyItemStack
import miragefairy2024.mod.fairy.getIdentifier
import miragefairy2024.mod.fairy.toFairyMotif
import miragefairy2024.util.get
import miragefairy2024.util.list
import miragefairy2024.util.string
import miragefairy2024.util.toEntryIngredient
import miragefairy2024.util.toIdentifier
import miragefairy2024.util.toNbtList
import miragefairy2024.util.toNbtString
import miragefairy2024.util.wrapper
import mirrg.kotlin.helium.Single

object FairyFamilyReiCategoryCard : ReiCategoryCard<FairyFamilyReiCategoryCard.Display>("fairy_family", "Fairy Family", "妖精系統") {
    override val serializer: Single<BasicDisplay.Serializer<Display>> by lazy {
        Single(BasicDisplay.Serializer.ofRecipeLess({ _, _, tag ->
            Display(
                tag.wrapper["Motif"].string.get()!!.toIdentifier().toFairyMotif()!!,
                tag.wrapper["Parents"].list.get()!!.map { it.wrapper.string.get()!!.toIdentifier().toFairyMotif()!! },
                tag.wrapper["Children"].list.get()!!.map { it.wrapper.string.get()!!.toIdentifier().toFairyMotif()!! },
            )
        }, { display, tag ->
            tag.wrapper["Motif"].string.set(display.motif.getIdentifier()!!.string)
            tag.wrapper["Parents"].list.set(display.parents.map { it.getIdentifier()!!.string.toNbtString() }.toNbtList())
            tag.wrapper["Children"].list.set(display.children.map { it.getIdentifier()!!.string.toNbtString() }.toNbtList())
        }))
    }

    class Display(val motif: Motif, val parents: List<Motif>, val children: List<Motif>) : BasicDisplay(
        listOf(motif).map { it.createFairyItemStack().toEntryIngredient() },
        (parents + children).map { it.createFairyItemStack().toEntryIngredient() },
    ) {
        override fun getCategoryIdentifier() = identifier.first
    }
}
