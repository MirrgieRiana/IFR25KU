package miragefairy2024.mod.machine

import miragefairy2024.ModContext
import miragefairy2024.mod.recipeviewer.views.FixedWidgetView

context(ModContext)
fun initMachineModule() {
    FermentationBarrelCard.init()
    FermentationBarrelRecipeCard.init()
    FermentationBarrelRecipeViewerCategoryCard.init()

    AuraReflectorFurnaceCard.init()
    AuraReflectorFurnaceRecipeCard.init()
    AuraReflectorFurnaceRecipeViewerCategoryCard.init()
}

class BlueFuelView : FixedWidgetView(13, 13)
