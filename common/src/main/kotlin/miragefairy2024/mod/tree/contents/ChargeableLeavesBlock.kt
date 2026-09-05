package miragefairy2024.mod.tree.contents

import com.mojang.serialization.MapCodec
import miragefairy2024.mod.particle.ParticleTypeCard
import miragefairy2024.util.get
import miragefairy2024.util.lightProxy
import miragefairy2024.util.randomBoolean
import miragefairy2024.util.with
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.ParticleUtils
import net.minecraft.util.RandomSource
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.LeavesBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BooleanProperty

class ChargeableLeavesBlock(settings: Properties) : LeavesBlock(settings) {
    companion object {
        val CODEC: MapCodec<ChargeableLeavesBlock> = simpleCodec(::ChargeableLeavesBlock)
        val CHARGED: BooleanProperty = BooleanProperty.create("charged")
    }

    override fun codec() = CODEC

    init {
        registerDefaultState(defaultBlockState().with(CHARGED, true))
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        super.createBlockStateDefinition(builder)
        builder.add(CHARGED)
    }

    override fun isRandomlyTicking(state: BlockState) = super.isRandomlyTicking(state) || !state[CHARGED]

    @Suppress("OVERRIDE_DEPRECATION")
    override fun randomTick(state: BlockState, level: ServerLevel, pos: BlockPos, random: RandomSource) {
        super.randomTick(state, level, pos, random)
        if (level.getBlockState(pos) != state) return // 親クラスの処理で葉が枯れ落ちた場合はスルー
        if (!state[CHARGED]) {
            if (random.randomBoolean(15, level.lightProxy.getLightLevel(pos))) {
                level.setBlock(pos, state.with(CHARGED, true), UPDATE_CLIENTS)
            }
        }
    }

    override fun animateTick(state: BlockState, level: Level, pos: BlockPos, random: RandomSource) {
        super.animateTick(state, level, pos, random)
        if (random.nextInt(20) == 0) {
            val blockPos = pos.below()
            if (!isFaceFull(level.getBlockState(blockPos).getCollisionShape(level, blockPos), Direction.UP)) {
                ParticleUtils.spawnParticleBelow(level, pos, random, ParticleTypeCard.HAIMEVISKA_BLOSSOM.particleType)
            }
        }
    }
}
