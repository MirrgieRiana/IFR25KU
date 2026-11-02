package miragefairy2024.client.mod.recipeviewer

import miragefairy2024.ModContext
import miragefairy2024.client.mod.recipeviewer.common.ClickableViewRenderer
import miragefairy2024.client.mod.recipeviewer.common.FilledRectangleViewRenderer
import miragefairy2024.client.mod.recipeviewer.common.GhostItemViewRenderer
import miragefairy2024.client.mod.recipeviewer.common.ImageButtonViewRenderer
import miragefairy2024.client.mod.recipeviewer.common.ItemViewRenderer
import miragefairy2024.client.mod.recipeviewer.common.MultiLineTextViewRenderer
import miragefairy2024.client.mod.recipeviewer.common.NinePatchImageViewRenderer
import miragefairy2024.client.mod.recipeviewer.common.ProgressViewRenderer
import miragefairy2024.client.mod.recipeviewer.common.TooltipBackgroundViewRenderer
import miragefairy2024.client.mod.recipeviewer.emi.hasClientEmi
import miragefairy2024.client.mod.recipeviewer.emi.initEmiClientSupport
import miragefairy2024.client.mod.recipeviewer.emi.initEmiViewPlacers
import miragefairy2024.client.mod.recipeviewer.rei.hasClientRei
import miragefairy2024.client.mod.recipeviewer.rei.initReiClientSupport
import miragefairy2024.client.mod.recipeviewer.rei.initReiViewPlacers
import miragefairy2024.client.mod.recipeviewer.vanilla.initVanillaViewPlacers
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.views.ClickableView
import miragefairy2024.mod.recipeviewer.views.FilledRectangleView
import miragefairy2024.mod.recipeviewer.views.GhostItemView
import miragefairy2024.mod.recipeviewer.views.ImageButtonView
import miragefairy2024.mod.recipeviewer.views.ItemView
import miragefairy2024.mod.recipeviewer.views.MultiLineTextView
import miragefairy2024.mod.recipeviewer.views.NinePatchImageView
import miragefairy2024.mod.recipeviewer.views.ProgressView
import miragefairy2024.mod.recipeviewer.views.TooltipBackgroundView
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.FormattedText
import net.minecraft.util.FormattedCharSequence

context(ModContext)
fun initRecipeViewerClientModule() {
    if (hasClientRei()) initReiClientSupport()
    if (hasClientRei()) initReiViewPlacers()
    if (hasClientEmi()) initEmiClientSupport()
    if (hasClientEmi()) initEmiViewPlacers()
    initVanillaViewPlacers()

    ViewRendererRegistry.register(NinePatchImageView::class.java, NinePatchImageViewRenderer)
    ViewRendererRegistry.register(FilledRectangleView::class.java, FilledRectangleViewRenderer)
    ViewRendererRegistry.register(ImageButtonView::class.java, ImageButtonViewRenderer)
    ViewRendererRegistry.register(ItemView::class.java, ItemViewRenderer)
    ViewRendererRegistry.register(ClickableView::class.java, ClickableViewRenderer)
    ViewRendererRegistry.register(ProgressView::class.java, ProgressViewRenderer)
    ViewRendererRegistry.register(MultiLineTextView::class.java, MultiLineTextViewRenderer)
    ViewRendererRegistry.register(GhostItemView::class.java, GhostItemViewRenderer)
    ViewRendererRegistry.register(TooltipBackgroundView::class.java, TooltipBackgroundViewRenderer)
}

val renderingProxy = object : RenderingProxy {
    val font by lazy { Minecraft.getInstance().font }
    override fun calculateTextWidth(text: FormattedText) = font.width(text)
    override fun calculateTextWidth(text: FormattedCharSequence) = font.width(text)
    override fun getTextHeight() = font.lineHeight
    override fun wrapText(text: FormattedText, maxWidth: Int): List<FormattedCharSequence> = font.split(text, maxWidth)
}
