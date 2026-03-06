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
import miragefairy2024.mod.recipeviewer.views.CatalystSlotView
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
import miragefairy2024.util.get
import miragefairy2024.util.toIngredientStack
import net.minecraft.core.registries.Registries

object AuraReflectorFurnaceRecipeViewerCategoryCard : SimpleMachineRecipeViewerCategoryCard<AuraReflectorFurnaceRecipe>() {
    override fun getId() = MirageFairy2024.identifier("aura_reflector_furnace")
    override fun getName() = EnJa("Aura Reflector Furnace", "オーラ反射炉")
    private fun getFuelIngredientStack(recipeEntry: RecipeEntry<AuraReflectorFurnaceRecipe>) = AuraReflectorFurnaceRecipe.FUELS.map { recipeEntry.registryAccess[Registries.ITEM, it.key].value() }.toIngredientStack()
    override fun getInputs(recipeEntry: RecipeEntry<AuraReflectorFurnaceRecipe>) = super.getInputs(recipeEntry) + listOf(Input(getFuelIngredientStack(recipeEntry), true))
    override fun getRecipeCard() = AuraReflectorFurnaceRecipeCard
    override fun getMachineCard() = AuraReflectorFurnaceCard
    override fun getScreenClickAreas() = listOf(Pair(getMachineCard().screenHandlerType.key, IntRectangle(88, 34, 24, 17)))

    override fun createView(recipeEntry: RecipeEntry<AuraReflectorFurnaceRecipe>) = View {
        val imageBound = IntRectangle(28, 16, 116, 54)
        val bounds = imageBound.grow(6, 2)
        val p = bounds.offset
        view += AbsoluteView(bounds.size).configure {

            view += ImageView(getTexture(bounds))

            fun getInput(index: Int) = recipeEntry.recipe.inputs.getOrNull(index) ?: IngredientStack.EMPTY
            view += InputSlotView(getInput(0)).noBackground().noMargin().configure {
                position = AbsoluteView.Offset(IntPoint(29, 17) - p)
            }
            view += InputSlotView(getInput(1)).noBackground().noMargin().configure {
                position = AbsoluteView.Offset(IntPoint(47, 17) - p)
            }
            view += InputSlotView(getInput(2)).noBackground().noMargin().configure {
                position = AbsoluteView.Offset(IntPoint(65, 17) - p)
            }
            view += CatalystSlotView(getFuelIngredientStack(recipeEntry)).noBackground().noMargin().configure {
                position = AbsoluteView.Offset(IntPoint(47, 53) - p)
            }
            view += BlueFuelView().configure {
                position = AbsoluteView.Offset(IntPoint(48, 37) - p)
            }

            view += ArrowView().configure {
                position = AbsoluteView.Offset(IntPoint(88, 34) - p)
                view.durationMilliSeconds = recipeEntry.recipe.duration * 50
            }
            view += TextView(recipeEntry.recipe.duration.toSecondsTextAsTicks()).configure {
                position = AbsoluteView.Offset(IntPoint(108, 18) - p)
                view.alignmentX = Alignment.CENTER
                view.color = ColorPair.DARK_GRAY
                view.shadow = false
            }

            view += OutputSlotView(recipeEntry.recipe.outputs[0]).noBackground().noMargin().configure {
                position = AbsoluteView.Offset(IntPoint(123, 35) - p)
            }

        }
    }
}
