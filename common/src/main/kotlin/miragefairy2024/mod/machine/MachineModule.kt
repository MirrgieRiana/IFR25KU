package miragefairy2024.mod.machine

import miragefairy2024.ModContext
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.ViewTexture
import miragefairy2024.mod.recipeviewer.views.FixedWidgetView

context(ModContext)
fun initMachineModule() {
    FermentationBarrelCard.init()
    FermentationBarrelRecipeCard.init()
    FermentationBarrelRecipeViewerCategoryCard.init()

    AuraReflectorFurnaceCard.init()
    AuraReflectorFurnaceRecipeCard.init()
    AuraReflectorFurnaceRecipeViewerCategoryCard.init()
    AuraReflectorFurnaceFuelRecipeViewerCategoryCard.init()

    AthanorRecipeCard.init()
    AthanorRecipeViewerCategoryCard.init()
}

class FuelView : FixedWidgetView(IntPoint(13, 13))

class BlueFuelView : FixedWidgetView(IntPoint(13, 13))

class TexturedArrowView(size: IntPoint) : FixedWidgetView(size) {
    var backgroundTexture: ViewTexture? = null
    var foregroundTexture: ViewTexture? = null
    var durationMilliSeconds: Int? = null
}
