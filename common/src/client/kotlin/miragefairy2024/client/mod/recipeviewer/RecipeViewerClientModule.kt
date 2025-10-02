package miragefairy2024.client.mod.recipeviewer

import miragefairy2024.ModContext
import miragefairy2024.mod.recipeviewer.RendererProxy

context(ModContext)
fun initRecipeViewerClientModule() {
    initReiClientSupport()
    initEmiClientSupport()
}

val rendererProxy = object : RendererProxy {

}
