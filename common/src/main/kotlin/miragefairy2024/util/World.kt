package miragefairy2024.util

import miragefairy2024.mod.SoundEventCard
import mirrg.kotlin.helium.max
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundSource
import net.minecraft.tags.BlockTags
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LightLayer
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.FarmBlock
import net.minecraft.world.level.block.GameMasterBlock
import net.minecraft.world.level.levelgen.structure.BoundingBox
import net.minecraft.world.phys.AABB

val Level.isServer get() = !this.isClientSide

fun BlockGetter.getMoisture(blockPos: BlockPos): Double {
    val blockState = this.getBlockState(blockPos)
    if (blockState isIn Blocks.FARMLAND) return 0.5 + 0.5 * (blockState.getValue(FarmBlock.MOISTURE) / 7.0)
    if (blockState isIn BlockTags.DIRT) return 0.5
    if (blockState isIn BlockTags.SAND) return 0.25
    return 0.0
}

fun BlockGetter.getCrystalErg(blockPos: BlockPos): Double {
    // TODO 妖精の継承を使って判定
    return when (getBlockState(blockPos).block) {

        Blocks.DIAMOND_BLOCK -> 1.0

        Blocks.EMERALD_BLOCK -> 0.8
        Blocks.AMETHYST_BLOCK -> 0.8

        Blocks.GOLD_BLOCK -> 0.6
        Blocks.QUARTZ_BLOCK -> 0.6

        Blocks.LAPIS_BLOCK -> 0.4
        Blocks.REDSTONE_BLOCK -> 0.4
        Blocks.IRON_BLOCK -> 0.4

        Blocks.COAL_BLOCK -> 0.2
        Blocks.COPPER_BLOCK -> 0.2

        else -> 0.0
    }
}

enum class NeighborType {
    FACES,
    EDGES,
    VERTICES,
}

fun blockVisitor(
    originalBlockPosList: Iterable<BlockPos>,
    visitOrigins: Boolean = true,
    maxDistance: Int = Int.MAX_VALUE,
    maxCount: Int? = null,
    neighborType: NeighborType = NeighborType.FACES,
    predicate: (distance: Int, fromBlockPos: BlockPos, toBlockPos: BlockPos) -> Boolean,
) = sequence {
    val checkedBlockPosList = originalBlockPosList.toMutableSet()
    var nextBlockPosList = originalBlockPosList.toMutableSet()
    var count = 0

    if (maxCount == 0) return@sequence

    (0..maxDistance).forEach { distance ->
        if (nextBlockPosList.isEmpty()) return@sequence

        val currentBlockPosList: Set<BlockPos> = nextBlockPosList
        nextBlockPosList = mutableSetOf()

        val nextDistance = distance + 1
        currentBlockPosList.forEach nextCurrentBlockPos@{ fromBlockPos ->

            if (distance > 0 || visitOrigins) {
                yield(Pair(distance, fromBlockPos))
                count++
                if (maxCount != null && count >= maxCount) return@sequence
            }

            fun check(offsetX: Int, offsetY: Int, offsetZ: Int) {
                val toBlockPos = fromBlockPos.offset(offsetX, offsetY, offsetZ)
                if (toBlockPos !in checkedBlockPosList && predicate(nextDistance, fromBlockPos, toBlockPos)) {
                    checkedBlockPosList += toBlockPos
                    nextBlockPosList += toBlockPos
                }
            }

            check(1, 0, 0)
            check(-1, 0, 0)
            check(0, 1, 0)
            check(0, -1, 0)
            check(0, 0, 1)
            check(0, 0, -1)
            if (neighborType != NeighborType.FACES) {
                check(0, 1, 1)
                check(0, 1, -1)
                check(0, -1, 1)
                check(0, -1, -1)
                check(1, 0, 1)
                check(1, 0, -1)
                check(-1, 0, 1)
                check(-1, 0, -1)
                check(1, 1, 0)
                check(1, -1, 0)
                check(-1, 1, 0)
                check(-1, -1, 0)
                if (neighborType != NeighborType.EDGES) {
                    check(1, 1, 1)
                    check(1, 1, -1)
                    check(1, -1, 1)
                    check(1, -1, -1)
                    check(-1, 1, 1)
                    check(-1, 1, -1)
                    check(-1, -1, 1)
                    check(-1, -1, -1)
                }
            }
        }

    }

}

/**
 * プレイヤーの動作としてブロックを壊します。
 *
 * [ServerPlayerInteractionManager.tryBreakBlock]とは以下の点で異なります。
 * - ブロックの硬度が無限の場合、無効になる。
 */
fun breakBlock(itemStack: ItemStack, world: Level, blockPos: BlockPos, player: ServerPlayer): Boolean {
    val blockState = world.getBlockState(blockPos)
    if (!itemStack.item.canAttackBlock(blockState, world, blockPos, player)) return false // このツールは採掘そのものができない
    val blockEntity = world.getBlockEntity(blockPos)
    val block = blockState.block

    if (blockState.getDestroySpeed(world, blockPos) < 0F) return false // このブロックは破壊不能
    if (block is GameMasterBlock && !player.canUseGameMasterBlocks()) {
        world.sendBlockUpdated(blockPos, blockState, blockState, Block.UPDATE_ALL)
        return false // コマンドブロックを破壊しようとした
    }
    if (player.blockActionRestricted(world, blockPos, player.gameMode.gameModeForPlayer)) return false // 破壊する権限がない

    player.connection.send(ClientboundLevelEventPacket(2001, blockPos, Block.getId(blockState), false)) // playerWillDestroyは採掘者本人にはエフェクトを送らない
    block.playerWillDestroy(world, blockPos, blockState, player)
    val success = world.removeBlock(blockPos, false)
    if (success) block.destroy(world, blockPos, blockState)
    if (player.isCreative) return true // クリエイティブの場合、ドロップを省略
    val newItemStack = itemStack.copy()
    val canHarvest = player.hasCorrectToolForDrops(blockState)
    itemStack.mineBlock(world, blockState, blockPos, player)
    if (success && canHarvest) block.playerDestroy(world, player, blockPos, blockState, blockEntity, newItemStack)
    return true
}

val isInMagicMining: ThreadLocal<Boolean> = ThreadLocal.withInitial { false }

/**
 * 魔法効果としてブロックを壊します。
 *
 * [breakBlock]とは以下の点で異なります。
 * - 近接武器の採掘不能特性を無視します。
 * - 専用のツールが必要なブロックを、ツールの種類にかかわらず回収可能です。
 * - [Item.mineBlock]を起動せず、アイテムの耐久値の減少などが発生しません。
 * - 魔法効果による採掘中に再帰的に呼び出された場合、例外が発生します。
 */
fun breakBlockByMagic(itemStack: ItemStack, world: Level, blockPos: BlockPos, player: ServerPlayer): Boolean {
    if (isInMagicMining.get()) throw IllegalStateException("Tried to magically mine while already in magic mining.")
    isInMagicMining.set(true)
    try {
        val blockState = world.getBlockState(blockPos)
        val blockEntity = world.getBlockEntity(blockPos)
        val block = blockState.block

        if (blockState.getDestroySpeed(world, blockPos) < 0F) return false // このブロックは破壊不能
        if (block is GameMasterBlock && !player.canUseGameMasterBlocks()) {
            world.sendBlockUpdated(blockPos, blockState, blockState, Block.UPDATE_ALL)
            return false // コマンドブロックを破壊しようとした
        }
        if (player.blockActionRestricted(world, blockPos, player.gameMode.gameModeForPlayer)) return false // 破壊する権限がない

        player.connection.send(ClientboundLevelEventPacket(2001, blockPos, Block.getId(blockState), false)) // playerWillDestroyは採掘者本人にはエフェクトを送らない
        block.playerWillDestroy(world, blockPos, blockState, player)
        val success = world.removeBlock(blockPos, false)
        if (success) block.destroy(world, blockPos, blockState)
        if (player.isCreative) return true // クリエイティブの場合、ドロップを省略
        val newItemStack = itemStack.copy()
        if (success) block.playerDestroy(world, player, blockPos, blockState, blockEntity, newItemStack)
        return true
    } finally {
        isInMagicMining.set(false)
    }
}

fun collectItem(
    world: Level,
    originalBlockPos: BlockPos,
    reach: Int = Int.MAX_VALUE,
    region: BoundingBox? = null,
    maxCount: Int = Int.MAX_VALUE,
    ignoreOriginalWall: Boolean = false,
    predicate: (ItemEntity) -> Boolean = { true },
    process: (ItemEntity) -> Boolean,
) {
    val box = when {
        region != null -> AABB.of(region)
        reach != Int.MAX_VALUE -> AABB(originalBlockPos).inflate(reach.toDouble())
        else -> AABB.of(BoundingBox.infinite())
    }
    val targetTable = world.getEntitiesOfClass(ItemEntity::class.java, box) {
        it.isValid && predicate(it)
    }.groupBy { it.blockPosition() }

    var remainingAmount = maxCount
    var processedCount = 0
    if (targetTable.isNotEmpty()) run finish@{
        blockVisitor(listOf(originalBlockPos), maxDistance = reach) { _, fromBlockPos, toBlockPos ->
            if (region != null && !region.isInside(toBlockPos)) return@blockVisitor false
            val offset = toBlockPos.subtract(fromBlockPos)
            val direction = when {
                offset.y == -1 -> Direction.DOWN
                offset.y == 1 -> Direction.UP
                offset.z == -1 -> Direction.NORTH
                offset.z == 1 -> Direction.SOUTH
                offset.x == -1 -> Direction.WEST
                offset.x == 1 -> Direction.EAST
                else -> throw AssertionError()
            }
            if (ignoreOriginalWall && fromBlockPos == originalBlockPos) {
                !world.getBlockState(toBlockPos).isFaceSturdy(world, toBlockPos, direction.opposite)
            } else {
                !world.getBlockState(toBlockPos).isFaceSturdy(world, toBlockPos, direction.opposite) && !world.getBlockState(fromBlockPos).isFaceSturdy(world, fromBlockPos, direction)
            }
        }.forEach { (_, blockPos) ->
            targetTable[blockPos]?.forEach {

                val doNext = process(it)

                processedCount++

                remainingAmount--
                if (remainingAmount <= 0) return@finish

                if (!doNext) return@finish
            }
        }
    }

    if (processedCount > 0) {

        // Effect
        val pos = originalBlockPos.center
        world.playSound(null, pos.x, pos.y, pos.z, SoundEventCard.COLLECT.soundEvent, SoundSource.PLAYERS, 0.15F, 0.8F + (world.random.nextFloat() - 0.5F) * 0.5F)

    }

}

class LightProxy(val level: Level) {

    fun getLightLevel(blockPos: BlockPos): Int {
        if (level.isClientSide) level.updateSkyBrightness()
        return level.getMaxLocalRawBrightness(blockPos)
    }

    fun getPermanentLightLevel(blockPos: BlockPos): Int {
        return getBlockLightLevel(blockPos) max getPermanentSkyLightLevel(blockPos)
    }

    fun getBlockLightLevel(blockPos: BlockPos): Int {
        return level.getBrightness(LightLayer.BLOCK, blockPos)
    }

    fun getPermanentSkyLightLevel(blockPos: BlockPos): Int {
        return level.getBrightness(LightLayer.SKY, blockPos)
    }

    fun getSkyDarken(): Int {
        if (level.isClientSide) level.updateSkyBrightness()
        return level.skyDarken
    }

}

val Level.lightProxy get() = LightProxy(this)
