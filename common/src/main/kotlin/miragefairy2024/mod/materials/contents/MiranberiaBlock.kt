package miragefairy2024.mod.materials.contents

import com.mojang.serialization.MapCodec
import miragefairy2024.mod.particle.ParticleTypeCard
import net.minecraft.core.BlockPos
import net.minecraft.util.RandomSource
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BushBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape

// シダの TallGrassBlock は、シダ以外だと骨粉で背の高い草に化けてしまううえ、codec の型が固定されていて継承できないのだ～🌱
// だから、シダと同じ姿かたちを持つ草を、親クラスの BushBlock から組み立てるのだ～🌱
class MiranberiaBlock(settings: Properties) : BushBlock(settings) {
    companion object {
        val CODEC: MapCodec<MiranberiaBlock> = simpleCodec(::MiranberiaBlock)

        // バニラのシダと同じ大きさなのだ～🌱
        val SHAPE: VoxelShape = box(2.0, 0.0, 2.0, 14.0, 13.0, 14.0)

        /** バニラの [net.minecraft.world.level.block.CherryLeavesBlock.animateTick] の、花びらを散らす確率と同じ値なのだ～🌱 */
        private const val PARTICLE_CHANCE = 10
    }

    override fun codec() = CODEC

    override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext) = SHAPE

    override fun animateTick(state: BlockState, level: Level, pos: BlockPos, random: RandomSource) {
        if (random.nextInt(PARTICLE_CHANCE) != 0) return
        // 株の根元ではなく、葉の高さのあたりから湧かせるのだ～🌱
        level.addParticle(
            ParticleTypeCard.SPARKLE.particleType,
            pos.x + 0.2 + random.nextDouble() * 0.6,
            pos.y + 0.2 + random.nextDouble() * 0.5,
            pos.z + 0.2 + random.nextDouble() * 0.6,
            0.0, 0.0, 0.0,
        )
    }
}
