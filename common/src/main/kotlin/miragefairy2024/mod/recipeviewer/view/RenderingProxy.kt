package miragefairy2024.mod.recipeviewer.view

import net.minecraft.network.chat.FormattedText
import net.minecraft.util.FormattedCharSequence

interface RenderingProxy {
    fun calculateTextWidth(text: FormattedText): Int
    fun calculateTextWidth(text: FormattedCharSequence): Int
    fun getTextHeight(): Int
    fun wrapText(text: FormattedText, maxWidth: Int): List<FormattedCharSequence>
}
