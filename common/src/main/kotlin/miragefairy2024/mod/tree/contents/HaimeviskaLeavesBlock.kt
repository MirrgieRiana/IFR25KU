package miragefairy2024.mod.tree.contents

import com.mojang.serialization.MapCodec
import miragefairy2024.mod.particle.ParticleTypeCard
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.util.ParticleUtils
import net.minecraft.util.RandomSource
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState

class HaimeviskaLeavesBlock(settings: Properties) : ChargeableLeavesBlock(settings) {
    companion object {
        val CODEC: MapCodec<HaimeviskaLeavesBlock> = simpleCodec(::HaimeviskaLeavesBlock)
    }

    override fun codec() = CODEC

    override fun animateTick(state: BlockState, level: Level, pos: BlockPos, random: RandomSource) {
        super.animateTick(state, level, pos, random)
        if (random.nextInt(20) == 0) {
            val blockPos = pos.below()
            if (!isFaceFull(level.getBlockState(blockPos).getCollisionShape(level, blockPos), Direction.UP)) {
                // ハイメヴィスカ固有のパーティクルをハードコードで表示するのだ～🌱
                ParticleUtils.spawnParticleBelow(level, pos, random, ParticleTypeCard.HAIMEVISKA_BLOSSOM.particleType)
            }
        }
    }
}
