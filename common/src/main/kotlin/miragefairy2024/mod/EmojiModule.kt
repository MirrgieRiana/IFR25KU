package miragefairy2024.mod

import miragefairy2024.util.invoke
import miragefairy2024.util.text

enum class Emoji(val string: String, val charCode: String) {
    STAR("★", "\uE600"),
    HEART("❤", "\uE601"),
    FOOD("🍖", "\uE602"),
    LEVEL("Lv", "\uE603"),
    LUCK("🍀", "\uE604"),
    LIGHT("💡", "\uE605"),
    UP("↑", "\uE606"),
    DOWN("↓", "\uE607"),

    MANA("◇", "\uE608"),
    POTION("🧪", "\uE609"),
    HUMAN("🏃‍➡️", "\uE60A"),
    SWORD("🗡️", "\uE60B"),
    SHIELD("🛡️", "\uE60C"),

    FLOOR_MOISTURE("湿った地面", "\uE610"),
    FLOOR_HARDNESS("硬い地面", "\uE611"),
    FLOOR_CRYSTAL_ERG("鉱物質の地面", "\uE612"),
    DARKNESS("闇", "\uE613"),
    OUTDOOR("屋外", "\uE614"),
    NATURAL("天然", "\uE615"),
    MOON("☾", "\uE616"),
    SUN("☀", "\uE617"),

    LOW_TEMPERATURE("低い気温", "\uE618"),
    MEDIUM_TEMPERATURE("普通の気温", "\uE619"),
    HIGH_TEMPERATURE("高い気温", "\uE61A"),
    LOW_HUMIDITY("低い湿度", "\uE61B"),
    MEDIUM_HUMIDITY("普通の湿度", "\uE61C"),
    HIGH_HUMIDITY("高い湿度", "\uE61D"),

    NUTRITION("栄養値", "\uE620"),
    GROWTH_BOOST("成長速度", "\uE621"),
    SEEDS_PRODUCTION("種子生成", "\uE622"),
    FRUITS_PRODUCTION("果実生成", "\uE623"),
    LEAVES_PRODUCTION("葉面生成", "\uE624"),
    RARE_PRODUCTION("希少品生成", "\uE625"),
    PRODUCTION_BOOST("生産能力", "\uE626"),
    NATURAL_ABSCISSION("自然落果", "\uE627"),

    CROSSBREEDING("交雑", "\uE628"),
    MUTATION("突然変異", "\uE629"),
    ;

    override fun toString() = charCode
}

operator fun Emoji.invoke() = text { this@invoke.charCode() }
