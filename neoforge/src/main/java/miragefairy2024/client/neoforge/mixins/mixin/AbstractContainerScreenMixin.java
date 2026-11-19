package miragefairy2024.client.neoforge.mixins.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import miragefairy2024.client.mixins.api.SlotViewClipping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin {
    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderSlotHighlight(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/inventory/Slot;IIF)V", remap = false))
    private void mirageFairy2024$renderNeoForgeSlotHighlight(AbstractContainerScreen<?> screen, GuiGraphics guiGraphics, Slot slot, int mouseX, int mouseY, float partialTick, Operation<Void> original) {
        ((SlotViewClipping) slot).mirageFairy2024$renderWithSlotClip(guiGraphics, () -> original.call(screen, guiGraphics, slot, mouseX, mouseY, partialTick));
    }
}
