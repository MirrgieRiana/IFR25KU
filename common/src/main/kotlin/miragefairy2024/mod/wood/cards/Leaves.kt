package miragefairy2024.mod.wood.cards

import miragefairy2024.ModContext
import miragefairy2024.mod.wood.WoodBlockCard
import miragefairy2024.mod.wood.WoodBlockConfiguration
import miragefairy2024.util.generator
import miragefairy2024.util.registerBlockGeneratedModelGeneration
import miragefairy2024.util.registerChild
import miragefairy2024.util.registerComposterInput
import miragefairy2024.util.registerCutoutRenderLayer
import miragefairy2024.util.registerFlammable
import miragefairy2024.util.registerFoliageColorProvider
import miragefairy2024.util.registerLootTableGeneration
import miragefairy2024.util.registerModelGeneration
import miragefairy2024.util.registerRedirectColorProvider
import miragefairy2024.util.registerSingletonBlockStateGeneration
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.data.models.model.TexturedModel
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.util.ParticleUtils
import net.minecraft.util.RandomSource
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.LeavesBlock
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.level.material.PushReaction

abstract class WoodLeavesBlockCard(configuration: WoodBlockConfiguration, private val saplingCard: () -> WoodBlockCard) : WoodBlockCard(configuration) {
    override fun createSettings(): BlockBehaviour.Properties = super.createSettings()
        .mapColor(MapColor.PLANT)
        .strength(0.2F)
        .randomTicks()
        .sound(SoundType.GRASS)
        .noOcclusion()
        .isValidSpawn(Blocks::ocelotOrParrot)
        .isSuffocating(Blocks::never)
        .isViewBlocking(Blocks::never)
        .ignitedByLava()
        .pushReaction(PushReaction.DESTROY)
        .isRedstoneConductor(Blocks::never)

    // 葉のモデルは樹種によって構成が異なるため、派生形で差し替えられるようにしてあるのだ
    context(ModContext)
    protected open fun initModelGeneration() {
        block.registerSingletonBlockStateGeneration()
        block.registerModelGeneration(TexturedModel.LEAVES)
        item.registerBlockGeneratedModelGeneration(block)
    }

    context(ModContext)
    override fun init() {
        super.init()

        // レンダリング
        initModelGeneration()
        block.registerCutoutRenderLayer()
        block.registerFoliageColorProvider()
        item.registerRedirectColorProvider()

        // レシピ
        block.registerLootTableGeneration { it, _ ->
            it.createLeavesDrops(block(), saplingCard().block(), 0.05F / 4F, 0.0625F / 4F, 0.083333336F / 4F, 0.1F / 4F)
        }
        item.registerComposterInput(0.3F)

        // 性質
        block.registerFlammable(30, 30)

        // タグ
        BlockTags.LEAVES.generator.registerChild(block)
        ItemTags.LEAVES.generator.registerChild(item)
        BlockTags.MINEABLE_WITH_HOE.generator.registerChild(block)

    }
}

// 樹液を垂らす葉なのだ～🌱
// 葉っぱが茂る樹冠の下側から、樹液の雫が垂れてくるのだ
abstract class WoodLeavesBlock(settings: Properties) : LeavesBlock(settings) {
    companion object {
        /** 樹液の雫が垂れる確率の逆数なのだ。 */
        private const val SAP_CHANCE = 60
    }

    /** この葉から垂れる樹液のパーティクルなのだ。 */
    protected abstract val sapParticleOptions: ParticleOptions

    override fun animateTick(state: BlockState, world: Level, pos: BlockPos, random: RandomSource) {
        super.animateTick(state, world, pos, random)
        if (random.nextInt(SAP_CHANCE) == 0) {
            val blockPos = pos.below()
            if (!isFaceFull(world.getBlockState(blockPos).getCollisionShape(world, blockPos), Direction.UP)) {
                ParticleUtils.spawnParticleBelow(world, pos, random, sapParticleOptions)
            }
        }
    }
}
