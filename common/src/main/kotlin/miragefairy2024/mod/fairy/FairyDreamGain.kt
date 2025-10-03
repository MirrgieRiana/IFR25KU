package miragefairy2024.mod.fairy

import miragefairy2024.ModContext
import miragefairy2024.util.eyeBlockPos
import miragefairy2024.util.getOrCreate
import miragefairy2024.util.itemStacks
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.core.BlockPos
import net.minecraft.util.Mth
import net.minecraft.world.Container
import net.minecraft.world.WorldlyContainerHolder
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.ChestBlock
import net.minecraft.world.level.block.entity.ChestBlockEntity
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.HitResult

context(ModContext)
fun initFairyDreamGain() {

    // 妖精の夢回収判定
    ServerTickEvents.END_SERVER_TICK.register { server ->
        if (server.tickCount % (20 * 5) == 0) {
            server.playerList.players.forEach { player ->
                if (player.isSpectator) return@forEach
                if (player.tickCount < 20 * 60) return@forEach
                val world = player.level()
                val random = world.random

                val motifs = mutableSetOf<Motif>()

                val items = mutableSetOf<Item>()
                val blocks = mutableSetOf<Block>()
                val entityTypes = mutableSetOf<EntityType<*>>()
                run {

                    fun insertItem(itemStack: ItemStack) {
                        val item = itemStack.item

                        items += item

                        if (item is FairyDreamProviderItem) motifs += item.getFairyDreamMotifs(itemStack)

                        val block = Block.byItem(item)
                        if (block != Blocks.AIR) blocks += block

                    }

                    fun insertBlockPos(blockPos: BlockPos) {
                        val blockState = world.getBlockState(blockPos)
                        val block = blockState.block

                        blocks += block

                        if (block is FairyDreamProviderBlock) motifs += block.getFairyDreamMotifs(world, blockPos)

                        run noInventory@{
                            val inventory = if (block is WorldlyContainerHolder) {
                                block.getContainer(blockState, world, blockPos)
                            } else if (blockState.hasBlockEntity()) {
                                val blockEntity = world.getBlockEntity(blockPos)
                                if (blockEntity is Container) {
                                    if (blockEntity is ChestBlockEntity && block is ChestBlock) {
                                        ChestBlock.getContainer(block, blockState, world, blockPos, true) ?: return@noInventory
                                    } else {
                                        blockEntity
                                    }
                                } else {
                                    return@noInventory
                                }
                            } else {
                                return@noInventory
                            }
                            inventory.itemStacks.forEach { itemStack ->
                                insertItem(itemStack)
                            }
                        }

                    }


                    // インベントリ判定
                    player.inventory.itemStacks.forEach { itemStack ->
                        insertItem(itemStack)
                    }

                    // 足元判定
                    insertBlockPos(player.blockPosition())
                    insertBlockPos(player.blockPosition().below())

                    // 視線判定
                    val start = player.eyePosition
                    val pitch = player.xRot
                    val yaw = player.yRot
                    val d = Mth.cos(-yaw * (Mth.PI / 180) - Mth.PI)
                    val a = Mth.sin(-yaw * (Mth.PI / 180) - Mth.PI)
                    val e = -Mth.cos(-pitch * (Mth.PI / 180))
                    val c = Mth.sin(-pitch * (Mth.PI / 180))
                    val end = start.add(a * e * 32.0, c * 32.0, d * e * 32.0)
                    val raycastResult = world.clip(ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player))
                    if (raycastResult.type == HitResult.Type.BLOCK) insertBlockPos(raycastResult.blockPos)

                    // 周辺エンティティ判定
                    val entities = world.getEntities(player, AABB(player.eyePosition.add(-8.0, -8.0, -8.0), player.eyePosition.add(8.0, 8.0, 8.0)))
                    entities.forEach {
                        entityTypes += it.type
                    }

                    // 周辺ブロック判定
                    insertBlockPos(player.eyeBlockPos.offset(random.nextInt(17) - 8, random.nextInt(17) - 8, random.nextInt(17) - 8))

                }
                items.forEach {
                    motifs += FairyDreamRecipes.ITEM.test(it)
                }
                blocks.forEach {
                    motifs += FairyDreamRecipes.BLOCK.test(it)
                }
                entityTypes.forEach {
                    motifs += FairyDreamRecipes.ENTITY_TYPE.test(it)
                }

                player.fairyDreamContainer.getOrCreate().gain(player, motifs)

            }
        }
    }

}
