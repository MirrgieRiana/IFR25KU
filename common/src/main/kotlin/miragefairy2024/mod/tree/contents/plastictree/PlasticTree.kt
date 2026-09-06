package miragefairy2024.mod.tree.contents.plastictree

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.tree.TreeConfiguration
import miragefairy2024.mod.tree.contents.haimeviska.GIANT_HAIMEVISKA_CONFIGURED_FEATURE_KEY
import miragefairy2024.mod.tree.contents.haimeviska.HAIMEVISKA_BLOCK_SET_TYPE
import miragefairy2024.mod.tree.contents.haimeviska.HAIMEVISKA_WOOD_TYPE
import miragefairy2024.mod.tree.contents.haimeviska.SMALL_HAIMEVISKA_CONFIGURED_FEATURE_KEY
import miragefairy2024.util.EnJa
import miragefairy2024.util.Registration
import miragefairy2024.util.enJa
import miragefairy2024.util.generator
import miragefairy2024.util.register
import miragefairy2024.util.registerChild
import miragefairy2024.util.toBlockTag
import miragefairy2024.util.toItemTag
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.world.level.material.MapColor

val PLASTIC_TREE_TREE_CONFIGURATION = object : TreeConfiguration {
    override fun getWoodMapColor() = MapColor.COLOR_YELLOW
    override fun getPlankMapColor() = MapColor.SAND
    override fun getBlockTag() = PLASTIC_TREE_LOGS_BLOCK_TAG
    override fun getItemTag() = PLASTIC_TREE_LOGS_ITEM_TAG
    override fun getBlockSetType() = HAIMEVISKA_BLOCK_SET_TYPE // TODO プラノキの板材がまだ無いから、ハイメヴィスカのものをプレースホルダーとして置いてあるのだ～🌱
    override fun getWoodType() = HAIMEVISKA_WOOD_TYPE // TODO プラノキの板材がまだ無いから、ハイメヴィスカのものをプレースホルダーとして置いてあるのだ～🌱
    override fun getTreeGrowerName() = MirageFairy2024.identifier("plastic_tree")
    override fun getGiantTree() = GIANT_HAIMEVISKA_CONFIGURED_FEATURE_KEY // TODO プラノキの樹木がまだ無いから、ハイメヴィスカのものをプレースホルダーとして置いてあるのだ～🌱
    override fun getSmallTree() = SMALL_HAIMEVISKA_CONFIGURED_FEATURE_KEY // TODO プラノキの樹木がまだ無いから、ハイメヴィスカのものをプレースホルダーとして置いてあるのだ～🌱
}

val PLASTIC_TREE_LOGS_BLOCK_TAG = MirageFairy2024.identifier("plastic_tree_logs").toBlockTag()
val PLASTIC_TREE_LOGS_ITEM_TAG = MirageFairy2024.identifier("plastic_tree_logs").toItemTag()

context(ModContext)
fun initPlasticTree() {

    // BlockType
    Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("plastic_tree_log")) { PlasticTreeLogBlock.CODEC }.register()


    // タグ
    PLASTIC_TREE_LOGS_BLOCK_TAG.enJa(EnJa("Plastic Tree Logs", "プラノキの原木"))
    PLASTIC_TREE_LOGS_ITEM_TAG.enJa(EnJa("Plastic Tree Logs", "プラノキの原木"))
    BlockTags.LOGS_THAT_BURN.generator.registerChild(PLASTIC_TREE_LOGS_BLOCK_TAG)
    ItemTags.LOGS_THAT_BURN.generator.registerChild(PLASTIC_TREE_LOGS_ITEM_TAG)

}
