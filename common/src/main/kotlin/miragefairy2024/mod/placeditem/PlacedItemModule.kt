package miragefairy2024.mod.placeditem

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.util.Channel
import miragefairy2024.util.EMPTY_ITEM_STACK
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.isNotIn
import miragefairy2024.util.obtain
import miragefairy2024.util.registerServerPacketReceiver
import net.minecraft.core.BlockPos
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.level.block.Block
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult

val PLACE_ITEM_KEY_TRANSLATION = Translation({ "key.${MirageFairy2024.MOD_ID}.place_item" }, "Place Item", "アイテムを置く")

context(ModContext)
fun initPlacedItemModule() {

    PLACE_ITEM_KEY_TRANSLATION.enJa()

    ModEvents.onInitialize {
        PlaceItemChannel.registerServerPacketReceiver { player, packet ->

            // パケットの正常性判定

            if (player.isSpectator) return@registerServerPacketReceiver // スペクテイターモード

            val hitResult = player.pick(player.blockInteractionRange(), 0F, false)
            if (hitResult.type != HitResult.Type.BLOCK) return@registerServerPacketReceiver // ブロックをターゲットにしていない
            if (hitResult !is BlockHitResult) return@registerServerPacketReceiver // ブロックをターゲットにしていない

            val blockPos = packet.blockPos
            val blockPos2 = if (player.level().getBlockState(hitResult.blockPos).canBeReplaced()) hitResult.blockPos else hitResult.blockPos.relative(hitResult.direction)
            if (blockPos != blockPos2) return@registerServerPacketReceiver // プレイヤーはその位置を見ていない

            if (packet.itemX !in 0.0..<1.0) return@registerServerPacketReceiver // 範囲外
            if (packet.itemY !in 0.0..<1.0) return@registerServerPacketReceiver // 範囲外
            if (packet.itemZ !in 0.0..<1.0) return@registerServerPacketReceiver // 範囲外


            // ブロックの設置判定

            val world = player.level()

            // 生成環境判定
            if (!world.getBlockState(blockPos).canBeReplaced()) return@registerServerPacketReceiver // 配置先が埋まっている

            // アイテム判定
            if (player.mainHandItem.isEmpty) return@registerServerPacketReceiver // アイテムを持っていない
            val itemStack = if (player.isCreative) player.mainHandItem.copyWithCount(1) else player.mainHandItem.split(1)


            // 成功

            world.setBlock(blockPos, PlacedItemCard.block().defaultBlockState(), Block.UPDATE_CLIENTS)
            val blockEntity = world.getBlockEntity(blockPos) as? PlacedItemBlockEntity ?: return@registerServerPacketReceiver // ブロックの配置に失敗した
            blockEntity.itemStack = itemStack
            blockEntity.itemX = packet.itemX
            blockEntity.itemY = packet.itemY
            blockEntity.itemZ = packet.itemZ
            blockEntity.itemRotateX = packet.itemRotateX
            blockEntity.itemRotateY = packet.itemRotateY
            blockEntity.updateShapeCache()
            blockEntity.setChanged()

            world.playSound(null, blockPos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, ((world.random.nextFloat() - world.random.nextFloat()) * 0.7F + 1.0F) * 2.0F)

        }
    }
    ModEvents.onInitialize {
        RemovePlacedItemChannel.registerServerPacketReceiver { player, packet ->

            // パケットの正常性判定

            if (player.isSpectator) return@registerServerPacketReceiver // スペクテイターモード

            val hitResult = player.pick(player.blockInteractionRange(), 0F, false)
            if (hitResult.type != HitResult.Type.BLOCK) return@registerServerPacketReceiver // ブロックをターゲットにしていない
            if (hitResult !is BlockHitResult) return@registerServerPacketReceiver // ブロックをターゲットにしていない

            val blockPos = packet.blockPos
            if (blockPos != hitResult.blockPos) return@registerServerPacketReceiver // プレイヤーはその位置を見ていない


            // ブロックの除去判定

            val world = player.level()

            if (world.getBlockState(blockPos) isNotIn PlacedItemCard.block()) return@registerServerPacketReceiver // ブロックが置かれていない
            val blockEntity = world.getBlockEntity(blockPos) as? PlacedItemBlockEntity ?: return@registerServerPacketReceiver // ブロックの取得に失敗した
            val itemStack = blockEntity.itemStack


            // 成功

            blockEntity.itemStack = EMPTY_ITEM_STACK
            world.removeBlock(blockPos, false)
            player.obtain(itemStack)

            // これを入れるとSEが2重に流れる
            //world.playSound(null, blockPos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, ((world.random.nextFloat() - world.random.nextFloat()) * 0.7F + 1.0F) * 2.0F)

        }
    }

    initPlacedItemBlock()

}

object PlaceItemChannel : Channel<PlaceItemChannel.Packet>(MirageFairy2024.identifier("place_item")) {

    class Packet(
        val blockPos: BlockPos,
        val itemX: Double,
        val itemY: Double,
        val itemZ: Double,
        val itemRotateX: Double,
        val itemRotateY: Double,
    )

    override fun writeToBuf(buf: RegistryFriendlyByteBuf, packet: Packet) {
        buf.writeInt(packet.blockPos.x)
        buf.writeInt(packet.blockPos.y)
        buf.writeInt(packet.blockPos.z)
        buf.writeDouble(packet.itemX)
        buf.writeDouble(packet.itemY)
        buf.writeDouble(packet.itemZ)
        buf.writeDouble(packet.itemRotateX)
        buf.writeDouble(packet.itemRotateY)
    }

    override fun readFromBuf(buf: RegistryFriendlyByteBuf): Packet {
        val blockPos = run {
            val x = buf.readInt()
            val y = buf.readInt()
            val z = buf.readInt()
            BlockPos(x, y, z)
        }
        val itemX = buf.readDouble()
        val itemY = buf.readDouble()
        val itemZ = buf.readDouble()
        val itemRotateX = buf.readDouble()
        val itemRotateY = buf.readDouble()
        return Packet(blockPos, itemX, itemY, itemZ, itemRotateX, itemRotateY)
    }

}

object RemovePlacedItemChannel : Channel<RemovePlacedItemChannel.Packet>(MirageFairy2024.identifier("remove_placed_item")) {

    class Packet(val blockPos: BlockPos)

    override fun writeToBuf(buf: RegistryFriendlyByteBuf, packet: Packet) {
        buf.writeInt(packet.blockPos.x)
        buf.writeInt(packet.blockPos.y)
        buf.writeInt(packet.blockPos.z)
    }

    override fun readFromBuf(buf: RegistryFriendlyByteBuf): Packet {
        val blockPos = run {
            val x = buf.readInt()
            val y = buf.readInt()
            val z = buf.readInt()
            BlockPos(x, y, z)
        }
        return Packet(blockPos)
    }

}
