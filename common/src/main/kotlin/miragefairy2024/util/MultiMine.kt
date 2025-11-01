package miragefairy2024.util

import mirrg.kotlin.helium.ceilToInt
import net.minecraft.core.BlockBox
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState

abstract class MultiMine(
    val level: Level,
    val blockPos: BlockPos,
    val blockState: BlockState,
    val miner: Player,
    val toolItem: Item,
    val toolItemStack: ItemStack,
) {

    interface Visitor {
        fun visit(
            originalBlockPosList: Iterable<BlockPos>,
            miningDamage: Double,
            maxDistance: Int = Int.MAX_VALUE,
            maxCount: Int? = null,
            neighborType: NeighborType = NeighborType.FACES,
            onMine: (BlockPos) -> Unit = {},
            region: BlockBox? = null,
            canContinue: (BlockPos, BlockState) -> Boolean,
        ): Boolean
    }

    abstract fun visit(visitor: Visitor)

    open fun isValidBaseBlockState(): Boolean = true

    fun execute(serverSide: ServerSide, limitHardness: Float) {
        if (miner.isShiftKeyDown) return // 使用者がスニーク中
        if (!toolItem.isCorrectToolForDrops(toolItemStack, blockState)) return // 非対応ツール
        if (!isValidBaseBlockState()) return // 掘ったブロックが適切でない

        // 発動

        visit(object : Visitor {
            override fun visit(
                originalBlockPosList: Iterable<BlockPos>,
                miningDamage: Double,
                maxDistance: Int,
                maxCount: Int?,
                neighborType: NeighborType,
                onMine: (BlockPos) -> Unit,
                region: BlockBox?,
                canContinue: (BlockPos, BlockState) -> Boolean,
            ): Boolean {
                blockVisitor(originalBlockPosList, visitOrigins = false, maxDistance = maxDistance, maxCount = maxCount, neighborType = neighborType) { _, _, toBlockPos ->
                    if (region != null && toBlockPos !in region) return@blockVisitor false // 範囲外
                    val blockState = level.getBlockState(toBlockPos)
                    if (blockState.getDestroySpeed(level, toBlockPos) < 0) return@blockVisitor false // 破壊不能な硬度
                    canContinue(toBlockPos, blockState)
                }.forEach fail@{ (_, blockPos) ->
                    if (toolItemStack.isEmpty) return false // ツールの耐久値が枯渇した
                    if (toolItemStack.maxDamage - toolItemStack.damageValue <= miningDamage.ceilToInt()) return false // ツールの耐久値が残り僅か

                    // 採掘を続行

                    val targetBlockState = level.getBlockState(blockPos)
                    if (!canContinue(blockPos, targetBlockState)) return@fail // 採掘時のイベントで条件が外れた
                    val targetHardness = targetBlockState.getDestroySpeed(level, blockPos)
                    if (targetHardness > limitHardness) return@fail // 上限硬度よりも硬いものは掘れない
                    if (breakBlockByMagic(toolItemStack, level, blockPos, miner.asServerPlayer(serverSide))) {
                        if (targetHardness > 0) {
                            val damage = level.random.randomInt(miningDamage)
                            if (damage > 0) {
                                toolItemStack.hurtAndBreak(damage, miner, EquipmentSlot.MAINHAND)
                            }
                        }
                        onMine(blockPos)
                    }
                }
                return true
            }
        })
    }

}
