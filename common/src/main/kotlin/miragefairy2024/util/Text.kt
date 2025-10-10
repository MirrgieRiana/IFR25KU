package miragefairy2024.util

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.FormattedText
import net.minecraft.network.chat.Style
import net.minecraft.util.FormattedCharSequence

fun Component.style(style: Style): Component = Component.empty().append(this).setStyle(style)
fun Component.formatted(formatting: ChatFormatting): Component = Component.empty().append(this).withStyle(formatting)
val Component.black get() = this.formatted(ChatFormatting.BLACK)
val Component.darkBlue get() = this.formatted(ChatFormatting.DARK_BLUE)
val Component.darkGreen get() = this.formatted(ChatFormatting.DARK_GREEN)
val Component.darkAqua get() = this.formatted(ChatFormatting.DARK_AQUA)
val Component.darkRed get() = this.formatted(ChatFormatting.DARK_RED)
val Component.darkPurple get() = this.formatted(ChatFormatting.DARK_PURPLE)
val Component.gold get() = this.formatted(ChatFormatting.GOLD)
val Component.gray get() = this.formatted(ChatFormatting.GRAY)
val Component.darkGray get() = this.formatted(ChatFormatting.DARK_GRAY)
val Component.blue get() = this.formatted(ChatFormatting.BLUE)
val Component.green get() = this.formatted(ChatFormatting.GREEN)
val Component.aqua get() = this.formatted(ChatFormatting.AQUA)
val Component.red get() = this.formatted(ChatFormatting.RED)
val Component.lightPurple get() = this.formatted(ChatFormatting.LIGHT_PURPLE)
val Component.yellow get() = this.formatted(ChatFormatting.YELLOW)
val Component.white get() = this.formatted(ChatFormatting.WHITE)
val Component.obfuscated get() = this.formatted(ChatFormatting.OBFUSCATED)
val Component.bold get() = this.formatted(ChatFormatting.BOLD)
val Component.strikethrough get() = this.formatted(ChatFormatting.STRIKETHROUGH)
val Component.underline get() = this.formatted(ChatFormatting.UNDERLINE)
val Component.italic get() = this.formatted(ChatFormatting.ITALIC)

fun Iterable<Component>.join(): Component {
    val result = Component.empty()
    this.forEach {
        result.append(it)
    }
    return result
}

fun Iterable<Component>.join(vararg separators: Component): Component {
    val result = Component.empty()
    this.forEachIndexed { index, text ->
        if (index != 0) {
            separators.forEach {
                result.append(it)
            }
        }
        result.append(text)
    }
    return result
}

fun Int.toRomanText() = if (this in 1..10) text { translate("enchantment.level.${this@toRomanText}") } else text { "$this"() }

fun FormattedCharSequence.toFormattedText(): FormattedText {
    val formattedTexts = mutableListOf<FormattedText>()

    var currentStyle: Style? = null
    val sb = StringBuilder()
    this.accept { i, style, j ->
        if (currentStyle == null) {
            currentStyle = style
        } else if (style != currentStyle) {
            formattedTexts += FormattedText.of(sb.toString(), currentStyle!!)
            sb.clear()
            currentStyle = style
        }
        sb.append(Character.toChars(j))
        true
    }
    if (currentStyle != null) formattedTexts += FormattedText.of(sb.toString(), currentStyle)

    if (formattedTexts.isEmpty()) FormattedText.EMPTY
    return FormattedText.composite(formattedTexts)
}
