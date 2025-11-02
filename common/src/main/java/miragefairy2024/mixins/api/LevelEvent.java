package miragefairy2024.mixins.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.Level;

public interface LevelEvent {
    Event<DestroyBlockProgressCallback> DESTROY_BLOCK_PROGRESS = EventFactory.createArrayBacked(DestroyBlockProgressCallback.class, callbacks -> (level, breakerId, pos, progress) -> {
        for (DestroyBlockProgressCallback callback : callbacks) {
            callback.destroyBlockProgress(level, breakerId, pos, progress);
        }
    });

    Event<HandlePlayerActionCallback> HANDLE_PLAYER_ACTION = EventFactory.createArrayBacked(HandlePlayerActionCallback.class, callbacks -> (listener, packet) -> {
        for (HandlePlayerActionCallback callback : callbacks) {
            callback.handlePlayerAction(listener, packet);
        }
    });

    interface DestroyBlockProgressCallback {
        void destroyBlockProgress(Level level, int breakerId, BlockPos pos, int progress);
    }

    interface HandlePlayerActionCallback {
        void handlePlayerAction(ServerGamePacketListenerImpl listener, ServerboundPlayerActionPacket packet);
    }
}
