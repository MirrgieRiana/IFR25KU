package miragefairy2024.mod.tree.contents.plastictree

import com.mojang.serialization.MapCodec
import miragefairy2024.mod.materials.MaterialCard
import miragefairy2024.mod.particle.ParticleTypeCard
import miragefairy2024.mod.tree.TreeBlockCard
import miragefairy2024.mod.tree.contents.DrippingLogBlock
import miragefairy2024.util.get
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.util.RandomSource
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState

class DrippingPlasticTreeLogBlock(settings: Properties) : DrippingLogBlock(settings) {
    companion object {
        val CODEC: MapCodec<DrippingPlasticTreeLogBlock> = simpleCodec(::DrippingPlasticTreeLogBlock)
    }

    override fun codec() = CODEC

    override fun getIncisedLogBlock() = TreeBlockCard.INCISED_LOG.block() // 傷の付いたプラノキの原木がまだ無いから、ハイメヴィスカのものをプレースホルダーとして置いてあるのだ～🌱
    override fun getSapItem() = MaterialCard.PLASTIC_TREE_SAP.item()
    override fun getRosinItem() = MaterialCard.HAIMEVISKA_ROSIN.item() // プラノキは涙を持たないから、ハイメヴィスカのものをプレースホルダーとして置いてあるのだ～🌱

    override fun animateTick(state: BlockState, level: Level, pos: BlockPos, random: RandomSource) {
        if (random.nextFloat() >= 0.2F) return

        val direction = state[FACING]
        val destBlockPos = pos.relative(direction)
        val destBlockState = level.getBlockState(destBlockPos)
        val destShape = destBlockState.getCollisionShape(level, destBlockPos)
        val hasSpace = when (direction) {
            Direction.NORTH -> destShape.max(Direction.Axis.Z) < 1.0
            Direction.SOUTH -> destShape.min(Direction.Axis.Z) > 0.0
            Direction.WEST -> destShape.max(Direction.Axis.X) < 1.0
            Direction.EAST -> destShape.min(Direction.Axis.X) > 0.0
            else -> throw IllegalStateException()
        }
        if (!(hasSpace || !destBlockState.isCollisionShapeFullBlock(level, destBlockPos))) return

        val position = random.nextInt(2)
        val x = when (position) {
            0 -> (7.0 + 7.0 * level.random.nextDouble()) / 16.0
            else -> (2.0 + 8.0 * level.random.nextDouble()) / 16.0
        }
        val y = when (position) {
            0 -> 12.0 / 16.0
            else -> 5.0 / 16.0
        }
        val z = 17.0 / 16.0

        val (x2, z2) = when (direction) {
            Direction.NORTH -> Pair(1.0 - x, 1.0 - z)
            Direction.EAST -> Pair(0.0 + z, 1.0 - x)
            Direction.SOUTH -> Pair(0.0 + x, 0.0 + z)
            Direction.WEST -> Pair(1.0 - z, 0.0 + x)
            else -> throw IllegalStateException()
        }

        level.addParticle(
            ParticleTypeCard.DRIPPING_SAP.particleType,
            pos.x + x2,
            pos.y + y - 1.0 / 16.0,
            pos.z + z2,
            0.0,
            0.0,
            0.0,
        )
    }
}
