package miragefairy2024.mod.magicplant

import miragefairy2024.MirageFairy2024
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder
import net.fabricmc.fabric.api.event.registry.RegistryAttribute
import net.minecraft.core.Registry
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation

// api

val traitEffectKeyRegistryKey: ResourceKey<Registry<TraitEffectKey<*>>> = ResourceKey.createRegistryKey(MirageFairy2024.identifier("trait_effect_key"))
val traitEffectKeyRegistry: Registry<TraitEffectKey<*>> = FabricRegistryBuilder.createSimple(traitEffectKeyRegistryKey).attribute(RegistryAttribute.SYNCED).buildAndRegister()

abstract class TraitEffectKey<T : Any> {

    abstract val emoji: Component

    abstract val name: Component

    abstract val sortValue: Double

    abstract val color: Int

    abstract fun getValue(power: Double): T

    abstract fun renderValue(value: T): Component

    /** 画面に表示されている数値を合計するための関数なのだ～🌱 */
    abstract fun plus(a: T, b: T): T

    abstract fun getDefaultValue(): T

}


// util

fun TraitEffectKey<*>.getIdentifier() = traitEffectKeyRegistry.getKey(this)!!
fun ResourceLocation.toTraitEffectKey() = traitEffectKeyRegistry.get(this)
val TraitEffectKey<*>.style: Style get() = Style.EMPTY.withColor(this.color)
