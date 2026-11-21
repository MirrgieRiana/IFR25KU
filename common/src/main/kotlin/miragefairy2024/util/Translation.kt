package miragefairy2024.util

import miragefairy2024.DataGenerationEvents
import miragefairy2024.ModContext
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block

context(_: ModContext)
fun en(getter: () -> Pair<String, String>) = DataGenerationEvents.onGenerateEnglishTranslation {
    val pair = getter()
    it.add(pair.first, pair.second)
}

context(_: ModContext)
fun ja(getter: () -> Pair<String, String>) = DataGenerationEvents.onGenerateJapaneseTranslation {
    val pair = getter()
    it.add(pair.first, pair.second)
}

context(_: ModContext)
@JvmName("enJaBlock")
fun (() -> Block).enJa(name: EnJa) {
    en { this().descriptionId to name.en }
    ja { this().descriptionId to name.ja }
}

context(_: ModContext)
@JvmName("enJaItem")
fun (() -> Item).enJa(name: EnJa) {
    en { this().descriptionId to name.en }
    ja { this().descriptionId to name.ja }
}

context(_: ModContext)
@JvmName("enJaEntityType")
fun (() -> EntityType<*>).enJa(name: EnJa) {
    en { this().descriptionId to name.en }
    ja { this().descriptionId to name.ja }
}

context(_: ModContext)
@JvmName("enJaTagKey")
fun <T> TagKey<T>.enJa(name: EnJa) {
    en { this.translationKey to name.en }
    ja { this.translationKey to name.ja }
}


class EnJa(val en: String, val ja: String)


class Translation(val keyGetter: () -> String, val en: String, val ja: String)

fun Translation(keyGetter: () -> String, enJa: EnJa) = Translation(keyGetter, enJa.en, enJa.ja)

context(_: TextScope)
operator fun Translation.invoke() = text { translate(this@invoke.keyGetter()) }

context(_: TextScope)
operator fun Translation.invoke(vararg args: Any?) = text { translate(this@invoke.keyGetter(), *args) }

context(_: ModContext)
fun Translation.enJa() {
    en { this.keyGetter() to en }
    ja { this.keyGetter() to ja }
}
