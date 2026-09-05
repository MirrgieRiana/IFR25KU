package miragefairy2024.mod.tree.contents.plastictree

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.tree.TreeConfiguration
import miragefairy2024.mod.tree.contents.haimeviska.GIANT_HAIMEVISKA_CONFIGURED_FEATURE_KEY
import miragefairy2024.mod.tree.contents.haimeviska.HAIMEVISKA_BLOCK_SET_TYPE
import miragefairy2024.mod.tree.contents.haimeviska.HAIMEVISKA_LOGS_BLOCK_TAG
import miragefairy2024.mod.tree.contents.haimeviska.HAIMEVISKA_LOGS_ITEM_TAG
import miragefairy2024.mod.tree.contents.haimeviska.HAIMEVISKA_WOOD_TYPE
import miragefairy2024.mod.tree.contents.haimeviska.SMALL_HAIMEVISKA_CONFIGURED_FEATURE_KEY
import miragefairy2024.util.Registration
import miragefairy2024.util.register
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.level.material.MapColor

val PLASTIC_TREE_TREE_CONFIGURATION = object : TreeConfiguration {
    override fun getWoodMapColor() = MapColor.COLOR_YELLOW
    override fun getPlankMapColor() = MapColor.SAND
    override fun getBlockTag() = HAIMEVISKA_LOGS_BLOCK_TAG // プラノキの原木がまだ無いから、ハイメヴィスカのものをプレースホルダーとして置いてあるのだ～🌱
    override fun getItemTag() = HAIMEVISKA_LOGS_ITEM_TAG // プラノキの原木がまだ無いから、ハイメヴィスカのものをプレースホルダーとして置いてあるのだ～🌱
    override fun getBlockSetType() = HAIMEVISKA_BLOCK_SET_TYPE // プラノキの板材がまだ無いから、ハイメヴィスカのものをプレースホルダーとして置いてあるのだ～🌱
    override fun getWoodType() = HAIMEVISKA_WOOD_TYPE // プラノキの板材がまだ無いから、ハイメヴィスカのものをプレースホルダーとして置いてあるのだ～🌱
    override fun getTreeGrowerName() = MirageFairy2024.identifier("plastic_tree")
    override fun getGiantTree() = GIANT_HAIMEVISKA_CONFIGURED_FEATURE_KEY // プラノキの樹木がまだ無いから、ハイメヴィスカのものをプレースホルダーとして置いてあるのだ～🌱
    override fun getSmallTree() = SMALL_HAIMEVISKA_CONFIGURED_FEATURE_KEY // プラノキの樹木がまだ無いから、ハイメヴィスカのものをプレースホルダーとして置いてあるのだ～🌱
}

context(ModContext)
fun initPlasticTree() {

    // BlockType
    Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("incised_plastic_tree_log")) { IncisedPlasticTreeLogBlock.CODEC }.register()

}
