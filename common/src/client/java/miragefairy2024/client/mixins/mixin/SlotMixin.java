package miragefairy2024.client.mixins.mixin;

import miragefairy2024.client.mixins.api.SlotPositionSetter;
import miragefairy2024.client.mixins.api.SlotViewClipping;
import miragefairy2024.mod.recipeviewer.view.IntRectangle;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.function.Supplier;

@Mixin(Slot.class)
public abstract class SlotMixin implements SlotPositionSetter, SlotViewClipping {
    @Shadow
    @Final
    @Mutable
    public int x;

    @Shadow
    @Final
    @Mutable
    public int y;

    @Override
    public void mirageFairy2024$setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Unique
    @Nullable
    private Supplier<IntRectangle> mirageFairy2024$clipBounds;

    @Override
    @Nullable
    public Supplier<IntRectangle> mirageFairy2024$getClipBounds() {
        return mirageFairy2024$clipBounds;
    }

    @Override
    public void mirageFairy2024$setClipBounds(@Nullable Supplier<IntRectangle> clipBounds) {
        mirageFairy2024$clipBounds = clipBounds;
    }
}
