package miragefairy2024.mod.tree

import net.minecraft.core.BlockPos
import net.minecraft.util.RandomSource
import net.minecraft.world.level.LevelSimulatedReader
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration
import java.util.function.BiConsumer

// バニラの TrunkPlacer.setDirtAt は、土でない地面を常に土へ置き換えるから、石化した樹脂のような地面を壊してしまうのだ～🌱
// 代わりに、置換可能なブロックだけを対象にすることで、幹が空中から始まる場合だけを埋めるのだ～🌱
fun setGroundAt(level: LevelSimulatedReader, blockSetter: BiConsumer<BlockPos, BlockState>, random: RandomSource, pos: BlockPos, config: TreeConfiguration) {
    if (level.isStateAtPosition(pos) { it.canBeReplaced() }) {
        blockSetter.accept(pos, config.dirtProvider.getState(random, pos))
    }
}
