package miragefairy2024.mod.materials.contents

import com.mojang.math.Axis
import com.mojang.serialization.MapCodec
import miragefairy2024.util.get
import miragefairy2024.util.isIn
import miragefairy2024.util.isNotIn
import miragefairy2024.util.with
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.sounds.SoundSource
import net.minecraft.util.Mth
import net.minecraft.util.RandomSource
import net.minecraft.util.StringRepresentable
import net.minecraft.world.InteractionHand
import net.minecraft.world.ItemInteractionResult
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.LevelEvent
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.EnumProperty
import net.minecraft.world.level.gameevent.GameEvent
import net.minecraft.world.level.pathfinder.PathComputationType
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape
import org.joml.Vector3d

class MiragidianLampBlock(settings: Properties) : Block(settings) {
    companion object {
        val CODEC: MapCodec<MiragidianLampBlock> = simpleCodec(::MiragidianLampBlock)
        val PART: EnumProperty<Part> = EnumProperty.create("part", Part::class.java)
        private val HEAD_SHAPE: VoxelShape = Shapes.or(
            box(7.0, 15.0, 7.0, 9.0, 16.0, 9.0), // hat
            box(5.0, 14.0, 5.0, 11.0, 15.0, 11.0), // hat
            box(2.0, 13.0, 2.0, 14.0, 14.0, 14.0), // hat
            box(3.0, 8.0, 3.0, 13.0, 13.0, 13.0), // lamp
            box(4.0, 3.0, 4.0, 12.0, 8.0, 12.0), // lamp
            box(6.0, 0.0, 6.0, 10.0, 3.0, 10.0), // pole
        )
        private val POLE_SHAPE: VoxelShape = box(6.0, 0.0, 6.0, 10.0, 16.0, 10.0)
        private val FOOT_SHAPE: VoxelShape = Shapes.or(
            box(6.0, 8.0, 6.0, 10.0, 16.0, 10.0), // pole
            box(4.0, 0.0, 4.0, 12.0, 8.0, 12.0), // foot
        )
    }

    override fun codec() = CODEC


    // BlockState

    enum class Part(val path: String, val connections: Set<Direction>) : StringRepresentable {
        HEAD("head", setOf(Direction.DOWN)),
        POLE("pole", setOf(Direction.UP, Direction.DOWN)),
        FOOT("foot", setOf(Direction.UP))
        ;

        override fun getSerializedName() = path
    }

    init {
        registerDefaultState(defaultBlockState().with(PART, Part.FOOT))
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(PART)
    }


    // Place

    override fun getStateForPlacement(context: BlockPlaceContext): BlockState? {
        val blockPos = context.clickedPos
        val level = context.level
        if (blockPos.y + 1 >= level.maxBuildHeight) return null
        if (!level.getBlockState(blockPos.above()).canBeReplaced(context)) return null
        return defaultBlockState().with(PART, Part.FOOT)
    }

    override fun setPlacedBy(level: Level, pos: BlockPos, state: BlockState, placer: LivingEntity?, stack: ItemStack) {
        level.setBlock(pos.above(), state.with(PART, Part.HEAD), 3)
    }


    // Remove

    override fun updateShape(state: BlockState, direction: Direction, neighborState: BlockState, level: LevelAccessor, pos: BlockPos, neighborPos: BlockPos): BlockState? {
        val part = state[PART]
        if (direction in part.connections) {
            return if (neighborState isIn this && direction.opposite in neighborState[PART].connections) {
                neighborState.with(PART, part)
            } else {
                Blocks.AIR.defaultBlockState()
            }
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos)
    }

    override fun playerWillDestroy(level: Level, pos: BlockPos, state: BlockState, player: Player): BlockState {
        if (!level.isClientSide && (player.isCreative || !player.hasCorrectToolForDrops(state))) {
            if (state[PART] != Part.FOOT) run {
                val parts = validate(level, pos) ?: return@run
                val footBlockState = level.getBlockState(parts.footBlockPos)
                level.setBlock(parts.footBlockPos, Blocks.AIR.defaultBlockState(), 1 or 2 or 32)
                level.levelEvent(player, LevelEvent.PARTICLES_DESTROY_BLOCK, parts.footBlockPos, getId(footBlockState))
            }
        }
        return super.playerWillDestroy(level, pos, state, player)
    }


    // Use

    override fun useItemOn(stack: ItemStack, state: BlockState, level: Level, pos: BlockPos, player: Player, hand: InteractionHand, hitResult: BlockHitResult): ItemInteractionResult? {
        if (stack isNotIn asItem()) return super.useItemOn(stack, state, level, pos, player, hand, hitResult)

        val parts = validate(level, pos) ?: return ItemInteractionResult.sidedSuccess(level.isClientSide)
        val newPoleBlockPos = parts.headBlockPos
        val newHeadBlockPos = parts.headBlockPos.above()
        if (newHeadBlockPos.y >= level.maxBuildHeight) return ItemInteractionResult.sidedSuccess(level.isClientSide)
        if (!level.getBlockState(newHeadBlockPos).canBeReplaced()) return ItemInteractionResult.sidedSuccess(level.isClientSide)

        if (level.isClientSide) return ItemInteractionResult.SUCCESS

        level.setBlock(newPoleBlockPos, defaultBlockState().with(PART, Part.POLE), 2)
        level.setBlock(newHeadBlockPos, defaultBlockState().with(PART, Part.HEAD), 2)
        level.blockUpdated(newPoleBlockPos, this)
        level.blockUpdated(newHeadBlockPos, this)
        level.gameEvent(player, GameEvent.BLOCK_CHANGE, newPoleBlockPos)
        level.gameEvent(player, GameEvent.BLOCK_CHANGE, newHeadBlockPos)
        // ミラジディアンのランプは無償で長さを伸長できる

        val soundType = defaultBlockState().with(PART, Part.HEAD).soundType
        level.playSound(null, newHeadBlockPos, soundType.placeSound, SoundSource.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F)

        return ItemInteractionResult.CONSUME
    }

    private class Parts(val headBlockPos: BlockPos, val poleBlockPoses: Set<BlockPos>, val footBlockPos: BlockPos)

    private fun validate(level: Level, blockPos: BlockPos): Parts? {
        val poleBlockPoses = mutableSetOf<BlockPos>()
        val headBlockPos = run {
            var y = blockPos.y
            while (true) {
                val blockPos2 = blockPos.atY(y)
                val blockState = level.getBlockState(blockPos2)
                if (blockState isNotIn this) return null
                when (blockState[PART]) {
                    Part.HEAD -> return@run blockPos2
                    Part.POLE -> poleBlockPoses += blockPos2
                    Part.FOOT -> Unit
                }
                y++
            }
            @Suppress("KotlinUnreachableCode")
            throw AssertionError()
        }
        val footBlockPos = run {
            var y = blockPos.y
            while (true) {
                val blockPos2 = blockPos.atY(y)
                val blockState = level.getBlockState(blockPos2)
                if (blockState isNotIn this) return null
                when (blockState[PART]) {
                    Part.HEAD -> Unit
                    Part.POLE -> poleBlockPoses += blockPos2
                    Part.FOOT -> return@run blockPos2
                }
                y--
            }
            @Suppress("KotlinUnreachableCode")
            throw AssertionError()
        }
        return Parts(headBlockPos, poleBlockPoses, footBlockPos)
    }


    // Shape

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape {
        return when (state[PART]) {
            Part.HEAD -> HEAD_SHAPE
            Part.POLE -> POLE_SHAPE
            Part.FOOT -> FOOT_SHAPE
        }
    }


    // Status

    @Suppress("OVERRIDE_DEPRECATION")
    override fun isPathfindable(state: BlockState, pathComputationType: PathComputationType) = false


    // Effect

    @Suppress("OVERRIDE_DEPRECATION")
    override fun animateTick(state: BlockState, level: Level, pos: BlockPos, random: RandomSource) {
        if (state[PART] == Part.HEAD) {
            if (random.nextInt(3) == 0) {
                val position = Vector3d(
                    2.0 + 12.0 * random.nextDouble(),
                    5.0 + 8.0 * random.nextDouble(),
                    2.0,
                ).also {
                    it.mul(1.0 / 16.0)
                    it.add(-0.5, -0.5, -0.5)
                    Axis.YP.rotation(Mth.HALF_PI * random.nextInt(4)).transform(it)
                    it.add(0.5, 0.5, 0.5)
                    it.add(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())
                }
                level.addParticle(ParticleTypes.END_ROD, position.x, position.y, position.z, 0.0, 0.0, 0.0)
            }
        }
    }

}
