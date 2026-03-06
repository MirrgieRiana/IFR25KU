package miragefairy2024.mod.biome

import com.mojang.serialization.Codec
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.Registration
import miragefairy2024.util.flower
import miragefairy2024.util.generator
import miragefairy2024.util.per
import miragefairy2024.util.register
import miragefairy2024.util.registerConfiguredFeature
import miragefairy2024.util.registerPlacedFeature
import miragefairy2024.util.square
import miragefairy2024.util.surface
import miragefairy2024.util.with
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.SlabBlock
import net.minecraft.world.level.block.entity.SpawnerBlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.SlabType
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration

object ElevatedSpawnerFeatureCard {
    val identifier = MirageFairy2024.identifier("elevated_spawner")
    val feature = ElevatedSpawnerFeature(NoneFeatureConfiguration.CODEC)
    val placedFeatureKey = Registries.PLACED_FEATURE with identifier

    context(ModContext)
    fun init() {
        Registration(BuiltInRegistries.FEATURE, identifier) { feature }.register()
        feature.generator(identifier) {
            registerConfiguredFeature { NoneFeatureConfiguration.INSTANCE }.generator {
                registerPlacedFeature(placedFeatureKey) { per(16) + flower(square, surface) }
            }
        }
    }
}

class ElevatedSpawnerFeature(codec: Codec<NoneFeatureConfiguration>) : Feature<NoneFeatureConfiguration>(codec) {
    override fun place(context: FeaturePlaceContext<NoneFeatureConfiguration>): Boolean {
        val level = context.level()
        val origin = context.origin()
        val random = context.random()

        // 支柱の高さ（4～10ブロック）
        val pillarHeight = random.nextIntBetweenInclusive(4, 10)

        // 構造物の全高: 支柱 + 台(1) + 空間(3) + 天井(1) + フェンス(1) + ラピスラズリ(1) = 支柱 + 7
        val totalHeight = pillarHeight + 7
        if (origin.y + totalHeight >= level.maxBuildHeight) return false

        // 地面が固体であることを確認
        if (!level.getBlockState(origin.below()).isSolidRender(level, origin.below())) return false

        // 支柱の位置が空であることを確認
        for (y in 0 until pillarHeight) {
            if (!level.isEmptyBlock(origin.above(y))) return false
        }

        // 台から上の7x7範囲が空であることを確認（ラピスラズリの高さまで）
        for (dy in 0..6) {
            for (dx in -3..3) {
                for (dz in -3..3) {
                    if (!level.isEmptyBlock(origin.above(pillarHeight + dy).offset(dx, 0, dz))) return false
                }
            }
        }

        // ランダムな石レンガバリアント
        fun stoneBricks(): BlockState = when (random.nextInt(3)) {
            0 -> Blocks.MOSSY_STONE_BRICKS.defaultBlockState()
            1 -> Blocks.CRACKED_STONE_BRICKS.defaultBlockState()
            else -> Blocks.STONE_BRICKS.defaultBlockState()
        }

        fun stoneWall(): BlockState = if (random.nextInt(2) == 0) Blocks.MOSSY_STONE_BRICK_WALL.defaultBlockState() else Blocks.STONE_BRICK_WALL.defaultBlockState()

        fun stoneSlab(): BlockState = (if (random.nextInt(2) == 0) Blocks.MOSSY_STONE_BRICK_SLAB else Blocks.STONE_BRICK_SLAB).defaultBlockState().with(SlabBlock.TYPE, SlabType.BOTTOM)

        // 支柱を配置
        for (y in 0 until pillarHeight) {
            level.setBlock(origin.above(y), stoneBricks(), 2)
        }

        val platformY = pillarHeight

        // 5x1x5の台を配置
        for (dx in -2..2) {
            for (dz in -2..2) {
                level.setBlock(origin.above(platformY).offset(dx, 0, dz), stoneBricks(), 2)
            }
        }

        // 台の中央にスケルトンスポナーを配置
        val spawnerPos = origin.above(platformY + 1)
        level.setBlock(spawnerPos, Blocks.SPAWNER.defaultBlockState(), 2)
        val blockEntity = level.getBlockEntity(spawnerPos)
        if (blockEntity is SpawnerBlockEntity) {
            blockEntity.setEntityId(EntityType.SKELETON, random)
        }

        // 台の四隅に高さ3の石レンガフェンスを配置
        for ((dx, dz) in listOf(-2 to -2, -2 to 2, 2 to -2, 2 to 2)) {
            for (dy in 1..3) {
                level.setBlock(origin.above(platformY + dy).offset(dx, 0, dz), stoneWall(), 2)
            }
        }

        val ceilingY = platformY + 4

        // 7x1x7の天井を配置（下置きハーフブロック、中央は全ブロック）
        for (dx in -3..3) {
            for (dz in -3..3) {
                val blockState = if (dx == 0 && dz == 0) stoneBricks() else stoneSlab()
                level.setBlock(origin.above(ceilingY).offset(dx, 0, dz), blockState, 2)
            }
        }

        // 天井中央の上に石レンガフェンスを配置
        level.setBlock(origin.above(ceilingY + 1), stoneWall(), 2)

        // その上にラピスラズリブロックを配置
        level.setBlock(origin.above(ceilingY + 2), Blocks.LAPIS_BLOCK.defaultBlockState(), 2)

        return true
    }
}
