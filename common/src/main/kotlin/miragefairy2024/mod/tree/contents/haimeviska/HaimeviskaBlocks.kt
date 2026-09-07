package miragefairy2024.mod.tree.contents.haimeviska

import com.mojang.serialization.MapCodec
import miragefairy2024.mod.materials.MaterialCard
import miragefairy2024.mod.particle.ParticleTypeCard
import miragefairy2024.mod.tree.TreeBlockCard
import miragefairy2024.mod.tree.contents.ChargeableLeavesBlock
import miragefairy2024.mod.tree.contents.DrippingLogBlock
import miragefairy2024.mod.tree.contents.HollowLogBlock
import miragefairy2024.mod.tree.contents.IncisableLogBlock
import miragefairy2024.mod.tree.contents.IncisedLogBlock
import miragefairy2024.mod.tree.contents.spawnHaimeviskaBlossomParticle
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
                ParticleUtils.spawnParticleBelow(level, pos, random, ParticleTypeCard.HAIMEVISKA_BLOSSOM.particleType)
            }
        }
    }
}

class HaimeviskaLogBlock(settings: Properties) : IncisableLogBlock(settings) {
    companion object {
        val CODEC: MapCodec<HaimeviskaLogBlock> = simpleCodec(::HaimeviskaLogBlock)
    }

    override fun codec() = CODEC

    override fun getIncisedLogBlock() = TreeBlockCard.INCISED_LOG.block()
}

class IncisedHaimeviskaLogBlock(settings: Properties) : IncisedLogBlock(settings) {
    companion object {
        val CODEC: MapCodec<IncisedHaimeviskaLogBlock> = simpleCodec(::IncisedHaimeviskaLogBlock)
    }

    override fun codec() = CODEC

    override fun getDrippingLogBlock() = TreeBlockCard.DRIPPING_LOG.block()
}

class DrippingHaimeviskaLogBlock(settings: Properties) : DrippingLogBlock(settings) {
    companion object {
        val CODEC: MapCodec<DrippingHaimeviskaLogBlock> = simpleCodec(::DrippingHaimeviskaLogBlock)
    }

    override fun codec() = CODEC

    override fun getIncisedLogBlock() = TreeBlockCard.INCISED_LOG.block()
    override fun getSapItem() = MaterialCard.HAIMEVISKA_SAP.item()
    override fun getRosinItem() = MaterialCard.HAIMEVISKA_ROSIN.item()

    override fun animateTick(state: BlockState, level: Level, pos: BlockPos, random: RandomSource) {
        spawnHaimeviskaBlossomParticle(state, level, pos, random)
    }
}

class HollowHaimeviskaLogBlock(settings: Properties) : HollowLogBlock(settings) {
    companion object {
        val CODEC: MapCodec<HollowHaimeviskaLogBlock> = simpleCodec(::HollowHaimeviskaLogBlock)
    }

    override fun codec() = CODEC
}
