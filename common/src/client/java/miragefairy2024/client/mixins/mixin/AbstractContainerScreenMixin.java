package miragefairy2024.client.mixins.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import miragefairy2024.client.mixins.api.SlotHovering;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin implements SlotHovering {
    @ModifyReturnValue(method = "isHovering(Lnet/minecraft/world/inventory/Slot;DD)Z", at = @At("RETURN"))
    private boolean isHovering(boolean hovering, Slot slot, double mouseX, double mouseY) {
        return mirageFairy2024$isHovering(slot, mouseX, mouseY, hovering);
    }
}
