package miragefairy2024.client.mod.recipeviewer

import miragefairy2024.ModContext
import miragefairy2024.client.mod.recipeviewer.emi.initEmiClientSupport
import miragefairy2024.client.mod.recipeviewer.rei.initReiClientSupport
import miragefairy2024.mod.recipeviewer.view.RendererProxy
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component

context(ModContext)
fun initRecipeViewerClientModule() {
    initReiClientSupport()
    initEmiClientSupport()
}

val rendererProxy = object : RendererProxy {
    val font by lazy { Minecraft.getInstance().font }
    override fun calculateTextWidth(component: Component) = font.width(component)
    override fun getTextHeight() = font.lineHeight
}
