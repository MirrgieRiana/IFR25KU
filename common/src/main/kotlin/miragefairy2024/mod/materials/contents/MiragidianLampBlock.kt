package miragefairy2024.mod.materials.contents

import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.util.StringRepresentable
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.EnumProperty
import net.minecraft.world.level.pathfinder.PathComputationType
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape

class MiragidianLampBlock(settings: Properties) : Block(settings) {
    companion object {
        val CODEC: MapCodec<MiragidianLampBlock> = simpleCodec(::MiragidianLampBlock)
        val PART: EnumProperty<Part> = EnumProperty.create("part", Part::class.java)
        private val SHAPE_FOOT: VoxelShape = box(5.0, 0.0, 5.0, 11.0, 4.0, 11.0)
        private val SHAPE_POLE: VoxelShape = box(7.0, 0.0, 7.0, 9.0, 16.0, 9.0)
        private val SHAPE_HEAD: VoxelShape = Shapes.or(
            box(6.0, 0.0, 6.0, 10.0, 12.0, 10.0),
            box(7.0, 12.0, 7.0, 9.0, 16.0, 9.0),
            box(7.0, 15.0, 7.0, 9.0, 16.0, 9.0),
        )
    }

    override fun codec() = CODEC

    enum class Part(val path: String) : StringRepresentable {
        HEAD("head"),
        POLE("pole"),
        FOOT("foot"),
        ;

        override fun getSerializedName() = path
    }

    init {
        registerDefaultState(defaultBlockState().setValue(PART, Part.FOOT))
    }

    override fun getStateForPlacement(context: BlockPlaceContext): BlockState? {
        val world = context.level
        val basePos = context.clickedPos
        if (!world.getBlockState(basePos.above()).isAir) return null
        if (!world.getBlockState(basePos.above(2)).isAir) return null
        return defaultBlockState().setValue(PART, Part.FOOT)
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(PART)
    }

    override fun setPlacedBy(world: Level, pos: BlockPos, state: BlockState, placer: LivingEntity?, itemStack: ItemStack) {
        super.setPlacedBy(world, pos, state, placer, itemStack)
        if (world.isClientSide) return
        if (state.getValue(PART) != Part.FOOT) return
        val polePos = pos.above()
        val headPos = pos.above(2)
        if (world.getBlockState(polePos).isAir && world.getBlockState(headPos).isAir) {
            world.setBlock(polePos, defaultBlockState().setValue(PART, Part.POLE), Block.UPDATE_CLIENTS)
            world.setBlock(headPos, defaultBlockState().setValue(PART, Part.HEAD), Block.UPDATE_CLIENTS)
        } else {
            world.removeBlock(pos, false)
        }
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun useWithoutItem(state: BlockState, world: Level, pos: BlockPos, player: Player, hitResult: BlockHitResult): InteractionResult {
        if (world.isClientSide) return InteractionResult.SUCCESS
        val headPos = findHead(world, pos) ?: return InteractionResult.PASS
        val newPolePos = headPos.above()
        val newHeadPos = headPos.above(2)
        if (!world.getBlockState(newPolePos).isAir) return InteractionResult.PASS
        if (!world.getBlockState(newHeadPos).isAir) return InteractionResult.PASS
        if (!player.isCreative) {
            val hand = player.mainHandItem
            if (hand.item != this.asItem()) return InteractionResult.PASS
            hand.shrink(1)
        }
        world.setBlock(headPos, defaultBlockState().setValue(PART, Part.POLE), Block.UPDATE_CLIENTS)
        world.setBlock(newPolePos, defaultBlockState().setValue(PART, Part.POLE), Block.UPDATE_CLIENTS)
        world.setBlock(newHeadPos, defaultBlockState().setValue(PART, Part.HEAD), Block.UPDATE_CLIENTS)
        return InteractionResult.CONSUME
    }

    private fun findHead(world: Level, origin: BlockPos): BlockPos? {
        val startState = world.getBlockState(origin)
        return when (startState.getValue(PART)) {
            Part.HEAD -> origin
            Part.POLE -> {
                var p = origin
                while (true) {
                    p = p.above()
                    val s = world.getBlockState(p)
                    if (s.block != this) break
                    if (s.getValue(PART) == Part.HEAD) return p
                }
                null
            }

            Part.FOOT -> {
                val head = origin.above(2)
                val s = world.getBlockState(head)
                if (s.block == this && s.getValue(PART) == Part.HEAD) head else null
            }
        }
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getShape(state: BlockState, world: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape = when (state.getValue(PART)) {
        Part.FOOT -> SHAPE_FOOT
        Part.POLE -> SHAPE_POLE
        Part.HEAD -> SHAPE_HEAD
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun isPathfindable(state: BlockState, pathComputationType: PathComputationType) = false

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onRemove(state: BlockState, world: Level, pos: BlockPos, newState: BlockState, moved: Boolean) {
        if (state.block == newState.block) {
            @Suppress("DEPRECATION") super.onRemove(state, world, pos, newState, moved)
            return
        }
        if (!world.isClientSide) when (state.getValue(PART)) {
            Part.FOOT -> {
                removeIfLamp(world, pos.above())
                removeIfLamp(world, pos.above(2))
            }

            Part.POLE -> {
                val below = pos.below()
                val belowState = world.getBlockState(below)
                if (belowState.block == this && belowState.getValue(PART) == Part.FOOT) {
                    removeIfLamp(world, below.above())
                    removeIfLamp(world, below.above(2))
                } else {
                    removeIfLamp(world, pos.above())
                    removeIfLamp(world, pos.below())
                }
            }

            Part.HEAD -> {
                removeIfLamp(world, pos.below())
                removeIfLamp(world, pos.below(2))
            }
        }
        @Suppress("DEPRECATION") super.onRemove(state, world, pos, newState, moved)
    }

    private fun removeIfLamp(world: Level, pos: BlockPos) {
        val s = world.getBlockState(pos); if (s.block == this) world.removeBlock(pos, false)
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun animateTick(state: BlockState, world: Level, pos: BlockPos, random: net.minecraft.util.RandomSource) {
        if (state.getValue(PART) != Part.HEAD) return
        val cx = pos.x + 0.5
        val cy = pos.y + 14.5 / 16.0
        val cz = pos.z + 0.5
        repeat(2) { world.addParticle(ParticleTypes.END_ROD, cx, cy, cz, (random.nextDouble() - 0.5) * 0.02, random.nextDouble() * 0.02, (random.nextDouble() - 0.5) * 0.02) }
    }
}
