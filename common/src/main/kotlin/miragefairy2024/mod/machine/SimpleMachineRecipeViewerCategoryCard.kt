package miragefairy2024.mod.machine

import com.mojang.serialization.Codec
import miragefairy2024.ModContext
import miragefairy2024.mod.recipeviewer.CONSUMPTION_CHANCE_TRANSLATION
import miragefairy2024.mod.recipeviewer.RecipeViewerCategoryCard
import miragefairy2024.mod.recipeviewer.RecipeViewerCategoryCardRecipeManagerBridge
import miragefairy2024.mod.recipeviewer.RecipeViewerEvents
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.ViewTexture
import miragefairy2024.mod.recipeviewer.views.AbsoluteView
import miragefairy2024.mod.recipeviewer.views.Child
import miragefairy2024.mod.recipeviewer.views.FilledRectangleView
import miragefairy2024.mod.recipeviewer.views.InputSlotView
import miragefairy2024.mod.recipeviewer.views.ParentView
import miragefairy2024.mod.recipeviewer.views.StackView
import miragefairy2024.mod.recipeviewer.views.configure
import miragefairy2024.mod.recipeviewer.views.noBackground
import miragefairy2024.mod.recipeviewer.views.noMargin
import miragefairy2024.mod.recipeviewer.views.plusAssign
import miragefairy2024.mod.recipeviewer.views.tooltip
import miragefairy2024.util.IngredientStack
import miragefairy2024.util.createItemStack
import miragefairy2024.util.invoke
import miragefairy2024.util.plus
import miragefairy2024.util.plusAssign
import miragefairy2024.util.text
import miragefairy2024.util.toIngredientStack
import mirrg.kotlin.helium.stripTrailingZeros
import mirrg.kotlin.hydrogen.formatAs
import net.minecraft.core.RegistryAccess
import kotlin.math.roundToInt

abstract class SimpleMachineRecipeViewerCategoryCard<R : SimpleMachineRecipe> : RecipeViewerCategoryCard<R>() {
    override fun getIcon() = getRecipeCard().getIcon()
    override fun getWorkstations() = listOf(getMachineCard().item().createItemStack())
    override fun getRecipeCodec(registryAccess: RegistryAccess): Codec<R> = getRecipeCard().serializer.codec().codec()
    override fun getInputs(recipeEntry: RecipeEntry<R>) = recipeEntry.recipe.inputs.map { input -> Input(input.ingredient.toIngredientStack(input.count), false) }
    override fun getOutputs(recipeEntry: RecipeEntry<R>) = recipeEntry.recipe.outputs
    abstract fun getRecipeCard(): SimpleMachineRecipeCard<R>
    abstract fun getMachineCard(): SimpleMachineCard<*, *, *, R>

    protected fun getTexture(bounds: IntRectangle) = ViewTexture(getMachineCard().backgroundTexture, getMachineCard().backgroundTextureSize, bounds)

    context(ModContext)
    override fun init() {
        super.init()
        RecipeViewerEvents.recipeViewerCategoryCardRecipeManagerBridges += RecipeViewerCategoryCardRecipeManagerBridge(getRecipeCard().recipeClass, getRecipeCard().type, this)
    }

    context(Child<*, out ParentView<AbsoluteView.Position>>)
    protected fun createInputSlot(recipeEntry: RecipeEntry<R>, index: Int, offset: IntPoint): Child<AbsoluteView.Position, StackView> {
        val input = recipeEntry.recipe.inputs.getOrNull(index)
        return StackView().configure {
            position = AbsoluteView.Offset(offset)
            view.sizingX = Sizing.FILL
            view.sizingY = Sizing.FILL

            // 消費されないアイテムの背景色を緑に
            if (input != null && input.consumptionChance < 1.0) {
                val alpha = if (input.consumptionChance <= 0.0) {
                    255
                } else {
                    ((0.8 - input.consumptionChance * 0.6) * 255.0).roundToInt()
                }
                val color = (alpha shl 24) or 0x00FF00
                view += FilledRectangleView().also { it.color.value = color }
            }

            // アイテム
            view += InputSlotView(input?.ingredientStack ?: IngredientStack.EMPTY).noBackground().noMargin().configure().let {
                if (input != null && input.consumptionChance < 1.0) {
                    it.tooltip(text { CONSUMPTION_CHANCE_TRANSLATION() + ": ${(input.consumptionChance * 100.0 formatAs "%.8f").stripTrailingZeros()}%"() })
                } else {
                    it
                }
            }

        }
    }
}
