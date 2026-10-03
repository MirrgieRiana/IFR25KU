package miragefairy2024.client.mixins.mixin;

import miragefairy2024.mixins.api.NoSlowdownWhileUsingItem;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
    @Redirect(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z"))
    private boolean isUsingItemForSlowdown(LocalPlayer instance) {
        if (!instance.isUsingItem()) return false;
        return !(instance.getUseItem().getItem() instanceof NoSlowdownWhileUsingItem);
    }
}
