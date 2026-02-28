package miragefairy2024.mod.materials.contents

import com.mojang.serialization.MapCodec
import miragefairy2024.mod.tool.MirageLeavesDamageTypeCard
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState

class MirageLeavesBlock(settings: Properties) : Block(settings) {
    companion object {
        val CODEC: MapCodec<MirageLeavesBlock> = simpleCodec(::MirageLeavesBlock)
    }

    override fun codec() = CODEC

    override fun playerWillDestroy(level: Level, pos: BlockPos, state: BlockState, player: Player): BlockState {
        run {
            if (level.isClientSide) return@run
            if (player.isCreative) return@run
            if (player.mainHandItem.isEmpty) {
                player.hurt(level.damageSources().source(MirageLeavesDamageTypeCard.registryKey), 1.0F)
            }
        }
        return super.playerWillDestroy(level, pos, state, player)
    }
}
