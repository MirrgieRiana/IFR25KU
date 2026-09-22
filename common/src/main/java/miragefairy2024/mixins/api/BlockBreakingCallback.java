package miragefairy2024.mixins.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public interface BlockBreakingCallback {
    Event<BlockBreakingCallback> EVENT = EventFactory.createArrayBacked(BlockBreakingCallback.class, callbacks -> (state, player, level, pos, blockBreakingDelta) -> {
        for (BlockBreakingCallback callback : callbacks) {
            blockBreakingDelta = callback.calcBlockBreakingDelta(state, player, level, pos, blockBreakingDelta);
        }
        return blockBreakingDelta;
    });

    float calcBlockBreakingDelta(BlockState state, Player player, BlockGetter level, BlockPos pos, float blockBreakingDelta);
}
