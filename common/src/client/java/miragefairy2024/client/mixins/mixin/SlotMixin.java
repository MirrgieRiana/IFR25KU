package miragefairy2024.client.mixins.mixin;

import miragefairy2024.client.mixins.api.SlotPositionSetter;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Slot.class)
public abstract class SlotMixin implements SlotPositionSetter {
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
}
