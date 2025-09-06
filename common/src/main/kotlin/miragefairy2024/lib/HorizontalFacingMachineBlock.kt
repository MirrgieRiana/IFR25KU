package miragefairy2024.lib

import miragefairy2024.util.checkType
import miragefairy2024.util.getOrNull
import miragefairy2024.util.isNotIn
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerPlayer
import net.minecraft.stats.Stats
import net.minecraft.world.InteractionResult
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult

abstract class HorizontalFacingMachineBlock(private val card: MachineCard<*, *, *>) : SimpleHorizontalFacingBlock(card.createBlockSettings()), EntityBlock {
    companion object {
        fun getActualSide(blockState: BlockState, side: Direction): Direction {
            return when (side) {
                Direction.UP, Direction.DOWN -> side

                else -> {
                    val direction = blockState.getOrNull(FACING) ?: Direction.NORTH
                    Direction.from2DDataValue((direction.get2DDataValue() + side.get2DDataValue()) % 4)
                }
            }
        }
    }

    // Block Entity

    override fun newBlockEntity(pos: BlockPos, state: BlockState) = card.blockEntityAccessor.create(pos, state)

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun triggerEvent(state: BlockState, world: Level, pos: BlockPos, type: Int, data: Int): Boolean {
        super.triggerEvent(state, world, pos, type, data)
        val blockEntity = world.getBlockEntity(pos) ?: return false
        return blockEntity.triggerEvent(type, data)
    }

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun onRemove(state: BlockState, world: Level, pos: BlockPos, newState: BlockState, moved: Boolean) {
        if (state isNotIn newState.block) {
            card.blockEntityAccessor.castOrNull(world.getBlockEntity(pos))?.dropItems()
            super.onRemove(state, world, pos, newState, moved)
        }
    }


    // Move

    override fun <T : BlockEntity> getTicker(world: Level, state: BlockState, type: BlockEntityType<T>): BlockEntityTicker<T>? {
        return if (world.isClientSide) {
            checkType(type, card.blockEntityType()) { world2, pos, state2, blockEntity ->
                blockEntity.clientTick(world2, pos, state2)
            }
        } else {
            checkType(type, card.blockEntityType()) { world2, pos, state2, blockEntity ->
                blockEntity.serverTick(world2, pos, state2)
            }
        }
    }


    // Gui

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getMenuProvider(state: BlockState, world: Level, pos: BlockPos) = world.getBlockEntity(pos) as? MenuProvider

    @Suppress("OVERRIDE_DEPRECATION")
    override fun useWithoutItem(state: BlockState, level: Level, pos: BlockPos, player: Player, hitResult: BlockHitResult): InteractionResult {
        if (level.isClientSide) return InteractionResult.SUCCESS
        val blockEntity = card.blockEntityAccessor.castOrNull(level.getBlockEntity(pos)) ?: return InteractionResult.CONSUME
        player.openMenu(object : ExtendedScreenHandlerFactory<Unit> {
            override fun createMenu(syncId: Int, playerInventory: Inventory, player: Player) = blockEntity.createMenu(syncId, playerInventory, player)
            override fun getDisplayName() = blockEntity.displayName
            override fun getScreenOpeningData(player: ServerPlayer) = Unit
        })
        player.awardStat(Stats.ITEM_USED.get(this.asItem()))
        return InteractionResult.CONSUME
    }

}
