package miragefairy2024.mod.tool.effects

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.PoemType
import miragefairy2024.mod.TextPoem
import miragefairy2024.mod.fairy.SOUL_STREAM_CONTAINABLE_TAG
import miragefairy2024.mod.tool.ToolConfiguration
import miragefairy2024.mod.tool.merge
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.invoke
import miragefairy2024.util.text

fun <T : ToolConfiguration> T.soulStreamContainable() = this.merge(SoulStreamContainableToolEffectType, true)

object SoulStreamContainableToolEffectType : BooleanToolEffectType<ToolConfiguration>() {
    private val TRANSLATION = Translation({ "item.${MirageFairy2024.identifier("fairy_mining_tool").toLanguageKey()}.soul_stream_containable" }, "Soul Stream Containable", "ソウルストリームに格納可能")

    context(_: ModContext)
    fun init() {
        TRANSLATION.enJa()
    }

    override fun apply(configuration: ToolConfiguration, value: Boolean) {
        if (!value) return
        configuration.descriptions += TextPoem(PoemType.DESCRIPTION, text { TRANSLATION() })
        configuration.tags += SOUL_STREAM_CONTAINABLE_TAG
    }
}
