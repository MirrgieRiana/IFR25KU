package miragefairy2024.mod.plasticwood

import com.mojang.serialization.MapCodec
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.wood.WoodFoliagePlacer
import miragefairy2024.mod.wood.WoodTrunkPlacer
import miragefairy2024.util.Registration
import miragefairy2024.util.count
import miragefairy2024.util.generator
import miragefairy2024.util.per
import miragefairy2024.util.plus
import miragefairy2024.util.register
import miragefairy2024.util.registerConfiguredFeature
import miragefairy2024.util.registerPlacedFeature
import miragefairy2024.util.tree
import miragefairy2024.util.unaryPlus
import miragefairy2024.util.with
import net.minecraft.core.Direction
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.util.RandomSource
import net.minecraft.world.level.block.HorizontalDirectionalBlock
import net.minecraft.world.level.block.RotatedPillarBlock
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType

// プラノキの TrunkPlacer カードなのだ
object PlasticTreeTrunkPlacerCard {
    val identifier = MirageFairy2024.identifier("plastic_tree")
    private val codec: MapCodec<PlasticTreeTrunkPlacer> = MapCodec.unit { PlasticTreeTrunkPlacer }
    val type: TrunkPlacerType<PlasticTreeTrunkPlacer> = TrunkPlacerType(codec)
}

// プラノキの FoliagePlacer カードなのだ
object PlasticTreeFoliagePlacerCard {
    val identifier = MirageFairy2024.identifier("plastic_tree")
    private val codec: MapCodec<PlasticTreeFoliagePlacer> = MapCodec.unit { PlasticTreeFoliagePlacer }
    val type: FoliagePlacerType<PlasticTreeFoliagePlacer> = FoliagePlacerType(codec)
}

// プラノキの TreeDecorator カードなのだ
object PlasticTreeTreeDecoratorCard {
    val identifier = MirageFairy2024.identifier("plastic_tree")
    val treeDecorator = PlasticTreeTreeDecorator()
    private val codec: MapCodec<PlasticTreeTreeDecorator> = MapCodec.unit { treeDecorator }
    val type: TreeDecoratorType<PlasticTreeTreeDecorator> = TreeDecoratorType(codec)
}

// ConfiguredFeature と PlacedFeature のキーなのだ
val PLASTIC_TREE_CONFIGURED_FEATURE_KEY = Registries.CONFIGURED_FEATURE with MirageFairy2024.identifier("plastic_tree")
val PLASTIC_TREE_PLACED_FEATURE_KEY = Registries.PLACED_FEATURE with MirageFairy2024.identifier("plastic_tree")

// 琥珀色の原生林専用配置キーなのだ（高密度）
val PLASTIC_TREE_AMBER_FOREST_PLACED_FEATURE_KEY = Registries.PLACED_FEATURE with MirageFairy2024.identifier("plastic_tree_amber_forest")

context(ModContext)
fun initPlasticWoodWorldGens() {

    // TrunkPlacerの登録なのだ
    Registration(BuiltInRegistries.TRUNK_PLACER_TYPE, PlasticTreeTrunkPlacerCard.identifier) { PlasticTreeTrunkPlacerCard.type }.register()

    // FoliagePlacerの登録なのだ
    Registration(BuiltInRegistries.FOLIAGE_PLACER_TYPE, PlasticTreeFoliagePlacerCard.identifier) { PlasticTreeFoliagePlacerCard.type }.register()

    // TreeDecoratorの登録なのだ
    Registration(BuiltInRegistries.TREE_DECORATOR_TYPE, PlasticTreeTreeDecoratorCard.identifier) { PlasticTreeTreeDecoratorCard.type }.register()

    // 地形生成
    Feature.TREE.generator(MirageFairy2024.identifier("plastic_tree")) {
        registerConfiguredFeature(PLASTIC_TREE_CONFIGURED_FEATURE_KEY) {
            TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(PlasticWoodBlockCard.LOG.block()),
                PlasticTreeTrunkPlacer, // 2x2の太い幹と、そこから伸びる枝
                BlockStateProvider.simple(PlasticWoodBlockCard.LEAVES.block()),
                PlasticTreeFoliagePlacer, // 枝先の葉の塊
                TwoLayersFeatureSize(1, 1, 2),
            ).ignoreVines().decorators(listOf(PlasticTreeTreeDecoratorCard.treeDecorator)).build()
        }.generator {

            // まばらな配置（将来的に他のバイオームにも生えることがあれば使うのだ）
            registerPlacedFeature(PLASTIC_TREE_PLACED_FEATURE_KEY) { per(1024) + tree(PlasticWoodBlockCard.SAPLING.block()) }

            // 琥珀色の原生林への高密度配置なのだ
            registerPlacedFeature(PLASTIC_TREE_AMBER_FOREST_PLACED_FEATURE_KEY) { count(6) + tree(PlasticWoodBlockCard.SAPLING.block()) }

        }
    }

}

// プラノキの幹なのだ～🌱
object PlasticTreeTrunkPlacer : WoodTrunkPlacer(18, 8, 0) { // 最大26。ハイメヴィスカより少し小さめなのだ
    override fun type() = PlasticTreeTrunkPlacerCard.type

    override fun getNextFoliageAngle(random: RandomSource, angle: Double) = random.nextDouble() * (Math.PI * 2) // ハイメヴィスカと違って螺旋を描かず、葉の向きは毎回ばらばらなのだ
}

// プラノキの葉なのだ～🌱
object PlasticTreeFoliagePlacer : WoodFoliagePlacer() {
    override fun type() = PlasticTreeFoliagePlacerCard.type
}

// プラノキの TreeDecorator なのだ
// 垂直の幹を一定確率で樹液が滴る原木に置き換えるのだ
class PlasticTreeTreeDecorator : TreeDecorator() {
    override fun type() = PlasticTreeTreeDecoratorCard.type
    override fun place(generator: Context) {
        generator.logs().forEach { blockPos ->
            if (!generator.level().isStateAtPosition(blockPos) { it == PlasticWoodBlockCard.LOG.block().defaultBlockState().with(RotatedPillarBlock.AXIS, Direction.Axis.Y) }) return@forEach // 垂直の幹のみなのだ
            val direction = Direction.from2DDataValue(generator.random().nextInt(4))
            if (!generator.isAir(blockPos.relative(direction))) return@forEach // 正面が空気の場合のみなのだ
            if (generator.random().nextInt(100) < 25) {
                generator.setBlock(blockPos, PlasticWoodBlockCard.DRIPPING_LOG.block().defaultBlockState().with(HorizontalDirectionalBlock.FACING, direction))
            }
        }
    }
}
