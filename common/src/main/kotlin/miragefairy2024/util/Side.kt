package miragefairy2024.util

import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level

object ServerSide

@Suppress("UnusedReceiverParameter")
val ServerPlayer.serverSide get() = ServerSide

@Suppress("UnusedReceiverParameter")
val ServerLevel.serverSide get() = ServerSide

val Level.serverSideOrNull get() = (this as? ServerLevel)?.serverSide

@Suppress("unused")
fun Player.asServerPlayer(serverSide: ServerSide) = this as ServerPlayer

@Suppress("unused")
fun Level.asServerLevel(serverSide: ServerSide) = this as ServerLevel
