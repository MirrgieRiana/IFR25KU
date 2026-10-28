package miragefairy2024.client.mixins.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import miragefairy2024.client.mixins.api.SlotViewClipping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin {

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderSlot(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/inventory/Slot;)V"))
    private void mirageFairy2024$renderSlot(AbstractContainerScreen<?> screen, GuiGraphics guiGraphics, Slot slot, Operation<Void> original) {
        mirageFairy2024$withSlotClip(guiGraphics, slot, () -> original.call(screen, guiGraphics, slot));
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
}
