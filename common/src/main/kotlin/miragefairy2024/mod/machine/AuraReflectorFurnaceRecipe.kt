package miragefairy2024.mod.machine

import com.mojang.serialization.Codec
import me.shedaniel.rei.api.common.display.basic.BasicDisplay
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
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
import miragefairy2024.mod.recipeviewer.RecipeViewerCategoryCard
import miragefairy2024.mod.recipeviewer.RecipeViewerCategoryCardRecipeManagerBridge
import miragefairy2024.mod.recipeviewer.RecipeViewerEvents
import miragefairy2024.mod.recipeviewer.SECONDS_TRANSLATION
import miragefairy2024.mod.recipeviewer.TextView
import miragefairy2024.mod.recipeviewer.View
import miragefairy2024.mod.recipeviewer.grow
import miragefairy2024.mod.recipeviewer.minus
import miragefairy2024.mod.recipeviewer.noBackground
import miragefairy2024.mod.recipeviewer.plusAssign
import miragefairy2024.mod.recipeviewer.topLeft
import miragefairy2024.util.EnJa
import miragefairy2024.util.IngredientStack
import miragefairy2024.util.createItemStack
import miragefairy2024.util.get
import miragefairy2024.util.invoke
import miragefairy2024.util.plusAssign
import miragefairy2024.util.text
import miragefairy2024.util.times
import miragefairy2024.util.toIngredientStack
import mirrg.kotlin.helium.stripTrailingZeros
import mirrg.kotlin.hydrogen.formatAs
import net.minecraft.core.RegistryAccess
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

object AuraReflectorFurnaceRecipeCard : SimpleMachineRecipeCard<AuraReflectorFurnaceRecipe>() {
    override val identifier = MirageFairy2024.identifier("aura_reflector_furnace")
    override fun getIcon() = AuraReflectorFurnaceCard.item().createItemStack()
    override val recipeClass = AuraReflectorFurnaceRecipe::class.java
    override fun createRecipe(group: String, inputs: List<IngredientStack>, output: ItemStack, duration: Int): AuraReflectorFurnaceRecipe {
        return AuraReflectorFurnaceRecipe(this, group, inputs, output, duration)
    }
}

class AuraReflectorFurnaceRecipe(
    card: AuraReflectorFurnaceRecipeCard,
    group: String,
    inputs: List<IngredientStack>,
    output: ItemStack,
    duration: Int,
) : SimpleMachineRecipe(
    card,
    group,
    inputs,
    output,
    duration,
) {
    companion object {
        val FUELS = mutableMapOf<ResourceKey<Item>, Int>()

        init {
            FUELS[BuiltInRegistries.ITEM.getResourceKey(Items.SOUL_SAND).get()] = 20 * 10
            FUELS[BuiltInRegistries.ITEM.getResourceKey(Items.SOUL_SOIL).get()] = 20 * 10
        }

        fun getFuelValue(item: Item) = FUELS[BuiltInRegistries.ITEM.getResourceKey(item).get()]
    }
}

abstract class SimpleMachineRecipeViewerCategoryCard<R : SimpleMachineRecipe> : RecipeViewerCategoryCard<R>() {
    override fun getIcon() = getRecipeCard().getIcon()
    override fun getWorkstations() = listOf(getMachineCard().item().createItemStack())
    override fun getRecipeCodec(registryAccess: RegistryAccess): Codec<R> = getRecipeCard().serializer.codec().codec()
    override fun getInputs(recipeEntry: RecipeEntry<R>) = recipeEntry.recipe.inputs.map { input -> Input(input.ingredient.toIngredientStack(input.count), false) }
    override fun getOutputs(recipeEntry: RecipeEntry<R>) = listOf(recipeEntry.recipe.output)
    abstract fun getRecipeCard(): SimpleMachineRecipeCard<R>
    abstract fun getMachineCard(): SimpleMachineCard<*, *, *, R>

    context(ModContext)
    override fun init() {
        super.init()
        RecipeViewerEvents.recipeViewerCategoryCardRecipeManagerBridges += RecipeViewerCategoryCardRecipeManagerBridge(getRecipeCard().recipeClass, getRecipeCard().type, this)
    }
}

object FermentationBarrelRecipeViewerCategoryCard : SimpleMachineRecipeViewerCategoryCard<FermentationBarrelRecipe>() {
    override fun getId() = MirageFairy2024.identifier("fermentation_barrel")
    override fun getName() = EnJa("Fermentation Barrel", "醸造樽")
    override fun getRecipeCard() = FermentationBarrelRecipeCard
    override fun getMachineCard() = FermentationBarrelCard
    override fun getScreenClickAreas() = listOf(Pair(getMachineCard().screenHandlerType.key, IntRectangle(77, 28, 22, 15)))

    override fun createView(recipeEntry: RecipeEntry<FermentationBarrelRecipe>) = View {
        val imageBound = IntRectangle(30, 16, 120, 40)
        val bound = imageBound.grow(6, 2)
        val p = bound.topLeft
        this += AbsoluteView(bound.width, bound.height) {

            this += ImageView("textures/gui/container/" * FermentationBarrelRecipeCard.identifier * ".png", bound)

            fun getInput(index: Int) = recipeEntry.recipe.inputs.getOrNull(index) ?: IngredientStack.EMPTY
            this += (IntPoint(42 - 1, 17 - 1) - p) to InputSlotView(getInput(0)).noBackground()
            this += (IntPoint(31 - 1, 39 - 1) - p) to InputSlotView(getInput(1)).noBackground()
            this += (IntPoint(53 - 1, 39 - 1) - p) to InputSlotView(getInput(2)).noBackground()

            this += (IntPoint(77, 28) - p) to ArrowView().apply {
                durationMilliSeconds = recipeEntry.recipe.duration * 50
            }
            val seconds = recipeEntry.recipe.duration.toDouble() / 20.0
            this += (IntPoint(88, 15) - p) to TextView(text { SECONDS_TRANSLATION((seconds formatAs "%.2f").stripTrailingZeros()) }).apply {
                horizontalAlignment = Alignment.CENTER
                color = ColorPair.DARK_GRAY
                shadow = false
            }

            this += (IntPoint(111 - 1, 28 - 1) - p) to OutputSlotView(recipeEntry.recipe.output).noBackground()

        }
    }
}

object AuraReflectorFurnaceRecipeViewerCategoryCard : SimpleMachineRecipeViewerCategoryCard<AuraReflectorFurnaceRecipe>() {
    override fun getId() = MirageFairy2024.identifier("aura_reflector_furnace")
    override fun getName() = EnJa("Aura Reflector Furnace", "オーラ反射炉")
    private fun getFuelIngredientStack() = AuraReflectorFurnaceRecipe.FUELS.map { BasicDisplay.registryAccess()[Registries.ITEM, it.key].value() }.toIngredientStack()
    override fun getInputs(recipeEntry: RecipeEntry<AuraReflectorFurnaceRecipe>) = super.getInputs(recipeEntry) + listOf(Input(getFuelIngredientStack(), true))
    override fun getRecipeCard() = AuraReflectorFurnaceRecipeCard
    override fun getMachineCard() = AuraReflectorFurnaceCard
    override fun getScreenClickAreas() = listOf(Pair(getMachineCard().screenHandlerType.key, IntRectangle(89, 35, 22, 15)))

    override fun createView(recipeEntry: RecipeEntry<AuraReflectorFurnaceRecipe>) = View {
        val imageBound = IntRectangle(28, 16, 116, 54)
        val bound = imageBound.grow(6, 2)
        val p = bound.topLeft
        this += AbsoluteView(bound.width, bound.height) {

            this += ImageView("textures/gui/container/" * AuraReflectorFurnaceRecipeCard.identifier * ".png", bound)

            fun getInput(index: Int) = recipeEntry.recipe.inputs.getOrNull(index) ?: IngredientStack.EMPTY
            this += (IntPoint(29 - 1, 17 - 1) - p) to InputSlotView(getInput(0)).noBackground()
            this += (IntPoint(47 - 1, 17 - 1) - p) to InputSlotView(getInput(1)).noBackground()
            this += (IntPoint(65 - 1, 17 - 1) - p) to InputSlotView(getInput(2)).noBackground()
            this += (IntPoint(47 - 1, 53 - 1) - p) to CatalystSlotView(getFuelIngredientStack()).noBackground()
            this += (IntPoint(48, 37) - p) to BlueFuelView()

            this += (IntPoint(89, 35) - p) to ArrowView().apply {
                durationMilliSeconds = recipeEntry.recipe.duration * 50
            }
            val seconds = recipeEntry.recipe.duration.toDouble() / 20.0
            this += (IntPoint(108, 18) - p) to TextView(text { SECONDS_TRANSLATION((seconds formatAs "%.2f").stripTrailingZeros()) }).apply {
                horizontalAlignment = Alignment.CENTER
                color = ColorPair.DARK_GRAY
                shadow = false
            }

            this += (IntPoint(123 - 1, 35 - 1) - p) to OutputSlotView(recipeEntry.recipe.output).noBackground()

        }
    }
}
