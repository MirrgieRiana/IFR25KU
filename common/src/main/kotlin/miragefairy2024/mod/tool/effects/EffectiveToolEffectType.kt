package miragefairy2024.mod.tool.effects

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.PoemType
import miragefairy2024.mod.TextPoem
import miragefairy2024.mod.tool.ToolConfiguration
import miragefairy2024.mod.tool.ToolEffectType
import miragefairy2024.mod.tool.merge
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.invoke
import miragefairy2024.util.text
import net.minecraft.tags.TagKey
import net.minecraft.world.level.block.Block

fun <T : ToolConfiguration> T.effective(vararg blockTags: TagKey<Block>) = this.merge(EffectiveToolEffectType, EffectiveToolEffectType.Value(blockTags.toSet()))

object EffectiveToolEffectType : ToolEffectType<ToolConfiguration, EffectiveToolEffectType.Value> {
    private val TRANSLATION = Translation({ "item.${MirageFairy2024.identifier("fairy_mining_tool").toLanguageKey()}.effective" }, "Mineable: %s", "採掘可能: %s")

    context(_: ModContext)
    fun init() {
        TRANSLATION.enJa()
    }

    class Value(val tags: Set<TagKey<Block>>)

    override fun castOrThrow(value: Any?) = value as Value
    override fun merge(a: Value, b: Value) = Value(a.tags + b.tags)

    override fun apply(configuration: ToolConfiguration, value: Value) {
        if (value.tags.isEmpty()) return
        value.tags.forEach {
            configuration.descriptions += TextPoem(PoemType.DESCRIPTION, text { TRANSLATION(it.name) })
            configuration.effectiveBlockTags += it
        }
    }
}
