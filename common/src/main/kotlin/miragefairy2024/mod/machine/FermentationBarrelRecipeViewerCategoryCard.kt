package miragefairy2024.mod.machine

import miragefairy2024.MirageFairy2024
import miragefairy2024.mod.recipeviewer.AbsoluteView
import miragefairy2024.mod.recipeviewer.Alignment
import miragefairy2024.mod.recipeviewer.ArrowView
import miragefairy2024.mod.recipeviewer.ColorPair
import miragefairy2024.mod.recipeviewer.ImageView
import miragefairy2024.mod.recipeviewer.InputSlotView
import miragefairy2024.mod.recipeviewer.IntPoint
import miragefairy2024.mod.recipeviewer.IntRectangle
import miragefairy2024.mod.recipeviewer.OutputSlotView
import miragefairy2024.mod.recipeviewer.SECONDS_TRANSLATION
import miragefairy2024.mod.recipeviewer.TextView
import miragefairy2024.mod.recipeviewer.View
import miragefairy2024.mod.recipeviewer.grow
import miragefairy2024.mod.recipeviewer.minus
import miragefairy2024.mod.recipeviewer.noBackground
import miragefairy2024.mod.recipeviewer.noMargin
import miragefairy2024.mod.recipeviewer.plusAssign
import miragefairy2024.mod.recipeviewer.topLeft
import miragefairy2024.util.EnJa
import miragefairy2024.util.IngredientStack
import miragefairy2024.util.invoke
import miragefairy2024.util.text
import miragefairy2024.util.times
import mirrg.kotlin.helium.stripTrailingZeros
import mirrg.kotlin.hydrogen.formatAs

object FermentationBarrelRecipeViewerCategoryCard : SimpleMachineRecipeViewerCategoryCard<FermentationBarrelRecipe>() {
    override fun getId() = MirageFairy2024.identifier("fermentation_barrel")
    override fun getName() = EnJa("Fermentation Barrel", "醸造樽")
    override fun getRecipeCard() = FermentationBarrelRecipeCard
    override fun getMachineCard() = FermentationBarrelCard
    override fun getScreenClickAreas() = listOf(Pair(getMachineCard().screenHandlerType.key, IntRectangle(76, 27, 24, 17)))

    override fun createView(recipeEntry: RecipeEntry<FermentationBarrelRecipe>) = View {
        val imageBound = IntRectangle(30, 16, 120, 40)
        val bound = imageBound.grow(6, 2)
        val p = bound.topLeft
        this += AbsoluteView(bound.width, bound.height) {

            this += ImageView("textures/gui/container/" * FermentationBarrelRecipeCard.identifier * ".png", bound)

            fun getInput(index: Int) = recipeEntry.recipe.inputs.getOrNull(index) ?: IngredientStack.EMPTY
            this += (IntPoint(42, 17) - p) to InputSlotView(getInput(0)).noBackground().noMargin()
            this += (IntPoint(31, 39) - p) to InputSlotView(getInput(1)).noBackground().noMargin()
            this += (IntPoint(53, 39) - p) to InputSlotView(getInput(2)).noBackground().noMargin()

            this += (IntPoint(76, 27) - p) to ArrowView().apply {
                durationMilliSeconds = recipeEntry.recipe.duration * 50
            }
            val seconds = recipeEntry.recipe.duration.toDouble() / 20.0
            this += (IntPoint(88, 15) - p) to TextView(text { SECONDS_TRANSLATION((seconds formatAs "%.2f").stripTrailingZeros()) }).apply {
                horizontalAlignment = Alignment.CENTER
                color = ColorPair.DARK_GRAY
                shadow = false
            }

            this += (IntPoint(111, 28) - p) to OutputSlotView(recipeEntry.recipe.output).noBackground().noMargin()

        }
    }
}
