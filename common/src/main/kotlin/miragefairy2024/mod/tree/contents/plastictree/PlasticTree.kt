package miragefairy2024.mod.tree.contents.plastictree

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.tree.TreeBlockCard
import miragefairy2024.mod.tree.TreeCard
import miragefairy2024.mod.tree.contents.haimeviska.GIANT_HAIMEVISKA_CONFIGURED_FEATURE_KEY
import miragefairy2024.mod.tree.contents.haimeviska.SMALL_HAIMEVISKA_CONFIGURED_FEATURE_KEY
import miragefairy2024.util.EnJa
import miragefairy2024.util.Registration
import miragefairy2024.util.enJa
import miragefairy2024.util.generator
import miragefairy2024.util.per
import miragefairy2024.util.plus
import miragefairy2024.util.register
import miragefairy2024.util.registerChild
import miragefairy2024.util.registerPlacedFeature
import miragefairy2024.util.toBlockTag
import miragefairy2024.util.toItemTag
import miragefairy2024.util.tree
import miragefairy2024.util.with
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration
import net.minecraft.world.level.material.MapColor

val PLASTIC_TREE_CARD = object : TreeCard {
    override fun getWoodMapColor() = MapColor.COLOR_YELLOW
    override fun getPlankMapColor() = MapColor.SAND
    override fun getBlockTag() = PLASTIC_TREE_LOGS_BLOCK_TAG
    override fun getItemTag() = PLASTIC_TREE_LOGS_ITEM_TAG
    override fun getBlockSetType() = TODO() // TODO プラノキの板材がまだ無いから、ハイメヴィスカのものをプレースホルダーとして置いてあるのだ～🌱
    override fun getWoodType() = TODO() // TODO プラノキの板材がまだ無いから、ハイメヴィスカのものをプレースホルダーとして置いてあるのだ～🌱
    override fun getTreeGrowerName() = MirageFairy2024.identifier("plastic_tree")
    override fun getGiantTree() = GIANT_HAIMEVISKA_CONFIGURED_FEATURE_KEY // TODO プラノキの樹木がまだ無いから、ハイメヴィスカのものをプレースホルダーとして置いてあるのだ～🌱
    override fun getSmallTree() = SMALL_HAIMEVISKA_CONFIGURED_FEATURE_KEY // TODO プラノキの樹木がまだ無いから、ハイメヴィスカのものをプレースホルダーとして置いてあるのだ～🌱
}

val PLASTIC_TREE_LOGS_BLOCK_TAG = MirageFairy2024.identifier("plastic_tree_logs").toBlockTag()
val PLASTIC_TREE_LOGS_ITEM_TAG = MirageFairy2024.identifier("plastic_tree_logs").toItemTag()

val SMALL_PLASTIC_TREE_OLD_GROWTH_AMBER_FOREST_PLACED_FEATURE_KEY = Registries.PLACED_FEATURE with MirageFairy2024.identifier("small_plastic_tree_old_growth_amber_forest")

val GIANT_PLASTIC_TREE_OLD_GROWTH_AMBER_FOREST_PLACED_FEATURE_KEY = Registries.PLACED_FEATURE with MirageFairy2024.identifier("giant_plastic_tree_old_growth_amber_forest")

context(ModContext)
fun initPlasticTree() {

    // BlockType
    Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("plastic_tree_log")) { PlasticTreeLogBlock.CODEC }.register()
    Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("incised_plastic_tree_log")) { IncisedPlasticTreeLogBlock.CODEC }.register()
    Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("dripping_plastic_tree_log")) { DrippingPlasticTreeLogBlock.CODEC }.register()


    // タグ
    PLASTIC_TREE_LOGS_BLOCK_TAG.enJa(EnJa("Plastic Tree Logs", "プラノキの原木"))
    PLASTIC_TREE_LOGS_ITEM_TAG.enJa(EnJa("Plastic Tree Logs", "プラノキの原木"))
    BlockTags.LOGS_THAT_BURN.generator.registerChild(PLASTIC_TREE_LOGS_BLOCK_TAG)
    ItemTags.LOGS_THAT_BURN.generator.registerChild(PLASTIC_TREE_LOGS_ITEM_TAG)


    // 地形生成
    Feature.TREE.generator(MirageFairy2024.identifier("small_plastic_tree")) {
        SMALL_HAIMEVISKA_CONFIGURED_FEATURE_KEY.generator<TreeConfiguration> { // TODO プラノキの樹木がまだ無いから、ハイメヴィスカのものをプレースホルダーとして置いてあるのだ～🌱

            // 琥珀色の原生林
            registerPlacedFeature(SMALL_PLASTIC_TREE_OLD_GROWTH_AMBER_FOREST_PLACED_FEATURE_KEY) { per(2) + tree(TreeBlockCard.PLASTIC_TREE_SAPLING.block()) }

        }
    }

    Feature.TREE.generator(MirageFairy2024.identifier("giant_plastic_tree")) {
        GIANT_HAIMEVISKA_CONFIGURED_FEATURE_KEY.generator<TreeConfiguration> { // TODO プラノキの樹木がまだ無いから、ハイメヴィスカのものをプレースホルダーとして置いてあるのだ～🌱

            // 琥珀色の原生林
            registerPlacedFeature(GIANT_PLASTIC_TREE_OLD_GROWTH_AMBER_FOREST_PLACED_FEATURE_KEY) { per(2) + tree(TreeBlockCard.PLASTIC_TREE_SAPLING.block()) }

        }
    }

}
