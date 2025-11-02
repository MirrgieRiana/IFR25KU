package miragefairy2024.client.mixins.mixin;

import miragefairy2024.mixins.api.LevelEvent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {
    @Inject(method = "destroyBlockProgress", at = @At(value = "HEAD"))
    private void destroyBlockProgress(int breakerId, BlockPos pos, int progress, CallbackInfo ci) {
        LevelEvent.DESTROY_BLOCK_PROGRESS.invoker().destroyBlockProgress((ClientLevel) (Object) this, breakerId, pos, progress);
    }
}
