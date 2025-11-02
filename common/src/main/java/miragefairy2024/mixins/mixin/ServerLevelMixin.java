package miragefairy2024.mixins.mixin;

import miragefairy2024.mixins.api.LevelEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {
    @Inject(method = "destroyBlockProgress", at = @At(value = "HEAD"))
    private void destroyBlockProgress(int breakerId, BlockPos pos, int progress, CallbackInfo ci) {
        LevelEvent.DESTROY_BLOCK_PROGRESS.invoker().destroyBlockProgress((ServerLevel) (Object) this, breakerId, pos, progress);
    }
}
