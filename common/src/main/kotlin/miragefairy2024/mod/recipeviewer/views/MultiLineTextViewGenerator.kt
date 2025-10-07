package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.RendererProxy
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewGenerator
import net.minecraft.network.chat.Component

class MultiLineTextViewGenerator(val text: Component) : ViewGenerator {
    override fun generateViews(rendererProxy: RendererProxy, regionSize: IntPoint): List<View> {
        return rendererProxy.wrapText(text, regionSize.x).map { TextView(it) }
    }
}
