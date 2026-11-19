package miragefairy2024.client.mixins.mixin;

import miragefairy2024.client.mixins.api.SlotHovering;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin implements SlotHovering {
    @Inject(method = "isHovering(Lnet/minecraft/world/inventory/Slot;DD)Z", at = @At("RETURN"), cancellable = true)
    private void isHovering(Slot slot, double mouseX, double mouseY, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(mirageFairy2024$isHovering(slot, mouseX, mouseY, cir.getReturnValueZ()));
    }
}
