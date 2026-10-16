package miragefairy2024.client.mixins.mixin;

import miragefairy2024.client.mixins.api.SlotScissorAreaHolder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {
    @Shadow
    protected int leftPos;

    @Shadow
    protected int topPos;

    /**
     * 矩形の指定を持つスロットは、その中だけへ描くのだ～🌱
     * 矩形の境目に跨ったスロットを、途中まで見せるために要るのだ～🌱
     * 矩形はスロットの座標と同じ原点からの相対であるため、画面の座標へ変換してから渡すのだ～🌱
     */
    @Inject(method = "renderSlot", at = @At("HEAD"))
    private void mirageFairy2024$enableSlotScissor(GuiGraphics guiGraphics, Slot slot, CallbackInfo ci) {
        int[] scissorArea = ((SlotScissorAreaHolder) slot).mirageFairy2024$getScissorArea();
        if (scissorArea == null) return;
        guiGraphics.enableScissor(scissorArea[0] + this.leftPos, scissorArea[1] + this.topPos, scissorArea[2] + this.leftPos, scissorArea[3] + this.topPos);
    }

    @Inject(method = "renderSlot", at = @At("RETURN"))
    private void mirageFairy2024$disableSlotScissor(GuiGraphics guiGraphics, Slot slot, CallbackInfo ci) {
        int[] scissorArea = ((SlotScissorAreaHolder) slot).mirageFairy2024$getScissorArea();
        if (scissorArea == null) return;
        guiGraphics.disableScissor();
    }
}
