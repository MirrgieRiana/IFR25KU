package miragefairy2024.client.mixins.mixin;

import miragefairy2024.client.mixins.api.SlotPositionSetter;
import miragefairy2024.client.mixins.api.SlotScissorAreaHolder;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class SlotMixin implements SlotPositionSetter, SlotScissorAreaHolder {
    @Shadow
    @Final
    @Mutable
    public int x;

    @Shadow
    @Final
    @Mutable
    public int y;

    @Unique
    private static final int MIRAGE_FAIRY_2024_SLOT_SIZE = 16;

    @Unique
    private @Nullable int[] mirageFairy2024$scissorArea = null;

    @Override
    public void mirageFairy2024$setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void mirageFairy2024$setScissorArea(@Nullable int[] scissorArea) {
        this.mirageFairy2024$scissorArea = scissorArea;
    }

    @Override
    public @Nullable int[] mirageFairy2024$getScissorArea() {
        return this.mirageFairy2024$scissorArea;
    }

    /**
     * 矩形の外へ完全に出たスロットは、無効として扱うのだ～🌱
     * 呼び出し元が、描画とホバーとクリックの3個とも、この判定で止めるのだ～🌱
     */
    @Inject(method = "isActive", at = @At("RETURN"), cancellable = true)
    private void mirageFairy2024$hideOutsideScissorArea(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) return;
        int[] scissorArea = this.mirageFairy2024$scissorArea;
        if (scissorArea == null) return;
        if (this.x + MIRAGE_FAIRY_2024_SLOT_SIZE <= scissorArea[0]) cir.setReturnValue(false);
        if (this.y + MIRAGE_FAIRY_2024_SLOT_SIZE <= scissorArea[1]) cir.setReturnValue(false);
        if (this.x >= scissorArea[2]) cir.setReturnValue(false);
        if (this.y >= scissorArea[3]) cir.setReturnValue(false);
    }
}
