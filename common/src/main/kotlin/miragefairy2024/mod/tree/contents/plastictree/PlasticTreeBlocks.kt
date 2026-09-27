package miragefairy2024.mod.tree.contents.plastictree

import com.mojang.serialization.MapCodec
import miragefairy2024.mod.materials.MaterialCard
import miragefairy2024.mod.particle.ParticleTypeCard
import miragefairy2024.mod.tree.TreeBlockCard
import miragefairy2024.mod.tree.contents.DrippingLogBlock
import miragefairy2024.mod.tree.contents.IncisableLogBlock
import miragefairy2024.mod.tree.contents.IncisedLogBlock
import miragefairy2024.mod.tree.contents.spawnDrippingSapParticle
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.util.ParticleUtils
import net.minecraft.util.RandomSource
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.LeavesBlock
import net.minecraft.world.level.block.state.BlockState

class PlasticTreeLeavesBlock(settings: Properties) : LeavesBlock(settings) {
    companion object {
        val CODEC: MapCodec<PlasticTreeLeavesBlock> = simpleCodec(::PlasticTreeLeavesBlock)
    }

    override fun codec() = CODEC

    override fun animateTick(state: BlockState, level: Level, pos: BlockPos, random: RandomSource) {
        super.animateTick(state, level, pos, random)
        if (random.nextInt(20) == 0) {
            val blockPos = pos.below()
            if (!isFaceFull(level.getBlockState(blockPos).getCollisionShape(level, blockPos), Direction.UP)) {
                ParticleUtils.spawnParticleBelow(level, pos, random, ParticleTypeCard.DRIPPING_PLASTIC_TREE_SAP.particleType)
            }
        }
    }
}

class PlasticTreeLogBlock(settings: Properties) : IncisableLogBlock(settings) {
    companion object {
        val CODEC: MapCodec<PlasticTreeLogBlock> = simpleCodec(::PlasticTreeLogBlock)
    }

    override fun codec() = CODEC

    override fun getIncisedLogBlock() = TreeBlockCard.INCISED_PLASTIC_TREE_LOG.block()
}

class IncisedPlasticTreeLogBlock(settings: Properties) : IncisedLogBlock(settings) {
    companion object {
        val CODEC: MapCodec<IncisedPlasticTreeLogBlock> = simpleCodec(::IncisedPlasticTreeLogBlock)
    }

    override fun codec() = CODEC

    override fun getDrippingLogBlock() = TreeBlockCard.DRIPPING_PLASTIC_TREE_LOG.block()
}

class DrippingPlasticTreeLogBlock(settings: Properties) : DrippingLogBlock(settings) {
    companion object {
        val CODEC: MapCodec<DrippingPlasticTreeLogBlock> = simpleCodec(::DrippingPlasticTreeLogBlock)
    }

    override fun codec() = CODEC

    override fun getIncisedLogBlock() = TreeBlockCard.INCISED_PLASTIC_TREE_LOG.block()
    override fun getSapItem() = MaterialCard.PLASTIC_TREE_SAP.item()
    override fun getRosinItem() = MaterialCard.PLASTIC_TREE_SAP.item() // TODO レア素材を指定するのだ～🌱

    override fun animateTick(state: BlockState, level: Level, pos: BlockPos, random: RandomSource) {
        spawnDrippingSapParticle(state, level, pos, random, ParticleTypeCard.DRIPPING_PLASTIC_TREE_SAP.particleType)
    }
}
