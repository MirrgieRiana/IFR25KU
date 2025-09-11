package miragefairy2024.mod.rei

import me.shedaniel.rei.api.common.display.basic.BasicDisplay
import miragefairy2024.mod.fairy.Motif
import miragefairy2024.mod.fairy.createFairyItemStack
import miragefairy2024.mod.fairy.getIdentifier
import miragefairy2024.mod.fairy.toFairyMotif
import miragefairy2024.util.createItemStack
import miragefairy2024.util.get
import miragefairy2024.util.getIdentifier
import miragefairy2024.util.list
import miragefairy2024.util.string
import miragefairy2024.util.toBlock
import miragefairy2024.util.toEntityType
import miragefairy2024.util.toEntryIngredient
import miragefairy2024.util.toEntryStack
import miragefairy2024.util.toIdentifier
import miragefairy2024.util.toItem
import miragefairy2024.util.toNbtList
import miragefairy2024.util.toNbtString
import miragefairy2024.util.wrapper
import mirrg.kotlin.helium.Single
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block

object ItemFairyDreamRecipeReiCategoryCard : ReiCategoryCard<ItemFairyDreamRecipeReiCategoryCard.Display>("item_fairy_dream_recipe", "Fairy Dream: Item", "妖精の夢：アイテム") {
    override val serializer: Single<BasicDisplay.Serializer<Display>> by lazy {
        Single(BasicDisplay.Serializer.ofRecipeLess({ _, _, tag ->
            Display(
                tag.wrapper["Items"].list.get()!!.map { item ->
                    item.wrapper.string.get()!!.toIdentifier().toItem()
                },
                tag.wrapper["Motif"].string.get()!!.toIdentifier().toFairyMotif()!!,
            )
        }, { display, tag ->
            tag.wrapper["Items"].set(display.items.map { item ->
                item.getIdentifier().string.toNbtString()
            }.toNbtList())
            tag.wrapper["Motif"].string.set(display.motif.getIdentifier()!!.string)
        }))
    }

    class Display(val items: List<Item>, val motif: Motif) : BasicDisplay(
        listOf(items.map { it.createItemStack().toEntryStack() }.toEntryIngredient()),
        listOf(motif.createFairyItemStack()).map { it.toEntryStack().toEntryIngredient() },
    ) {
        override fun getCategoryIdentifier() = identifier.first
    }
}

object BlockFairyDreamRecipeReiCategoryCard : ReiCategoryCard<BlockFairyDreamRecipeReiCategoryCard.Display>("block_fairy_dream_recipe", "Fairy Dream: Block", "妖精の夢：ブロック") {
    override val serializer: Single<BasicDisplay.Serializer<Display>> by lazy {
        Single(BasicDisplay.Serializer.ofRecipeLess({ _, _, tag ->
            Display(
                tag.wrapper["Blocks"].list.get()!!.map { block ->
                    block.wrapper.string.get()!!.toIdentifier().toBlock()
                },
                tag.wrapper["Motif"].string.get()!!.toIdentifier().toFairyMotif()!!,
            )
        }, { display, tag ->
            tag.wrapper["Blocks"].set(display.blocks.map { block ->
                block.getIdentifier().string.toNbtString()
            }.toNbtList())
            tag.wrapper["Motif"].string.set(display.motif.getIdentifier()!!.string)
        }))
    }

    class Display(val blocks: List<Block>, val motif: Motif) : BasicDisplay(
        listOf(blocks.map { it.asItem().createItemStack().toEntryStack() }.toEntryIngredient()),
        listOf(motif.createFairyItemStack()).map { it.toEntryStack().toEntryIngredient() },
    ) {
        override fun getCategoryIdentifier() = identifier.first
    }
}

object EntityTypeFairyDreamRecipeReiCategoryCard : ReiCategoryCard<EntityTypeFairyDreamRecipeReiCategoryCard.Display>("entity_fairy_dream_recipe", "Fairy Dream: Entity", "妖精の夢：エンティティ") {
    override val serializer: Single<BasicDisplay.Serializer<Display>> by lazy {
        Single(BasicDisplay.Serializer.ofRecipeLess({ _, _, tag ->
            Display(
                tag.wrapper["EntityTypes"].list.get()!!.map { entityType ->
                    entityType.wrapper.string.get()!!.toIdentifier().toEntityType()
                },
                tag.wrapper["Motif"].string.get()!!.toIdentifier().toFairyMotif()!!,
            )
        }, { display, tag ->
            tag.wrapper["EntityTypes"].set(display.entityTypes.map { entityType ->
                entityType.getIdentifier().string.toNbtString()
            }.toNbtList())
            tag.wrapper["Motif"].string.set(display.motif.getIdentifier()!!.string)
        }))
    }

    class Display(val entityTypes: List<EntityType<*>>, val motif: Motif) : BasicDisplay(
        listOf(),
        listOf(motif.createFairyItemStack()).map { it.toEntryStack().toEntryIngredient() },
    ) {
        override fun getCategoryIdentifier() = identifier.first
    }
}
