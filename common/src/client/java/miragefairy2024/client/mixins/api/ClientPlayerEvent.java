package miragefairy2024.client.mixins.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;

public interface ClientPlayerEvent {
    Event<SameDestroyTargetCallback> SAME_DESTROY_TARGET = EventFactory.createArrayBacked(SameDestroyTargetCallback.class, callbacks -> (pos) -> {
        for (SameDestroyTargetCallback callback : callbacks) {
            if (!callback.sameDestroyTarget(pos)) return false;
        }
        return true;
    });

    interface SameDestroyTargetCallback {
        boolean sameDestroyTarget(BlockPos pos);
    }
}
