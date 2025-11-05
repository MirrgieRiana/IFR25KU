package miragefairy2024.mod.tool.effects

import miragefairy2024.mod.DynamicPoem
import miragefairy2024.mod.PoemType
import miragefairy2024.mod.tool.ToolConfiguration
import miragefairy2024.mod.tool.ToolEffectType
import miragefairy2024.mod.tool.merge
import net.minecraft.core.component.DataComponents
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item

fun <T : ToolConfiguration> T.enchantable(tag: TagKey<Item>) = this.merge(EnchantableToolEffectType, EnchantableToolEffectType.Value(setOf(tag)))

object EnchantableToolEffectType : ToolEffectType<ToolConfiguration, EnchantableToolEffectType.Value> {
    class Value(val tags: Set<TagKey<Item>>)

    override fun castOrThrow(value: Any?) = value as Value
    override fun merge(a: Value, b: Value) = Value(a.tags + b.tags)

    override fun apply(configuration: ToolConfiguration, value: Value) {
        configuration.tags += value.tags
        configuration.descriptions += DynamicPoem(PoemType.DESCRIPTION) { itemStack, _ ->
            val enchantments = itemStack[DataComponents.ENCHANTMENTS] ?: return@DynamicPoem emptyList()
            if (!enchantments.isEmpty) return@DynamicPoem emptyList()
            value.tags.map { it.name }
        }
    }
}
