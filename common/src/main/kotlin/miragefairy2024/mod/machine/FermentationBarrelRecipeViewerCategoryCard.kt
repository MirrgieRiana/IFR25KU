package miragefairy2024.mod.machine

import miragefairy2024.MirageFairy2024
import miragefairy2024.mod.recipeviewer.toSecondsTextAsTicks
import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.ColorPair
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.grow
import miragefairy2024.mod.recipeviewer.view.minus
import miragefairy2024.mod.recipeviewer.view.offset
import miragefairy2024.mod.recipeviewer.view.size
import miragefairy2024.mod.recipeviewer.views.AbsoluteView
import miragefairy2024.mod.recipeviewer.views.ArrowView
import miragefairy2024.mod.recipeviewer.views.ImageView
import miragefairy2024.mod.recipeviewer.views.InputSlotView
import miragefairy2024.mod.recipeviewer.views.OutputSlotView
import miragefairy2024.mod.recipeviewer.views.TextView
import miragefairy2024.mod.recipeviewer.views.View
import miragefairy2024.mod.recipeviewer.views.configure
import miragefairy2024.mod.recipeviewer.views.noBackground
import miragefairy2024.mod.recipeviewer.views.noMargin
import miragefairy2024.mod.recipeviewer.views.plusAssign
import miragefairy2024.util.EnJa
import miragefairy2024.util.IngredientStack

object FermentationBarrelRecipeViewerCategoryCard : SimpleMachineRecipeViewerCategoryCard<FermentationBarrelRecipe>() {
    override fun getId() = MirageFairy2024.identifier("fermentation_barrel")
    override fun getName() = EnJa("Fermentation Barrel", "醸造樽")
    override fun getRecipeCard() = FermentationBarrelRecipeCard
    override fun getMachineCard() = FermentationBarrelCard
    override fun getScreenClickAreas() = listOf(Pair(getMachineCard().screenHandlerType.key, IntRectangle(76, 27, 24, 17)))

    override fun createView(recipeEntry: RecipeEntry<FermentationBarrelRecipe>) = View {
        val imageBound = IntRectangle(30, 16, 120, 40)
        val bounds = imageBound.grow(6, 2)
        val p = bounds.offset
        view += AbsoluteView(bounds.size).configure {

            view += ImageView(getTexture(bounds))

            fun getInput(index: Int) = recipeEntry.recipe.inputs.getOrNull(index) ?: IngredientStack.EMPTY
            view += InputSlotView(getInput(0)).noBackground().noMargin().configure {
                position = AbsoluteView.Offset(IntPoint(42, 17) - p)
            }
            view += InputSlotView(getInput(1)).noBackground().noMargin().configure {
                position = AbsoluteView.Offset(IntPoint(31, 39) - p)
            }
            view += InputSlotView(getInput(2)).noBackground().noMargin().configure {
                position = AbsoluteView.Offset(IntPoint(53, 39) - p)
            }

            view += ArrowView().configure {
                position = AbsoluteView.Offset(IntPoint(76, 27) - p)
                view.durationMilliSeconds = recipeEntry.recipe.duration * 50
            }
            view += TextView(recipeEntry.recipe.duration.toSecondsTextAsTicks()).configure {
                position = AbsoluteView.Offset(IntPoint(88, 15) - p)
                view.alignmentX = Alignment.CENTER
                view.color = ColorPair.DARK_GRAY
                view.shadow = false
            }

            view += OutputSlotView(recipeEntry.recipe.outputs[0]).noBackground().noMargin().configure {
                position = AbsoluteView.Offset(IntPoint(111, 28) - p)
            }

        }
    }
}
