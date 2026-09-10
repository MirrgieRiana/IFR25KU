package miragefairy2024.mixins.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface EatFoodCallback {
    Event<EatFoodCallback> EVENT = EventFactory.createArrayBacked(EatFoodCallback.class, callbacks -> (entity, level, stack, foodProperties) -> {
        for (EatFoodCallback callback : callbacks) {
            callback.eatFood(entity, level, stack, foodProperties);
        }
    });

    void eatFood(LivingEntity entity, Level level, ItemStack stack, FoodProperties foodProperties);
}
