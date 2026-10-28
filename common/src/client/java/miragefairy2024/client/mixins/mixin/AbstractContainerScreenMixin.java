package miragefairy2024.client.mixins.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import miragefairy2024.client.mixins.api.SlotViewClipping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin {
    @Shadow
    @Nullable
    protected Slot hoveredSlot;

    @Shadow
    private boolean isHovering(Slot slot, double mouseX, double mouseY) {
        throw new AssertionError();
    }

    @Unique
    private int mirageFairy2024$lastMouseX;

    @Unique
    private int mirageFairy2024$lastMouseY;

    @Inject(method = "render", at = @At("HEAD"))
    private void mirageFairy2024$captureMouse(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        mirageFairy2024$lastMouseX = mouseX;
        mirageFairy2024$lastMouseY = mouseY;
    }

    @Inject(method = {"keyPressed", "mouseClicked"}, at = @At("HEAD"))
    private void mirageFairy2024$clearHiddenHoveredSlot(CallbackInfoReturnable<Boolean> cir) {
        // スクロール後、次の描画より先に入力されても、以前ホバーしたスロットへショートカットを送らないのだ～🌱
        if (hoveredSlot != null && ((SlotViewClipping) hoveredSlot).mirageFairy2024$getClipBounds() != null && !isHovering(hoveredSlot, mirageFairy2024$lastMouseX, mirageFairy2024$lastMouseY)) {
            hoveredSlot = null;
        }
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderSlot(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/inventory/Slot;)V"))
    private void mirageFairy2024$renderSlot(AbstractContainerScreen<?> screen, GuiGraphics guiGraphics, Slot slot, Operation<Void> original) {
        mirageFairy2024$renderWithSlotClip(guiGraphics, slot, () -> original.call(screen, guiGraphics, slot));
    }

    @Group(name = "mirageFairy2024$slotHighlight", min = 1)
    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderSlotHighlight(Lnet/minecraft/client/gui/GuiGraphics;III)V"), require = 0)
    private void mirageFairy2024$renderSlotHighlight(GuiGraphics guiGraphics, int x, int y, int blitOffset, Operation<Void> original, @Local(name = "slot") Slot slot) {
        mirageFairy2024$renderWithSlotClip(guiGraphics, slot, () -> original.call(guiGraphics, x, y, blitOffset));
    }

    @Group(name = "mirageFairy2024$slotHighlight", min = 1)
    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderSlotHighlight(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/inventory/Slot;IIF)V", remap = false), require = 0)
    private void mirageFairy2024$renderNeoForgeSlotHighlight(AbstractContainerScreen<?> screen, GuiGraphics guiGraphics, Slot slot, int mouseX, int mouseY, float partialTick, Operation<Void> original) {
        mirageFairy2024$renderWithSlotClip(guiGraphics, slot, () -> original.call(screen, guiGraphics, slot, mouseX, mouseY, partialTick));
    }

    @Unique
    private static void mirageFairy2024$renderWithSlotClip(GuiGraphics guiGraphics, Slot slot, Runnable render) {
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
