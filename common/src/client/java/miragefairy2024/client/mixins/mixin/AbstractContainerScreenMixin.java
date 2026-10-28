package miragefairy2024.client.mixins.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import miragefairy2024.client.mixins.api.SlotViewClipping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin {

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderSlot(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/inventory/Slot;)V"))
    private void mirageFairy2024$renderSlot(AbstractContainerScreen<?> screen, GuiGraphics guiGraphics, Slot slot, Operation<Void> original) {
        mirageFairy2024$withSlotClip(guiGraphics, slot, () -> original.call(screen, guiGraphics, slot));
    }

    @Group(name = "mirageFairy2024$slotHighlight", min = 1)
    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderSlotHighlight(Lnet/minecraft/client/gui/GuiGraphics;III)V"), require = 0)
    private void mirageFairy2024$renderSlotHighlight(GuiGraphics guiGraphics, int x, int y, int blitOffset, Operation<Void> original, @Local Slot slot) {
        mirageFairy2024$withSlotClip(guiGraphics, slot, () -> original.call(guiGraphics, x, y, blitOffset));
    }

    @Group(name = "mirageFairy2024$slotHighlight", min = 1)
    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderSlotHighlight(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/inventory/Slot;IIF)V", remap = false), require = 0)
    private void mirageFairy2024$renderNeoForgeSlotHighlight(AbstractContainerScreen<?> screen, GuiGraphics guiGraphics, Slot slot, int mouseX, int mouseY, float partialTick, Operation<Void> original) {
        mirageFairy2024$withSlotClip(guiGraphics, slot, () -> original.call(screen, guiGraphics, slot, mouseX, mouseY, partialTick));
    }

    @Unique
    private static void mirageFairy2024$withSlotClip(GuiGraphics guiGraphics, Slot slot, Runnable render) {
        var provider = ((SlotViewClipping) slot).mirageFairy2024$getClipBounds();
        if (provider == null) {
            render.run();
            return;
        }
        var bounds = provider.get();
        guiGraphics.enableScissor(bounds.getX(), bounds.getY(), bounds.getX() + bounds.getSizeX(), bounds.getY() + bounds.getSizeY());
        try {
            render.run();
        } finally {
            guiGraphics.disableScissor();
        }
    }

    @Inject(method = "isHovering(Lnet/minecraft/world/inventory/Slot;DD)Z", at = @At("RETURN"), cancellable = true)
    private void mirageFairy2024$isHovering(Slot slot, double mouseX, double mouseY, CallbackInfoReturnable<Boolean> cir) {
        if (!((SlotViewClipping) slot).mirageFairy2024$isInsideClip(mouseX, mouseY)) {
            cir.setReturnValue(false);
        }
    }
}
