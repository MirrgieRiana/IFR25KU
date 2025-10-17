package miragefairy2024.client.mod.recipeviewer

import miragefairy2024.ModContext
import miragefairy2024.client.mod.recipeviewer.common.ClickableViewRenderer
import miragefairy2024.client.mod.recipeviewer.common.ImageButtonViewRenderer
import miragefairy2024.client.mod.recipeviewer.common.NinePatchImageViewRenderer
import miragefairy2024.client.mod.recipeviewer.emi.hasClientEmi
import miragefairy2024.client.mod.recipeviewer.emi.initEmiClientSupport
import miragefairy2024.client.mod.recipeviewer.emi.initEmiViewPlacers
import miragefairy2024.client.mod.recipeviewer.rei.hasClientRei
import miragefairy2024.client.mod.recipeviewer.rei.initReiClientSupport
import miragefairy2024.client.mod.recipeviewer.rei.initReiViewPlacers
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.views.ClickableView
import miragefairy2024.mod.recipeviewer.views.ImageButtonView
import miragefairy2024.mod.recipeviewer.views.NinePatchImageView
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.FormattedText
import net.minecraft.util.FormattedCharSequence

context(ModContext)
fun initRecipeViewerClientModule() {
    if (hasClientRei()) initReiClientSupport()
    if (hasClientRei()) initReiViewPlacers()
    if (hasClientEmi()) initEmiClientSupport()
    if (hasClientEmi()) initEmiViewPlacers()

    ViewRendererRegistry.register(NinePatchImageView::class.java, NinePatchImageViewRenderer)
    ViewRendererRegistry.register(ImageButtonView::class.java, ImageButtonViewRenderer)
    ViewRendererRegistry.register(ClickableView::class.java, ClickableViewRenderer)
}

val renderingProxy = object : RenderingProxy {
    val font by lazy { Minecraft.getInstance().font }
    override fun calculateTextWidth(text: FormattedText) = font.width(text)
    override fun calculateTextWidth(text: FormattedCharSequence) = font.width(text)
    override fun getTextHeight() = font.lineHeight
    override fun wrapText(text: FormattedText, maxWidth: Int): List<FormattedCharSequence> = font.split(text, maxWidth)
}
