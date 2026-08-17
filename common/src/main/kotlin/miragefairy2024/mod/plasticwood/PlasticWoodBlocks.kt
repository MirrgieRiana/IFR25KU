package miragefairy2024.mod.plasticwood

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.poem
import miragefairy2024.mod.wood.WoodBlockCard
import miragefairy2024.mod.wood.WoodBlockConfiguration
import miragefairy2024.mod.wood.cards.WoodHorizontalFacingLogBlockCard
import miragefairy2024.mod.plasticwood.cards.PlasticTreeLeavesBlockCard
import miragefairy2024.mod.wood.cards.WoodLogBlockCard
import miragefairy2024.mod.wood.cards.WoodSaplingBlockCard
import miragefairy2024.util.EnJa
import miragefairy2024.util.enJa
import miragefairy2024.util.generator
import miragefairy2024.util.registerChild
import miragefairy2024.util.string
import miragefairy2024.util.toBlockTag
import miragefairy2024.util.toItemTag
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.world.level.block.grower.TreeGrower
import net.minecraft.world.level.material.MapColor
import java.util.Optional

object PlasticWoodBlockCard {
    val entries = mutableListOf<WoodBlockCard>()
    private operator fun WoodBlockCard.not() = apply { entries += this }

    val LEAVES = !WoodBlockConfiguration(
        "plastic_tree_leaves", EnJa("Plastic Tree Leaves", "プラノキの葉"),
        PoemList(1).poem(EnJa("TODO", "TODO")),
    ).let { PlasticTreeLeavesBlockCard(it) }
    val LOG = !WoodBlockConfiguration(
        "plastic_tree_log", EnJa("Plastic Tree Log", "プラノキの原木"),
        PoemList(1).poem(EnJa("TODO", "TODO")),
    ).let { WoodLogBlockCard(it, PLASTIC_TREE_LOGS_BLOCK_TAG, PLASTIC_TREE_LOGS_ITEM_TAG, MapColor.SAND, MapColor.COLOR_YELLOW) }
    val DRIPPING_LOG = !WoodBlockConfiguration(
        "dripping_plastic_tree_log", EnJa("Dripping Plastic Tree Log", "樹液が滴るプラノキの原木"),
        PoemList(1).poem(EnJa("TODO", "TODO")),
    ).let { WoodHorizontalFacingLogBlockCard(it, { LOG }, PLASTIC_TREE_LOGS_BLOCK_TAG, PLASTIC_TREE_LOGS_ITEM_TAG, MapColor.COLOR_YELLOW) }
    val SAPLING = !WoodBlockConfiguration(
        "plastic_tree_sapling", EnJa("Plastic Tree Sapling", "プラノキの苗木"),
        PoemList(1).poem(EnJa("TODO", "TODO")),
    ).let { WoodSaplingBlockCard(it) { TreeGrower(MirageFairy2024.identifier("plastic_tree").string, Optional.of(PLASTIC_TREE_CONFIGURED_FEATURE_KEY), Optional.empty(), Optional.empty()) } } // 2x2に苗木を植えないと育たない仕様にするため、megaTreeに設定するのだ
}


val PLASTIC_TREE_LOGS_BLOCK_TAG = MirageFairy2024.identifier("plastic_tree_logs").toBlockTag()
val PLASTIC_TREE_LOGS_ITEM_TAG = MirageFairy2024.identifier("plastic_tree_logs").toItemTag()

context(ModContext)
fun initPlasticWoodBlocks() {

    PlasticWoodBlockCard.entries.forEach { card ->
        card.init()
    }

    // タグ
    PLASTIC_TREE_LOGS_BLOCK_TAG.enJa(EnJa("Plastic Tree Logs", "プラノキの原木"))
    PLASTIC_TREE_LOGS_ITEM_TAG.enJa(EnJa("Plastic Tree Logs", "プラノキの原木"))
    BlockTags.LOGS_THAT_BURN.generator.registerChild(PLASTIC_TREE_LOGS_BLOCK_TAG)
    ItemTags.LOGS_THAT_BURN.generator.registerChild(PLASTIC_TREE_LOGS_ITEM_TAG)

}
