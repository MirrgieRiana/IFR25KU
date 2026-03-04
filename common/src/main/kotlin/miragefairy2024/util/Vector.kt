package miragefairy2024.util

import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Block
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape

fun Vec3.toBlockPos(): BlockPos = BlockPos.containing(this)

fun BlockPos.toBox() = AABB(this)

fun createCuboidShape(radius: Double, height: Double): VoxelShape = Block.box(8 - radius, 0.0, 8 - radius, 8 + radius, height, 8 + radius)

fun VoxelShape.rotateY90(): VoxelShape = toAabbs().map { Shapes.box(1 - it.maxZ, it.minY, it.minX, 1 - it.minZ, it.maxY, it.maxX) }.reduce(Shapes::or)
