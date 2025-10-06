package miragefairy2024.mod.machine

import miragefairy2024.ModContext
import miragefairy2024.mod.recipeviewer.IntPoint
import miragefairy2024.mod.recipeviewer.SolidView

context(ModContext)
fun initMachineModule() {
    FermentationBarrelCard.init()
    FermentationBarrelRecipeCard.init()
    FermentationBarrelRecipeViewerCategoryCard.init()

    AuraReflectorFurnaceCard.init()
    AuraReflectorFurnaceRecipeCard.init()
    AuraReflectorFurnaceRecipeViewerCategoryCard.init()
}

class BlueFuelView : SolidView(IntPoint(13, 13))
