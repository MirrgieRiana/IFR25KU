package miragefairy2024.mod.machine

import miragefairy2024.MirageFairy2024
import miragefairy2024.mod.recipeviewer.SECONDS_TRANSLATION
import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.ColorPair
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.grow
import miragefairy2024.mod.recipeviewer.view.minus
import miragefairy2024.mod.recipeviewer.view.topLeft
import miragefairy2024.mod.recipeviewer.views.AbsoluteView
import miragefairy2024.mod.recipeviewer.views.ArrowView
import miragefairy2024.mod.recipeviewer.views.ImageView
import miragefairy2024.mod.recipeviewer.views.InputSlotView
import miragefairy2024.mod.recipeviewer.views.OutputSlotView
import miragefairy2024.mod.recipeviewer.views.TextView
import miragefairy2024.mod.recipeviewer.views.configure
import miragefairy2024.mod.recipeviewer.views.noBackground
import miragefairy2024.mod.recipeviewer.views.noMargin
import miragefairy2024.mod.recipeviewer.views.plusAssign
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
        view += AbsoluteView(bound.width, bound.height).configure {

            view += ImageView("textures/gui/container/" * FermentationBarrelRecipeCard.identifier * ".png", bound)

            fun getInput(index: Int) = recipeEntry.recipe.inputs.getOrNull(index) ?: IngredientStack.EMPTY
            view += InputSlotView(getInput(0)).noBackground().noMargin().configure {
                position = IntPoint(42, 17) - p
            }
            view += InputSlotView(getInput(1)).noBackground().noMargin().configure {
                position = IntPoint(31, 39) - p
            }
            view += InputSlotView(getInput(2)).noBackground().noMargin().configure {
                position = IntPoint(53, 39) - p
            }

            view += ArrowView().configure {
                position = IntPoint(76, 27) - p
                view.durationMilliSeconds = recipeEntry.recipe.duration * 50
            }
            val seconds = recipeEntry.recipe.duration.toDouble() / 20.0
            view += TextView(text { SECONDS_TRANSLATION((seconds formatAs "%.2f").stripTrailingZeros()) }).configure {
                position = IntPoint(88, 15) - p
                view.horizontalAlignment = Alignment.CENTER
                view.color = ColorPair.DARK_GRAY
                view.shadow = false
            }

            view += OutputSlotView(recipeEntry.recipe.output).noBackground().noMargin().configure {
                position = IntPoint(111, 28) - p
            }

        }
    }
}
