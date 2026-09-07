package miragefairy2024.mod.tree.contents.plastictree

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.tree.TreeBlockCard
import miragefairy2024.mod.tree.TreeConfiguration
import miragefairy2024.mod.tree.contents.haimeviska.GiantHaimeviskaFoliagePlacer
import miragefairy2024.mod.tree.contents.haimeviska.HAIMEVISKA_BLOCK_SET_TYPE
import miragefairy2024.mod.tree.contents.haimeviska.HAIMEVISKA_WOOD_TYPE
import miragefairy2024.mod.tree.contents.haimeviska.SmallHaimeviskaFoliagePlacer
import miragefairy2024.util.EnJa
import miragefairy2024.util.Registration
import miragefairy2024.util.enJa
import miragefairy2024.util.generator
import miragefairy2024.util.register
import miragefairy2024.util.registerChild
import miragefairy2024.util.registerConfiguredFeature
import miragefairy2024.util.toBlockTag
import miragefairy2024.util.toItemTag
import miragefairy2024.util.with
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.util.valueproviders.ConstantInt
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration as TreeConfiguration2

val PLASTIC_TREE_TREE_CONFIGURATION = object : TreeConfiguration {
    override fun getWoodMapColor() = MapColor.COLOR_YELLOW
    override fun getPlankMapColor() = MapColor.SAND
    override fun getBlockTag() = PLASTIC_TREE_LOGS_BLOCK_TAG
    override fun getItemTag() = PLASTIC_TREE_LOGS_ITEM_TAG
    override fun getBlockSetType() = HAIMEVISKA_BLOCK_SET_TYPE // TODO プラノキの板材がまだ無いから、ハイメヴィスカのものをプレースホルダーとして置いてあるのだ～🌱
    override fun getWoodType() = HAIMEVISKA_WOOD_TYPE // TODO プラノキの板材がまだ無いから、ハイメヴィスカのものをプレースホルダーとして置いてあるのだ～🌱
    override fun getTreeGrowerName() = MirageFairy2024.identifier("plastic_tree")
    override fun getGiantTree() = GIANT_PLASTIC_TREE_CONFIGURED_FEATURE_KEY
    override fun getSmallTree() = SMALL_PLASTIC_TREE_CONFIGURED_FEATURE_KEY
}

val PLASTIC_TREE_LOGS_BLOCK_TAG = MirageFairy2024.identifier("plastic_tree_logs").toBlockTag()
val PLASTIC_TREE_LOGS_ITEM_TAG = MirageFairy2024.identifier("plastic_tree_logs").toItemTag()

val SMALL_PLASTIC_TREE_CONFIGURED_FEATURE_KEY = Registries.CONFIGURED_FEATURE with MirageFairy2024.identifier("small_plastic_tree")

val GIANT_PLASTIC_TREE_CONFIGURED_FEATURE_KEY = Registries.CONFIGURED_FEATURE with MirageFairy2024.identifier("giant_plastic_tree")

context(ModContext)
fun initPlasticTree() {

    // BlockType
    Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("plastic_tree_log")) { PlasticTreeLogBlock.CODEC }.register()
    Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("incised_plastic_tree_log")) { IncisedPlasticTreeLogBlock.CODEC }.register()
    Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("dripping_plastic_tree_log")) { DrippingPlasticTreeLogBlock.CODEC }.register()


    // 木
    Registration(BuiltInRegistries.TRUNK_PLACER_TYPE, GiantPlasticTreeTrunkPlacerCard.identifier) { GiantPlasticTreeTrunkPlacerCard.type }.register()
    Registration(BuiltInRegistries.TRUNK_PLACER_TYPE, SmallPlasticTreeTrunkPlacerCard.identifier) { SmallPlasticTreeTrunkPlacerCard.type }.register()
    Registration(BuiltInRegistries.TREE_DECORATOR_TYPE, PlasticTreeTreeDecoratorCard.identifier) { PlasticTreeTreeDecoratorCard.type }.register()


    // タグ
    PLASTIC_TREE_LOGS_BLOCK_TAG.enJa(EnJa("Plastic Tree Logs", "プラノキの原木"))
    PLASTIC_TREE_LOGS_ITEM_TAG.enJa(EnJa("Plastic Tree Logs", "プラノキの原木"))
    BlockTags.LOGS_THAT_BURN.generator.registerChild(PLASTIC_TREE_LOGS_BLOCK_TAG)
    ItemTags.LOGS_THAT_BURN.generator.registerChild(PLASTIC_TREE_LOGS_ITEM_TAG)


    // 地形生成
    Feature.TREE.generator(MirageFairy2024.identifier("small_plastic_tree")) {
        registerConfiguredFeature(SMALL_PLASTIC_TREE_CONFIGURED_FEATURE_KEY) {
            TreeConfiguration2.TreeConfigurationBuilder(
                BlockStateProvider.simple(TreeBlockCard.PLASTIC_TREE_LOG.block()),
                SmallPlasticTreeTrunkPlacer,
                BlockStateProvider.simple(TreeBlockCard.PLASTIC_TREE_LEAVES.block()),
                SmallHaimeviskaFoliagePlacer(ConstantInt.of(1), ConstantInt.of(0), 0),
                TwoLayersFeatureSize(1, 0, 1),
            ).ignoreVines().decorators(listOf(PlasticTreeTreeDecorator)).build()
        }
    }
    Feature.TREE.generator(MirageFairy2024.identifier("giant_plastic_tree")) {
        registerConfiguredFeature(GIANT_PLASTIC_TREE_CONFIGURED_FEATURE_KEY) {
            TreeConfiguration2.TreeConfigurationBuilder(
                BlockStateProvider.simple(TreeBlockCard.PLASTIC_TREE_LOG.block()),
                GiantPlasticTreeTrunkPlacer,
                BlockStateProvider.simple(TreeBlockCard.PLASTIC_TREE_LEAVES.block()),
                GiantHaimeviskaFoliagePlacer,
                TwoLayersFeatureSize(1, 1, 2),
            ).ignoreVines().decorators(listOf(PlasticTreeTreeDecorator)).build()
        }
    }

}
