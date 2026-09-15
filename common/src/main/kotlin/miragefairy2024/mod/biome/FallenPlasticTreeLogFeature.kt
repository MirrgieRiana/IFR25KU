package miragefairy2024.mod.biome

import com.mojang.serialization.Codec
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.tree.TreeBlockCard
import miragefairy2024.mod.tree.contents.plastictree.onResinCementedDirt
import miragefairy2024.util.Registration
import miragefairy2024.util.center
import miragefairy2024.util.flower
import miragefairy2024.util.generator
import miragefairy2024.util.ground
import miragefairy2024.util.per
import miragefairy2024.util.plus
import miragefairy2024.util.register
import miragefairy2024.util.registerConfiguredFeature
import miragefairy2024.util.registerPlacedFeature
import miragefairy2024.util.with
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.RotatedPillarBlock
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration

object FallenPlasticTreeLogFeatureCard {
    val identifier = MirageFairy2024.identifier("fallen_plastic_tree_log")
    val feature = FallenPlasticTreeLogFeature(NoneFeatureConfiguration.CODEC)
    val placedFeatureKey = Registries.PLACED_FEATURE with identifier

    context(ModContext)
    fun init() {
        Registration(BuiltInRegistries.FEATURE, identifier) { feature }.register()
        feature.generator(identifier) {
            registerConfiguredFeature { NoneFeatureConfiguration.INSTANCE }.generator {
                registerPlacedFeature(placedFeatureKey) { per(8) + flower(center, ground) + onResinCementedDirt }
            }
        }
    }
}

class FallenPlasticTreeLogFeature(codec: Codec<NoneFeatureConfiguration>) : Feature<NoneFeatureConfiguration>(codec) {
    override fun place(context: FeaturePlaceContext<NoneFeatureConfiguration>): Boolean {
        val level = context.level()
        val originBlockPos = context.origin()
        val random = context.random()

        // 空気や草のように既存のブロックを押しのけずに済み、かつ水没もしていない位置にのみ、丸太を置けるのだ～🌱
        fun canPlaceLog(blockPos: BlockPos): Boolean {
            val blockState = level.getBlockState(blockPos)
            return blockState.canBeReplaced() && blockState.fluidState.isEmpty
        }

        // 丸太が宙に浮かないように、直下が完全な立方体であることを確かめるのだ～🌱
        fun isSupported(blockPos: BlockPos): Boolean {
            val belowBlockPos = blockPos.below()
            return level.getBlockState(belowBlockPos).isSolidRender(level, belowBlockPos)
        }

        val stumpHeight = random.nextIntBetweenInclusive(1, 2)
        val stumpBlockPosList = (0 until stumpHeight).map { originBlockPos.above(it) }

        // 切り株が既存の地形にめり込む位置では、倒木そのものを生成しないのだ～🌱
        if (!stumpBlockPosList.all { canPlaceLog(it) }) return false

        val logBlockState = TreeBlockCard.PLASTIC_TREE_LOG.block().defaultBlockState()

        // 折れ残った切り株なのだ～🌱
        stumpBlockPosList.forEach { blockPos ->
            level.setBlock(blockPos, logBlockState.with(RotatedPillarBlock.AXIS, Direction.Axis.Y), 2)
        }

        val direction = Direction.from2DDataValue(random.nextInt(4))
        val length = random.nextIntBetweenInclusive(5, 9)

        // 折れた木が切り株から離れて倒れた様子を出すため、倒れた部分は1ブロックの隙間を空けた先から始まるのだ～🌱
        val fallenBaseBlockPosList = (0 until length).map { originBlockPos.relative(direction, it + 2) }

        // 倒れた部分は水平にまっすぐ横たわるから、その全体を一度に置ける高さを、切り株の足元を中心に上下2ブロックまで探すのだ～🌱
        // 全体が宙に浮く高さを弾くために、どこか1か所でも直下に支えがあることを要求するのだ～🌱
        val fallenBlockPosList = (-2..2)
            .map { dy -> fallenBaseBlockPosList.map { it.above(dy) } }
            .firstOrNull { blockPosList -> blockPosList.all { canPlaceLog(it) } && blockPosList.any { isSupported(it) } }

        // 倒れる先が無い地形では、切り株だけが残るのだ～🌱
        if (fallenBlockPosList == null) return true

        // 地面に横たわる幹なのだ～🌱
        fallenBlockPosList.forEach { blockPos ->
            level.setBlock(blockPos, logBlockState.with(RotatedPillarBlock.AXIS, direction.axis), 2)
        }

        // 幹の上には苔がまばらに生えるのだ～🌱
        fallenBlockPosList.forEach { blockPos ->
            if (random.nextFloat() >= 0.3F) return@forEach
            val mossBlockPos = blockPos.above()
            if (!level.getBlockState(mossBlockPos).canBeReplaced()) return@forEach
            level.setBlock(mossBlockPos, Blocks.MOSS_CARPET.defaultBlockState(), 2)
        }

        return true
    }
}
