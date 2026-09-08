package miragefairy2024.mixins.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import miragefairy2024.mixins.api.DamageCallback;
import miragefairy2024.mixins.api.EatFoodCallback;
import miragefairy2024.mixins.api.EquippedItemBrokenCallback;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Inject(method = "eat(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/food/FoodProperties;)Lnet/minecraft/world/item/ItemStack;", at = @At("HEAD"))
    private void eatFood(Level level, ItemStack stack, FoodProperties foodProperties, CallbackInfoReturnable<ItemStack> info) {
        EatFoodCallback.EVENT.invoker().eatFood((LivingEntity) (Object) this, level, stack, foodProperties);
    }

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir, @Local(ordinal = 0, argsOnly = true) LocalFloatRef amount2) {
        float newAmount2 = DamageCallback.EVENT.invoker().modifyDamageAmount((LivingEntity) (Object) this, source, amount2.get());
        if (newAmount2 <= 0F) {
            cir.setReturnValue(false);
        } else {
            amount2.set(newAmount2);
        }
    }

    @Inject(method = "onEquippedItemBroken", at = @At("TAIL"))
    private void onEquippedItemBroken(Item item, EquipmentSlot slot, CallbackInfo ci) {
        EquippedItemBrokenCallback.EVENT.invoker().onEquippedItemBroken((LivingEntity) (Object) this, item, slot);
    }
}
