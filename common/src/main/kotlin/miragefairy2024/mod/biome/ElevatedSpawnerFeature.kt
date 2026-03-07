package miragefairy2024.mod.biome

import com.mojang.serialization.Codec
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.Registration
import miragefairy2024.util.flower
import miragefairy2024.util.generator
import miragefairy2024.util.per
import miragefairy2024.util.randomOrThrow
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
        val originBlockPos = context.origin()
        val random = context.random()
        val pillarHeight = random.nextIntBetweenInclusive(4, 10)
        val totalHeight = pillarHeight + 1 + 3 + 1 + 1 + 1

        if (originBlockPos.y + totalHeight - 1 > level.maxBuildHeight - 1) return false

        // 検証
        if (!level.getBlockState(originBlockPos.below()).isSolidRender(level, originBlockPos.below())) return false
        (0 until pillarHeight).forEach { dy ->
            if (!level.isEmptyBlock(originBlockPos.above(dy))) return false
        }
        (-3..3).forEach { dx ->
            (pillarHeight until totalHeight).forEach { dy ->
                (-3..3).forEach { dz ->
                    if (!level.isEmptyBlock(originBlockPos.offset(dx, dy, dz))) return false
                }
            }
        }

        // バリアントリスト
        val blocks = listOf(Blocks.STONE_BRICKS, Blocks.MOSSY_STONE_BRICKS, Blocks.CRACKED_STONE_BRICKS)
        fun nextBlock() = blocks.randomOrThrow(random).defaultBlockState()
        val walls = listOf(Blocks.STONE_BRICK_WALL, Blocks.MOSSY_STONE_BRICK_WALL)
        fun nextWall() = walls.randomOrThrow(random).defaultBlockState()
        val slabs = listOf(Blocks.STONE_BRICK_SLAB, Blocks.MOSSY_STONE_BRICK_SLAB)
        fun nextBottomSlab() = slabs.randomOrThrow(random).defaultBlockState().with(SlabBlock.TYPE, SlabType.BOTTOM)

        var y2 = 0

        // 支柱
        (0 until pillarHeight).forEach { dy ->
            level.setBlock(originBlockPos.above(y2 + dy), nextBlock(), 2)
        }
        y2 += pillarHeight

        // 支柱の上に5x1x5の台
        (-2..2).forEach { dx ->
            (-2..2).forEach { dz ->
                level.setBlock(originBlockPos.offset(dx, y2, dz), nextBlock(), 2)
            }
        }
        y2 += 1

        // 台の中央にスケルトンスポナー
        run {
            val spawnerBlockPos = originBlockPos.above(y2)
            level.setBlock(spawnerBlockPos, Blocks.SPAWNER.defaultBlockState(), 2)
            val blockEntity = level.getBlockEntity(spawnerBlockPos)
            if (blockEntity is SpawnerBlockEntity) {
                blockEntity.setEntityId(EntityType.SKELETON, random)
            }
        }


        // 台の四隅に高さ3の石レンガフェンス
        fun f(dx: Int, dz: Int) {
            repeat(3) { dy ->
                level.setBlock(originBlockPos.offset(dx, y2 + dy, dz), nextWall(), 2)
            }
        }
        f(-2, -2)
        f(-2, 2)
        f(2, -2)
        f(2, 2)
        y2 += 3

        // フェンスの上に7x1x7の下置きハーフブロック・中央は全ブロックの天井
        (-3..3).forEach { dx ->
            (-3..3).forEach { dz ->
                val blockState = if (dx == 0 && dz == 0) nextBlock() else nextBottomSlab()
                level.setBlock(originBlockPos.offset(dx, y2, dz), blockState, 2)
            }
        }
        y2 += 1

        // 天井中央の上に石レンガフェンス
        level.setBlock(originBlockPos.above(y2), nextWall(), 2)
        y2 += 1

        // 石レンガフェンスの上にラピスラズリブロック
        level.setBlock(originBlockPos.above(y2), Blocks.LAPIS_BLOCK.defaultBlockState(), 2)
        y2 += 1

        return true
    }
}
