package miragefairy2024.mod.tree.contents.haimeviska

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.common.rootAdvancement
import miragefairy2024.mod.tree.TreeBlockCard
import miragefairy2024.mod.tree.TreeConfiguration
import miragefairy2024.util.AdvancementCard
import miragefairy2024.util.AdvancementCardType
import miragefairy2024.util.EnJa
import miragefairy2024.util.Registration
import miragefairy2024.util.count
import miragefairy2024.util.createItemStack
import miragefairy2024.util.enJa
import miragefairy2024.util.generator
import miragefairy2024.util.per
import miragefairy2024.util.placeWhenVegetalDecoration
import miragefairy2024.util.plus
import miragefairy2024.util.register
import miragefairy2024.util.registerChild
import miragefairy2024.util.registerConfiguredFeature
import miragefairy2024.util.registerPlacedFeature
import miragefairy2024.util.toBlockTag
import miragefairy2024.util.toItemTag
import miragefairy2024.util.tree
import miragefairy2024.util.unaryPlus
import miragefairy2024.util.with
import net.fabricmc.fabric.api.`object`.builder.v1.block.type.BlockSetTypeBuilder
import net.fabricmc.fabric.api.`object`.builder.v1.block.type.WoodTypeBuilder
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.util.valueproviders.ConstantInt
import net.minecraft.world.level.block.state.properties.BlockSetType
import net.minecraft.world.level.block.state.properties.WoodType
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider
import net.minecraft.world.level.levelgen.feature.treedecorators.LeaveVineDecorator
import net.minecraft.world.level.levelgen.feature.treedecorators.TrunkVineDecorator
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration as TreeConfiguration2

val HAIMEVISKA_TREE_CONFIGURATION = object : TreeConfiguration {
    override fun getWoodMapColor() = MapColor.TERRACOTTA_ORANGE
    override fun getPlankMapColor() = MapColor.RAW_IRON
    override fun getBlockTag() = HAIMEVISKA_LOGS_BLOCK_TAG
    override fun getItemTag() = HAIMEVISKA_LOGS_ITEM_TAG
    override fun getBlockSetType() = HAIMEVISKA_BLOCK_SET_TYPE
    override fun getWoodType() = HAIMEVISKA_WOOD_TYPE
    override fun getTreeGrowerName() = MirageFairy2024.identifier("haimeviska")
    override fun getGiantTree() = GIANT_HAIMEVISKA_CONFIGURED_FEATURE_KEY
    override fun getSmallTree() = SMALL_HAIMEVISKA_CONFIGURED_FEATURE_KEY
}

lateinit var HAIMEVISKA_BLOCK_SET_TYPE: BlockSetType
lateinit var HAIMEVISKA_WOOD_TYPE: WoodType

val HAIMEVISKA_LOGS_BLOCK_TAG = MirageFairy2024.identifier("haimeviska_logs").toBlockTag()
val HAIMEVISKA_LOGS_ITEM_TAG = MirageFairy2024.identifier("haimeviska_logs").toItemTag()

val SMALL_HAIMEVISKA_CONFIGURED_FEATURE_KEY = Registries.CONFIGURED_FEATURE with MirageFairy2024.identifier("small_haimeviska")
val SMALL_HAIMEVISKA_PLACED_FEATURE_KEY = Registries.PLACED_FEATURE with MirageFairy2024.identifier("small_haimeviska")
val SMALL_HAIMEVISKA_FAIRY_FOREST_PLACED_FEATURE_KEY = Registries.PLACED_FEATURE with MirageFairy2024.identifier("small_haimeviska_fairy_forest")

val GIANT_HAIMEVISKA_CONFIGURED_FEATURE_KEY = Registries.CONFIGURED_FEATURE with MirageFairy2024.identifier("giant_haimeviska")
val GIANT_HAIMEVISKA_PLACED_FEATURE_KEY = Registries.PLACED_FEATURE with MirageFairy2024.identifier("giant_haimeviska")
val GIANT_HAIMEVISKA_FAIRY_FOREST_PLACED_FEATURE_KEY = Registries.PLACED_FEATURE with MirageFairy2024.identifier("giant_haimeviska_fairy_forest")
val GIANT_HAIMEVISKA_DEEP_FAIRY_FOREST_PLACED_FEATURE_KEY = Registries.PLACED_FEATURE with MirageFairy2024.identifier("giant_haimeviska_deep_fairy_forest")

val haimeviskaAdvancement = AdvancementCard(
    identifier = MirageFairy2024.identifier("haimeviska"),
    context = AdvancementCard.Sub { rootAdvancement.await() },
    icon = { TreeBlockCard.DRIPPING_LOG.item().createItemStack() },
    name = EnJa("What is it like to be a plant?", "植物として生きるとはどのようなことか"),
    description = EnJa("Explore the overworld to find Haimeviska the fairy tree", "地上を探検して精樹ハイメヴィスカを探す"),
    criterion = AdvancementCard.hasItem { TreeBlockCard.LOG.item() },
    type = AdvancementCardType.TOAST_AND_JEWELS,
)

context(ModContext)
fun initHaimeviska() {

    // BlockType
    Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("haimeviska_leaves")) { HaimeviskaLeavesBlock.CODEC }.register()
    Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("haimeviska_log")) { HaimeviskaLogBlock.CODEC }.register()
    Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("incised_haimeviska_log")) { IncisedHaimeviskaLogBlock.CODEC }.register()
    Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("dripping_haimeviska_log")) { DrippingHaimeviskaLogBlock.CODEC }.register()
    Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("hollow_haimeviska_log")) { HollowHaimeviskaLogBlock.CODEC }.register()


    // 木
    Registration(BuiltInRegistries.TRUNK_PLACER_TYPE, GiantHaimeviskaTrunkPlacerCard.identifier) { GiantHaimeviskaTrunkPlacerCard.type }.register()
    Registration(BuiltInRegistries.TRUNK_PLACER_TYPE, SmallHaimeviskaTrunkPlacerCard.identifier) { SmallHaimeviskaTrunkPlacerCard.type }.register()
    Registration(BuiltInRegistries.FOLIAGE_PLACER_TYPE, GiantHaimeviskaFoliagePlacerCard.identifier) { GiantHaimeviskaFoliagePlacerCard.type }.register()
    Registration(BuiltInRegistries.FOLIAGE_PLACER_TYPE, SmallHaimeviskaFoliagePlacerCard.identifier) { SmallHaimeviskaFoliagePlacerCard.type }.register()
    Registration(BuiltInRegistries.TREE_DECORATOR_TYPE, HaimeviskaTreeDecoratorCard.identifier) { HaimeviskaTreeDecoratorCard.type }.register()

    // WoodType
    HAIMEVISKA_BLOCK_SET_TYPE = BlockSetTypeBuilder().register(MirageFairy2024.identifier("haimeviska"))
    HAIMEVISKA_WOOD_TYPE = WoodTypeBuilder().register(MirageFairy2024.identifier("haimeviska"), HAIMEVISKA_BLOCK_SET_TYPE)


    // タグ
    HAIMEVISKA_LOGS_BLOCK_TAG.enJa(EnJa("Haimeviska Logs", "ハイメヴィスカの原木"))
    HAIMEVISKA_LOGS_ITEM_TAG.enJa(EnJa("Haimeviska Logs", "ハイメヴィスカの原木"))
    BlockTags.LOGS_THAT_BURN.generator.registerChild(HAIMEVISKA_LOGS_BLOCK_TAG)
    ItemTags.LOGS_THAT_BURN.generator.registerChild(HAIMEVISKA_LOGS_ITEM_TAG)


    // 地形生成
    Feature.TREE.generator(MirageFairy2024.identifier("small_haimeviska")) {
        registerConfiguredFeature(SMALL_HAIMEVISKA_CONFIGURED_FEATURE_KEY) {
            TreeConfiguration2.TreeConfigurationBuilder(
                BlockStateProvider.simple(TreeBlockCard.LOG.block()),
                SmallHaimeviskaTrunkPlacer,
                BlockStateProvider.simple(TreeBlockCard.LEAVES.block()),
                SmallHaimeviskaFoliagePlacer(ConstantInt.of(1), ConstantInt.of(0), 0),
                TwoLayersFeatureSize(1, 0, 1),
            ).ignoreVines().decorators(listOf(HaimeviskaTreeDecorator, TrunkVineDecorator.INSTANCE, LeaveVineDecorator(0.05F))).build()
        }.generator {

            // まばら
            registerPlacedFeature(SMALL_HAIMEVISKA_PLACED_FEATURE_KEY) { per(1024) + tree(TreeBlockCard.SAPLING.block()) }.placeWhenVegetalDecoration { +ConventionalBiomeTags.IS_PLAINS + +ConventionalBiomeTags.IS_FOREST } // 平原・森林バイオームに配置

            // 高密度
            registerPlacedFeature(SMALL_HAIMEVISKA_FAIRY_FOREST_PLACED_FEATURE_KEY) { per(16) + tree(TreeBlockCard.SAPLING.block()) }

        }
    }
    Feature.TREE.generator(MirageFairy2024.identifier("giant_haimeviska")) {
        registerConfiguredFeature(GIANT_HAIMEVISKA_CONFIGURED_FEATURE_KEY) {
            TreeConfiguration2.TreeConfigurationBuilder(
                BlockStateProvider.simple(TreeBlockCard.LOG.block()),
                GiantHaimeviskaTrunkPlacer,
                BlockStateProvider.simple(TreeBlockCard.LEAVES.block()),
                GiantHaimeviskaFoliagePlacer,
                TwoLayersFeatureSize(1, 1, 2),
            ).ignoreVines().decorators(listOf(HaimeviskaTreeDecorator, TrunkVineDecorator.INSTANCE, LeaveVineDecorator(0.05F))).build()
        }.generator {

            // まばら
            registerPlacedFeature(GIANT_HAIMEVISKA_PLACED_FEATURE_KEY) { per(1024) + tree(TreeBlockCard.SAPLING.block()) }.placeWhenVegetalDecoration { +ConventionalBiomeTags.IS_PLAINS + +ConventionalBiomeTags.IS_FOREST } // 平原・森林バイオームに配置

            // 高密度
            registerPlacedFeature(GIANT_HAIMEVISKA_FAIRY_FOREST_PLACED_FEATURE_KEY) { per(16) + tree(TreeBlockCard.SAPLING.block()) }

            // 超高密度
            registerPlacedFeature(GIANT_HAIMEVISKA_DEEP_FAIRY_FOREST_PLACED_FEATURE_KEY) { count(8) + tree(TreeBlockCard.SAPLING.block()) }

        }
    }


    // 進捗
    haimeviskaAdvancement.init()

}
