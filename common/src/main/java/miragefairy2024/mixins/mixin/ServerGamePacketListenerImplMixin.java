package miragefairy2024.mixins.mixin;

import miragefairy2024.mixins.api.LevelEvent;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {
    @Inject(method = "handlePlayerAction", at = @At("HEAD"))
    private void handlePlayerAction(ServerboundPlayerActionPacket packet, CallbackInfo ci) {
        LevelEvent.HANDLE_PLAYER_ACTION.invoker().handlePlayerAction((ServerGamePacketListenerImpl) (Object) this, packet);
    }
}
