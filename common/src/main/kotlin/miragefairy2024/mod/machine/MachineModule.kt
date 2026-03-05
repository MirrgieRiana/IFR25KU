package miragefairy2024.mod.machine

import miragefairy2024.ModContext
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.ViewTexture
import miragefairy2024.mod.recipeviewer.view.sized
import miragefairy2024.mod.recipeviewer.views.AbstractView
import miragefairy2024.mod.recipeviewer.views.FixedWidgetView
import miragefairy2024.util.ObservableValue

context(ModContext)
fun initMachineModule() {
    FermentationBarrelCard.init()
    FermentationBarrelRecipeCard.init()
    FermentationBarrelRecipeViewerCategoryCard.init()

    AuraReflectorFurnaceCard.init()
    AuraReflectorFurnaceRecipeCard.init()
    AuraReflectorFurnaceRecipeViewerCategoryCard.init()
    AuraReflectorFurnaceFuelRecipeViewerCategoryCard.init()

    AthanorCard.init()
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

class FilledRectangleView : AbstractView(), PlaceableView {
    val color = ObservableValue(0)
    override var sizingX = Sizing.FILL
    override var sizingY = Sizing.FILL
    override fun calculateContentSize() = IntPoint.Companion.ZERO
    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = viewPlacer.place(this, offset.sized(actualSize))
}
