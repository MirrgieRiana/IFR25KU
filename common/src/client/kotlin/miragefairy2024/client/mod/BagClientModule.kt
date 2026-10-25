package miragefairy2024.client.mod

import miragefairy2024.ModContext
import miragefairy2024.client.mod.recipeviewer.vanilla.ViewContainerScreen
import miragefairy2024.client.util.registerHandledScreen
import miragefairy2024.mod.BagCard

context(ModContext)
fun initBagClientModule() {
    BagCard.screenHandlerType.registerHandledScreen(::ViewContainerScreen)
}
