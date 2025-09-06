package miragefairy2024.util

import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState

fun <E : BlockEntity> BlockEntityType(
    creator: (BlockPos, BlockState) -> E,
    blocks: Set<Block>,
): BlockEntityType<E> {
    // BlockEntityType コンストラクタの dataType は @Nullable でないが、実際には何にも使われていない。
    // 他の個所では @Nullable である Util.fetchChoiceType(References.BLOCK_ENTITY, key) を渡しており、違反している。
    /** @see BlockEntityType.register */
    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    return BlockEntityType(creator, blocks, null)
}

fun <A : BlockEntity, E : BlockEntity> checkType(
    actualType: BlockEntityType<A>,
    expectedType: BlockEntityType<E>,
    ticker: BlockEntityTicker<E>,
): BlockEntityTicker<A>? {
    return if (actualType === expectedType) {
        @Suppress("UNCHECKED_CAST")
        ticker as BlockEntityTicker<A>
    } else {
        null
    }
}
