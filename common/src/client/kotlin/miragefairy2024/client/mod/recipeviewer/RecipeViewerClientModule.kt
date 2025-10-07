package miragefairy2024.client.mod.recipeviewer

import miragefairy2024.ModContext
import miragefairy2024.mod.recipeviewer.RendererProxy
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.FormattedText
import net.minecraft.util.FormattedCharSequence

context(ModContext)
fun initRecipeViewerClientModule() {
    initReiClientSupport()
    initEmiClientSupport()
}

val rendererProxy = object : RendererProxy {
    val font by lazy { Minecraft.getInstance().font }
    override fun calculateTextWidth(text: FormattedText) = font.width(text)
    override fun getTextHeight() = font.lineHeight
    override fun wrapText(text: FormattedText, maxWidth: Int): List<FormattedCharSequence> = font.split(text, maxWidth)
}
