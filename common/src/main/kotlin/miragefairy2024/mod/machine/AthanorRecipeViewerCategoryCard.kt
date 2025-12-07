package miragefairy2024.mod.machine

import miragefairy2024.MirageFairy2024
import miragefairy2024.mod.recipeviewer.SECONDS_TRANSLATION
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
import miragefairy2024.util.invoke
import miragefairy2024.util.text
import miragefairy2024.util.toIngredientStack
import mirrg.kotlin.helium.stripTrailingZeros
import mirrg.kotlin.hydrogen.formatAs
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity

object AthanorRecipeViewerCategoryCard : SimpleMachineRecipeViewerCategoryCard<AthanorRecipe>() {
    override fun getId() = MirageFairy2024.identifier("athanor")
    override fun getName() = EnJa("Athanor", "アタノール")
    private fun getFuelIngredientStack() = AbstractFurnaceBlockEntity.getFuel().keys.toIngredientStack()
    override fun getInputs(recipeEntry: RecipeEntry<AthanorRecipe>) = super.getInputs(recipeEntry) + listOf(Input(getFuelIngredientStack(), true))
    override fun getRecipeCard() = AthanorRecipeCard
    override fun getMachineCard() = AthanorCard
    override fun getScreenClickAreas() = listOf(Pair(getMachineCard().screenHandlerType.key, IntRectangle(88, 34, 24, 17)/* TODO */))

    override fun createView(recipeEntry: RecipeEntry<AthanorRecipe>) = View {
        // TODO
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
            view += InputSlotView(getInput(3)).noBackground().noMargin().configure {
                position = AbsoluteView.Offset(IntPoint(47, 35) - p)
            }

            view += CatalystSlotView(getFuelIngredientStack()).noBackground().noMargin().configure {
                position = AbsoluteView.Offset(IntPoint(47, 53) - p)
            }
            view += FuelView().configure {
                position = AbsoluteView.Offset(IntPoint(48, 37) - p)
            }

            view += ArrowView().configure {
                position = AbsoluteView.Offset(IntPoint(88, 34) - p)
                view.durationMilliSeconds = recipeEntry.recipe.duration * 50
            }
            view += TextView(text { SECONDS_TRANSLATION((recipeEntry.recipe.duration.toDouble() / 20.0 formatAs "%.2f").stripTrailingZeros()) }).configure {
                position = AbsoluteView.Offset(IntPoint(108, 18) - p)
                view.alignmentX = Alignment.CENTER
                view.color = ColorPair.DARK_GRAY
                view.shadow = false
            }

            fun getOutput(index: Int) = if (index == 0) recipeEntry.recipe.output else null
            view += OutputSlotView(getOutput(0) ?: ItemStack.EMPTY).noBackground().noMargin().configure {
                position = AbsoluteView.Offset(IntPoint(111, 28) - p)
            }
            view += OutputSlotView(getOutput(1) ?: ItemStack.EMPTY).noBackground().noMargin().configure {
                position = AbsoluteView.Offset(IntPoint(129, 28) - p)
            }
            view += OutputSlotView(getOutput(2) ?: ItemStack.EMPTY).noBackground().noMargin().configure {
                position = AbsoluteView.Offset(IntPoint(111, 46) - p)
            }
            view += OutputSlotView(getOutput(3) ?: ItemStack.EMPTY).noBackground().noMargin().configure {
                position = AbsoluteView.Offset(IntPoint(129, 46) - p)
            }

        }
    }
}
