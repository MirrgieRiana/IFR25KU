package miragefairy2024.mod.machine

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import miragefairy2024.MirageFairy2024
import miragefairy2024.util.IngredientStack
import miragefairy2024.util.createItemStack
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

object AuraReflectorFurnaceRecipeCard : SimpleMachineRecipeCard<AuraReflectorFurnaceRecipe>() {
    override val identifier = MirageFairy2024.identifier("aura_reflector_furnace")
    override fun getIcon() = AuraReflectorFurnaceCard.item().createItemStack()
    override val recipeClass = AuraReflectorFurnaceRecipe::class.java
    override fun createRecipe(group: String, inputs: List<IngredientStack>, outputs: List<ItemStack>, duration: Int): AuraReflectorFurnaceRecipe {
        return AuraReflectorFurnaceRecipe(this, group, inputs, outputs, duration)
    }
}

class AuraReflectorFurnaceRecipe(
    card: AuraReflectorFurnaceRecipeCard,
    group: String,
    inputs: List<IngredientStack>,
    outputs: List<ItemStack>,
    duration: Int,
) : SimpleMachineRecipe(
    card,
    group,
    inputs,
    outputs,
    duration,
) {
    companion object {
        val FUELS = mutableMapOf<ResourceKey<Item>, AuraReflectorFurnaceFuel>()

        init {
            registerFuel(BuiltInRegistries.ITEM.getResourceKey(Items.SOUL_SAND).get(), 20 * 10)
            registerFuel(BuiltInRegistries.ITEM.getResourceKey(Items.SOUL_SOIL).get(), 20 * 10)
        }

        fun registerFuel(fuel: ResourceKey<Item>, duration: Int) {
            check(fuel !in FUELS) { "Fuel $fuel is already registered." }
            FUELS[fuel] = AuraReflectorFurnaceFuel(fuel, duration)
        }

        fun getFuelValue(itemStack: ItemStack) = FUELS[BuiltInRegistries.ITEM.getResourceKey(itemStack.item).get()]?.duration
    }
}

data class AuraReflectorFurnaceFuel(val fuel: ResourceKey<Item>, val duration: Int) {
    companion object {
        val CODEC: Codec<AuraReflectorFurnaceFuel> = RecordCodecBuilder.create { instance ->
            instance.group(
                ResourceKey.codec(BuiltInRegistries.ITEM.key()).fieldOf("Fuel").forGetter { it.fuel },
                Codec.INT.fieldOf("Duration").forGetter { it.duration },
            ).apply(instance, ::AuraReflectorFurnaceFuel)
        }
    }
}
