package miragefairy2024.client.mixins.mixin;

import miragefairy2024.client.mixins.api.ClientPlayerEvent;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {
    @Inject(method = "sameDestroyTarget", at = @At(value = "HEAD"), cancellable = true)
    private void sameDestroyTarget(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        boolean result = ClientPlayerEvent.SAME_DESTROY_TARGET.invoker().sameDestroyTarget(pos);
        if (!result) cir.setReturnValue(false);
    }
}
