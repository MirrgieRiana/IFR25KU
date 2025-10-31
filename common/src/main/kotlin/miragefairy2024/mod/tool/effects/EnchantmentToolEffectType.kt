package miragefairy2024.mod.tool.effects

import miragefairy2024.ModifyItemEnchantmentsHandler
import miragefairy2024.mod.DynamicPoem
import miragefairy2024.mod.EnchantmentCard
import miragefairy2024.mod.PoemType
import miragefairy2024.mod.tool.ToolConfiguration
import miragefairy2024.mod.tool.ToolEffectType
import miragefairy2024.mod.tool.merge
import miragefairy2024.util.get
import miragefairy2024.util.invoke
import miragefairy2024.util.plus
import miragefairy2024.util.text
import miragefairy2024.util.toRomanText
import mirrg.kotlin.helium.atLeast
import mirrg.kotlin.helium.max
import mirrg.kotlin.helium.or
import mirrg.kotlin.java.hydrogen.orNull
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.enchantment.Enchantment

fun <T : ToolConfiguration> T.enchantment(enchantment: ResourceKey<Enchantment>, level: Int = 1) = this.merge(EnchantmentToolEffectType, EnchantmentToolEffectType.Value(mapOf(enchantment to level)))

fun <T : ToolConfiguration> T.areaMining(lateral: Int, forward: Int, backward: Int) = this
    .let { if (lateral >= 1) it.enchantment(EnchantmentCard.LATERAL_AREA_MINING.key, lateral) else it }
    .let { if (forward >= 1) it.enchantment(EnchantmentCard.FORWARD_AREA_MINING.key, forward) else it }
    .let { if (backward >= 1) it.enchantment(EnchantmentCard.BACKWARD_AREA_MINING.key, backward) else it }

object EnchantmentToolEffectType : ToolEffectType<ToolConfiguration, EnchantmentToolEffectType.Value> {
    class Value(val map: Map<ResourceKey<Enchantment>, Int>)

    override fun castOrThrow(value: Any?) = value as Value
    override fun merge(a: Value, b: Value) = Value((a.map.keys + b.map.keys).associateWith { key -> (a.map[key] ?: 0) max (b.map[key] ?: 0) })

    override fun apply(configuration: ToolConfiguration, value: Value) {
        if (value.map.isEmpty()) return
        value.map.forEach { (enchantment, level) ->
            configuration.descriptions += DynamicPoem(PoemType.DESCRIPTION) { context ->
                val trueEnchantment = context.registries().or { return@DynamicPoem Component.empty() }[Registries.ENCHANTMENT, enchantment].value()
                text { trueEnchantment.description + if (level >= 2 || trueEnchantment.maxLevel >= 2) " "() + level.toRomanText() else ""() }
            }
        }
        configuration.modifyItemEnchantmentsHandlers += ModifyItemEnchantmentsHandler { _, mutableItemEnchantments, enchantmentLookup ->
            value.map.forEach { (enchantmentKey, level) ->
                val enchantment = enchantmentLookup[enchantmentKey].orNull ?: return@forEach
                mutableItemEnchantments.set(enchantment, mutableItemEnchantments.getLevel(enchantment) atLeast level)
            }
        }
    }
}
