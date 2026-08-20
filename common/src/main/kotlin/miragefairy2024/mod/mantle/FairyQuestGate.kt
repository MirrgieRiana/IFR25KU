package miragefairy2024.mod.mantle

import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.Portal
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.EnumProperty
import net.minecraft.world.level.dimension.DimensionType
import net.minecraft.world.level.portal.DimensionTransition
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.shapes.CollisionContext

/** ゲートの内側の、水平方向の幅なのだ～🌱 */
const val FAIRY_QUEST_GATE_WIDTH = 2

/** ゲートの内側の、鉛直方向の高さなのだ～🌱 */
const val FAIRY_QUEST_GATE_HEIGHT = 3

/**
 * フェアリークエストゲートの、通り抜けられる部分のブロックなのだ～🌱
 *
 * ネザーポータルと同様に、[BlockStateProperties.HORIZONTAL_AXIS] によって向きを持つのだ～🌱
 */
class FairyQuestGatePortalBlock(properties: Properties) : MantleBlock(properties), Portal {
    companion object {
        val CODEC: MapCodec<FairyQuestGatePortalBlock> = simpleCodec(::FairyQuestGatePortalBlock)
        val AXIS: EnumProperty<Direction.Axis> = BlockStateProperties.HORIZONTAL_AXIS
        private val X_AXIS_SHAPE = box(0.0, 0.0, 6.0, 16.0, 16.0, 10.0)
        private val Z_AXIS_SHAPE = box(6.0, 0.0, 0.0, 10.0, 16.0, 16.0)
    }

    init {
        registerDefaultState(stateDefinition.any().setValue(AXIS, Direction.Axis.X))
    }

    override fun codec() = CODEC

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(AXIS)
    }

    override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext) = when (state.getValue(AXIS)) {
        Direction.Axis.Z -> Z_AXIS_SHAPE
        else -> X_AXIS_SHAPE
    }

    override fun updateShape(state: BlockState, direction: Direction, neighborState: BlockState, level: LevelAccessor, pos: BlockPos, neighborPos: BlockPos): BlockState {
        // ネザーポータルと同じく、枠が欠けると消えるのだ～🌱
        val axis = state.getValue(AXIS)
        val isAlongPortal = direction.axis == Direction.Axis.Y || direction.axis == axis
        if (isAlongPortal) return super.updateShape(state, direction, neighborState, level, pos, neighborPos)
        if (neighborState.`is`(this)) return super.updateShape(state, direction, neighborState, level, pos, neighborPos)
        if (neighborState.`is`(MantleBlockCard.FAIRY_QUEST_GATE.block())) return super.updateShape(state, direction, neighborState, level, pos, neighborPos)
        return Blocks.AIR.defaultBlockState()
    }

    override fun entityInside(state: BlockState, level: Level, pos: BlockPos, entity: Entity) {
        if (entity.canUsePortal(false)) entity.setAsInsidePortal(this, pos)
    }

    override fun getPortalTransitionTime(level: ServerLevel, entity: Entity) = 80

    override fun getLocalTransition() = Portal.Transition.CONFUSION

    override fun getPortalDestination(level: ServerLevel, entity: Entity, pos: BlockPos): DimensionTransition? {
        val destinationLevelKey = if (level.dimension() == MANTLE_DIMENSION_KEY) Level.OVERWORLD else MANTLE_DIMENSION_KEY
        val destinationLevel = level.server.getLevel(destinationLevelKey) ?: return null

        // 座標の縮尺は、ディメンションタイプに設定された係数によって決まるのだ～🌱
        val scale = DimensionType.getTeleportationScale(level.dimensionType(), destinationLevel.dimensionType())
        val destinationY = if (destinationLevelKey == MANTLE_DIMENSION_KEY) MANTLE_DIMENSION_ARRIVAL_Y else pos.y
        val approximateBlockPos = destinationLevel.worldBorder.clampToBounds(entity.x * scale, destinationY.toDouble(), entity.z * scale)

        val axis = entity.level().getBlockState(pos).getOptionalValue(AXIS).orElse(Direction.Axis.X)
        val gateBlockPos = findOrCreateFairyQuestGate(destinationLevel, approximateBlockPos, axis) ?: return null

        val position = Vec3(gateBlockPos.x + 0.5, gateBlockPos.y.toDouble(), gateBlockPos.z + 0.5)
        return DimensionTransition(destinationLevel, position, Vec3.ZERO, entity.yRot, entity.xRot, DimensionTransition.PLAY_PORTAL_SOUND.then(DimensionTransition.PLACE_PORTAL_TICKET))
    }
}

/**
 * [approximateBlockPos] の近くにある既存のフェアリークエストゲートを探し、
 * 見つからなければ新しく作って、その内側の最下部の座標を返すのだ～🌱
 */
private fun findOrCreateFairyQuestGate(level: ServerLevel, approximateBlockPos: BlockPos, axis: Direction.Axis): BlockPos? {
    val searchRadius = 64

    // 既存のゲートを探すのだ～🌱
    var nearestBlockPos: BlockPos? = null
    var nearestDistance = Double.MAX_VALUE
    BlockPos.betweenClosedStream(approximateBlockPos.offset(-searchRadius, -searchRadius, -searchRadius), approximateBlockPos.offset(searchRadius, searchRadius, searchRadius)).forEach { blockPos ->
        if (!level.getBlockState(blockPos).`is`(MantleBlockCard.FAIRY_QUEST_GATE_PORTAL.block())) return@forEach
        val distance = blockPos.distSqr(approximateBlockPos)
        if (distance < nearestDistance) {
            nearestDistance = distance
            nearestBlockPos = blockPos.immutable()
        }
    }
    nearestBlockPos?.let { return it }

    // 見つからなかったので、新しく作るのだ～🌱
    val originBlockPos = findFairyQuestGatePlacement(level, approximateBlockPos) ?: return null
    placeFairyQuestGate(level, originBlockPos, axis)
    return originBlockPos
}

/** ゲートを設置できる、周囲が埋まりすぎていない場所を探すのだ～🌱 */
private fun findFairyQuestGatePlacement(level: ServerLevel, approximateBlockPos: BlockPos): BlockPos? {
    val minY = level.minBuildHeight + 1
    val maxY = level.maxBuildHeight - FAIRY_QUEST_GATE_HEIGHT - 2
    if (minY > maxY) return null
    val y = approximateBlockPos.y.coerceIn(minY, maxY)
    return BlockPos(approximateBlockPos.x, y, approximateBlockPos.z)
}

/**
 * [originBlockPos] を内側の最下部の角として、フェアリークエストゲートを設置するのだ～🌱
 *
 * ネザーポータルと同じく、内側を囲うフレームは、四隅を欠いた形なのだ～🌱
 */
fun placeFairyQuestGate(level: LevelAccessor, originBlockPos: BlockPos, axis: Direction.Axis) {
    val widthDirection = if (axis == Direction.Axis.X) Direction.EAST else Direction.SOUTH
    val frameBlockState = MantleBlockCard.FAIRY_QUEST_GATE.block().defaultBlockState()
    val portalBlockState = MantleBlockCard.FAIRY_QUEST_GATE_PORTAL.block().defaultBlockState().setValue(FairyQuestGatePortalBlock.AXIS, axis)

    fun blockPosOf(dw: Int, dy: Int) = originBlockPos.relative(widthDirection, dw).above(dy)

    // 内側をポータルで満たすのだ～🌱
    (0 until FAIRY_QUEST_GATE_WIDTH).forEach { dw ->
        (0 until FAIRY_QUEST_GATE_HEIGHT).forEach { dy ->
            level.setBlock(blockPosOf(dw, dy), portalBlockState, Block.UPDATE_CLIENTS)
        }
    }

    // 上下をフレームで塞ぐのだ～🌱
    (0 until FAIRY_QUEST_GATE_WIDTH).forEach { dw ->
        level.setBlock(blockPosOf(dw, -1), frameBlockState, Block.UPDATE_CLIENTS)
        level.setBlock(blockPosOf(dw, FAIRY_QUEST_GATE_HEIGHT), frameBlockState, Block.UPDATE_CLIENTS)
    }

    // 左右をフレームで塞ぐのだ～🌱
    (0 until FAIRY_QUEST_GATE_HEIGHT).forEach { dy ->
        level.setBlock(blockPosOf(-1, dy), frameBlockState, Block.UPDATE_CLIENTS)
        level.setBlock(blockPosOf(FAIRY_QUEST_GATE_WIDTH, dy), frameBlockState, Block.UPDATE_CLIENTS)
    }
}
