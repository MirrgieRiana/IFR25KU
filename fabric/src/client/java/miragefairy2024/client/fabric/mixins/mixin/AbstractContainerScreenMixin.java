package miragefairy2024.client.fabric.mixins.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import miragefairy2024.client.mixins.api.SlotViewClipping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin {
    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderSlotHighlight(Lnet/minecraft/client/gui/GuiGraphics;III)V"))
    private void mirageFairy2024$renderSlotHighlight(GuiGraphics guiGraphics, int x, int y, int blitOffset, Operation<Void> original, @Local(name = "slot") Slot slot) {
        ((SlotViewClipping) slot).mirageFairy2024$renderWithSlotClip(guiGraphics, () -> original.call(guiGraphics, x, y, blitOffset));
    }
}
