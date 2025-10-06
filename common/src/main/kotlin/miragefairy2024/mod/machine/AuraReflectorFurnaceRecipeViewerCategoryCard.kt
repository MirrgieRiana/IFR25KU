package miragefairy2024.mod.machine

import me.shedaniel.rei.api.common.display.basic.BasicDisplay
import miragefairy2024.MirageFairy2024
import miragefairy2024.mod.recipeviewer.AbsoluteView
import miragefairy2024.mod.recipeviewer.Alignment
import miragefairy2024.mod.recipeviewer.ArrowView
import miragefairy2024.mod.recipeviewer.CatalystSlotView
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
import miragefairy2024.mod.recipeviewer.size
import miragefairy2024.mod.recipeviewer.topLeft
import miragefairy2024.util.EnJa
import miragefairy2024.util.IngredientStack
import miragefairy2024.util.get
import miragefairy2024.util.invoke
import miragefairy2024.util.text
import miragefairy2024.util.times
import miragefairy2024.util.toIngredientStack
import mirrg.kotlin.helium.stripTrailingZeros
import mirrg.kotlin.hydrogen.formatAs
import net.minecraft.core.registries.Registries

object AuraReflectorFurnaceRecipeViewerCategoryCard : SimpleMachineRecipeViewerCategoryCard<AuraReflectorFurnaceRecipe>() {
    override fun getId() = MirageFairy2024.identifier("aura_reflector_furnace")
    override fun getName() = EnJa("Aura Reflector Furnace", "オーラ反射炉")
    private fun getFuelIngredientStack() = AuraReflectorFurnaceRecipe.FUELS.map { BasicDisplay.registryAccess()[Registries.ITEM, it.key].value() }.toIngredientStack()
    override fun getInputs(recipeEntry: RecipeEntry<AuraReflectorFurnaceRecipe>) = super.getInputs(recipeEntry) + listOf(Input(getFuelIngredientStack(), true))
    override fun getRecipeCard() = AuraReflectorFurnaceRecipeCard
    override fun getMachineCard() = AuraReflectorFurnaceCard
    override fun getScreenClickAreas() = listOf(Pair(getMachineCard().screenHandlerType.key, IntRectangle(88, 34, 24, 17)))

    override fun createView(recipeEntry: RecipeEntry<AuraReflectorFurnaceRecipe>) = View {
        val imageBound = IntRectangle(28, 16, 116, 54)
        val bound = imageBound.grow(6, 2)
        val p = bound.topLeft
        this += AbsoluteView(bound.size) {

            this += ImageView("textures/gui/container/" * AuraReflectorFurnaceRecipeCard.identifier * ".png", bound, IntPoint(256, 256))

            fun getInput(index: Int) = recipeEntry.recipe.inputs.getOrNull(index) ?: IngredientStack.EMPTY
            this += Pair(IntPoint(29, 17) - p, null) to InputSlotView(getInput(0)).noBackground().noMargin()
            this += Pair(IntPoint(47, 17) - p, null) to InputSlotView(getInput(1)).noBackground().noMargin()
            this += Pair(IntPoint(65, 17) - p, null) to InputSlotView(getInput(2)).noBackground().noMargin()
            this += Pair(IntPoint(47, 53) - p, null) to CatalystSlotView(getFuelIngredientStack()).noBackground().noMargin()
            this += Pair(IntPoint(48, 37) - p, null) to BlueFuelView()

            this += Pair(IntPoint(88, 34) - p, null) to ArrowView().apply {
                durationMilliSeconds = recipeEntry.recipe.duration * 50
            }
            val seconds = recipeEntry.recipe.duration.toDouble() / 20.0
            this += Pair(IntPoint(108, 18) - p, null) to TextView(text { SECONDS_TRANSLATION((seconds formatAs "%.2f").stripTrailingZeros()) }).apply {
                xAlignment = Alignment.CENTER
                color = ColorPair.DARK_GRAY
                shadow = false
            }

            this += Pair(IntPoint(123, 35) - p, null) to OutputSlotView(recipeEntry.recipe.output).noBackground().noMargin()

        }
    }
}
