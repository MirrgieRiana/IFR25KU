package miragefairy2024.mod.fairy

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa

private val identifier = MirageFairy2024.identifier("fairy_dream")
val GAIN_FAIRY_DREAM_TRANSLATION = Translation({ "gui.${identifier.toLanguageKey()}.gain" }, "Dreamed of a new fairy!", "新たな妖精の夢を見た！")
val GAIN_FAIRY_TRANSLATION = Translation({ "gui.${identifier.toLanguageKey()}.gain_fairy" }, "%s found!", "%sを発見した！")
val GIVE_ALL_SUCCESS_TRANSLATION = Translation({ identifier.toLanguageKey("commands", "give.all.success") }, "Gave %s fairy dreams", "%s 個の妖精の夢を付与しました")
val GIVE_ONE_SUCCESS_TRANSLATION = Translation({ identifier.toLanguageKey("commands", "give.one.success") }, "Gave %s dream", "%s の夢を付与しました")
val ALREADY_HAVE_DREAM_TRANSLATION = Translation({ identifier.toLanguageKey("commands", "already_have_dream") }, "You already have %s dream", "すでに %s の夢を持っています")
val UNKNOWN_MOTIF_TRANSLATION = Translation({ identifier.toLanguageKey("commands", "unknown_motif") }, "Unknown motif: %s", "不明なモチーフ: %s")
val REMOVE_ALL_SUCCESS_TRANSLATION = Translation({ identifier.toLanguageKey("commands", "remove.all.success") }, "Removed %s fairy dreams", "%s 個の妖精の夢を削除しました")
val REMOVE_ONE_SUCCESS_TRANSLATION = Translation({ identifier.toLanguageKey("commands", "remove.one.success") }, "Removed %s dream", "%s の夢を削除しました")
val DO_NOT_HAVE_DREAM_TRANSLATION = Translation({ identifier.toLanguageKey("commands", "do_not_have_dream") }, "You don't have %s dream", "あなたは %s の夢を持っていません")

context(ModContext)
fun initFairyDream() {
    initGainFairyDreamChannel()
    initFairyDreamContainer()
    initFairyDreamGain()

    GAIN_FAIRY_DREAM_TRANSLATION.enJa()
    GAIN_FAIRY_TRANSLATION.enJa()
    GIVE_ALL_SUCCESS_TRANSLATION.enJa()
    GIVE_ONE_SUCCESS_TRANSLATION.enJa()
    ALREADY_HAVE_DREAM_TRANSLATION.enJa()
    UNKNOWN_MOTIF_TRANSLATION.enJa()
    REMOVE_ALL_SUCCESS_TRANSLATION.enJa()
    REMOVE_ONE_SUCCESS_TRANSLATION.enJa()
    DO_NOT_HAVE_DREAM_TRANSLATION.enJa()
}
