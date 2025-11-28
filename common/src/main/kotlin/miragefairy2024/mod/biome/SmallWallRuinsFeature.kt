package miragefairy2024.mod.biome

import com.mojang.serialization.Codec
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.materials.BlockMaterialCard
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
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.world.level.block.SlabBlock
import net.minecraft.world.level.block.state.properties.SlabType
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter

object SmallWallRuinsFeatureCard {
    val identifier = MirageFairy2024.identifier("small_wall_ruins")
    val feature = SmallWallRuinsFeature(NoneFeatureConfiguration.CODEC)
    val placedFeatureKey = Registries.PLACED_FEATURE with identifier

    context(ModContext)
    fun init() {
        Registration(BuiltInRegistries.FEATURE, identifier) { feature }.register()
        feature.generator(identifier) {
            registerConfiguredFeature { NoneFeatureConfiguration.INSTANCE }.generator {
                // 適度な頻度で、地表の花配置と同様のサンプリング + 下が耐霊石タイルであることを要求
                registerPlacedFeature(placedFeatureKey) {
                    per(8) + flower(square, surface) + listOf(
                        BlockPredicateFilter.forPredicate(
                            BlockPredicate.matchesBlocks(BlockPos.ZERO.below(), BlockMaterialCard.AURA_RESISTANT_CERAMIC_TILES.block())
                        )
                    )
                }
            }
        }
    }
}

class SmallWallRuinsFeature(codec: Codec<NoneFeatureConfiguration>) : Feature<NoneFeatureConfiguration>(codec) {
    override fun place(context: FeaturePlaceContext<NoneFeatureConfiguration>): Boolean {
        val level = context.level()
        val origin = context.origin()
        val random = context.random()

        // パラメータ（小規模）
        val height = random.nextIntBetweenInclusive(2, 4)
        val len1 = random.nextIntBetweenInclusive(2, 5)
        val len2 = random.nextIntBetweenInclusive(2, 5)

        // 向き（L字：X と Z を組み合わせ、符号はランダム）
        val sx = if (random.nextBoolean()) 1 else -1
        val sz = if (random.nextBoolean()) 1 else -1

        // タイル以外の耐霊石ブロック集合（地面に混じっていたら失敗）
        val nonTileAuraBlocks = setOf(
            BlockMaterialCard.AURA_RESISTANT_CERAMIC.block(),
            BlockMaterialCard.AURA_RESISTANT_CERAMIC_SLAB.block(),
            BlockMaterialCard.AURA_RESISTANT_CERAMIC_STAIRS.block(),
            BlockMaterialCard.COBBLED_AURA_RESISTANT_CERAMIC.block(),
            BlockMaterialCard.SMOOTH_AURA_RESISTANT_CERAMIC.block(),
            BlockMaterialCard.POLISHED_AURA_RESISTANT_CERAMIC.block(),
            BlockMaterialCard.AURA_RESISTANT_CERAMIC_BRICKS.block(),
            BlockMaterialCard.AURA_RESISTANT_CERAMIC_BRICKS_SLAB.block(),
            BlockMaterialCard.AURA_RESISTANT_CERAMIC_BRICKS_STAIRS.block(),
            BlockMaterialCard.CHISELED_AURA_RESISTANT_CERAMIC.block(),
        )

        fun isReplaceable(pos: net.minecraft.core.BlockPos): Boolean {
            val state = level.getBlockState(pos)
            // 置換可能の簡易判定：空気または衝突形状が空（葉・草など）
            return level.isEmptyBlock(pos) || state.getCollisionShape(level, pos).isEmpty
        }

        fun existsNonTileAuraInPlannedSpace(x: Int, z: Int, baseEffective: net.minecraft.core.BlockPos): Boolean {
            // 下方向：固体に当たるまで最大10マス
            run {
                var p = baseEffective.below()
                var steps = 0
                while (steps < 10) {
                    val s = level.getBlockState(p)
                    if (s.isSolidRender(level, p)) break
                    if (s.block in nonTileAuraBlocks) return true
                    p = p.below()
                    steps++
                }
            }
            // 上方向：柱高さ + 3
            run {
                var p = baseEffective
                var steps = 0
                while (steps <= height + 3) {
                    val s = level.getBlockState(p)
                    if (s.block in nonTileAuraBlocks) return true
                    p = p.above()
                    steps++
                }
            }
            return false
        }

        // 設置可能性チェック：各柱の上方向が空で、基部下が固体かつ耐霊石タイル、ビルド高さ内
        fun computeDownExtension(base: net.minecraft.core.BlockPos): Int {
            var ext = 0
            var p = base.below()
            while (ext < 3 && isReplaceable(p)) {
                p = p.below()
                ext++
            }
            return ext
        }

        fun canPlaceColumn(x: Int, z: Int): Pair<Boolean, net.minecraft.core.BlockPos?> {
            val base = origin.offset(x, 0, z)
            val downExt = computeDownExtension(base)
            val baseEffective = base.below(downExt)
            val below = baseEffective.below()
            if (baseEffective.y < level.minBuildHeight || baseEffective.y + height >= level.maxBuildHeight) return Pair(false, null)
            val belowState = level.getBlockState(below)
            if (!belowState.isSolidRender(level, below)) return Pair(false, null)
            // 地面は耐霊石タイル限定
            if (belowState.block != BlockMaterialCard.AURA_RESISTANT_CERAMIC_TILES.block()) return Pair(false, null)
            for (dy in 0 until height) {
                val p = baseEffective.above(dy)
                if (!level.isEmptyBlock(p)) return Pair(false, null)
            }
            // 角の上にスラブを置く空間も確保（最上部高さは不変：origin.above(height)）
            if (x == 0 && z == 0) {
                val top = origin.above(height)
                if (top.y >= level.maxBuildHeight) return Pair(false, null)
                if (!level.isEmptyBlock(top)) return Pair(false, null)
            }
            return Pair(true, baseEffective)
        }

        // 2本の辺の全ての柱を事前チェック（可否 + 非タイル耐霊石の混在チェック）
        val basesEffectiveEdge1 = mutableListOf<net.minecraft.core.BlockPos>()
        val basesEffectiveEdge2 = mutableListOf<net.minecraft.core.BlockPos>()
        for (i in 0 until len1) {
            val dx = i * sx
            val (ok, baseEff) = canPlaceColumn(dx, 0)
            if (!ok || baseEff == null) return false
            if (existsNonTileAuraInPlannedSpace(dx, 0, baseEff)) return false
            basesEffectiveEdge1 += baseEff
        }
        for (i in 0 until len2) {
            val dz = i * sz
            val (ok, baseEff) = canPlaceColumn(0, dz)
            if (!ok || baseEff == null) return false
            if (existsNonTileAuraInPlannedSpace(0, dz, baseEff)) return false
            basesEffectiveEdge2 += baseEff
        }

        // 置くブロック
        val brickState = BlockMaterialCard.AURA_RESISTANT_CERAMIC_BRICKS.block().defaultBlockState()
        val slabState = BlockMaterialCard.AURA_RESISTANT_CERAMIC_BRICKS_SLAB.block().defaultBlockState()
            .setValue(SlabBlock.TYPE, SlabType.BOTTOM)

        // 設置：縦柱を並べる（角の重複はそのまま上書き）
        fun placeColumn(baseEffective: net.minecraft.core.BlockPos) {
            for (dy in 0 until height) {
                level.setBlock(baseEffective.above(dy), brickState, 2)
            }
        }
        basesEffectiveEdge1.forEach { placeColumn(it) }
        basesEffectiveEdge2.forEach { placeColumn(it) }

        // 角の頂部にレンガのハーフブロック（最上部は不変）
        level.setBlock(origin.above(height), slabState, 2)

        return true
    }
}
