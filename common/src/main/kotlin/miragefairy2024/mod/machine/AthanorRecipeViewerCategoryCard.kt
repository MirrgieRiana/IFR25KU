package miragefairy2024.mod.machine

import com.mojang.serialization.Codec
import dev.architectury.registry.fuel.FuelRegistry
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.recipeviewer.RecipeViewerCategoryCard
import miragefairy2024.mod.recipeviewer.RecipeViewerCategoryCardRecipeManagerBridge
import miragefairy2024.mod.recipeviewer.RecipeViewerEvents
import miragefairy2024.mod.recipeviewer.toSecondsTextAsTicks
import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.ColorPair
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.ViewTexture
import miragefairy2024.mod.recipeviewer.view.grow
import miragefairy2024.mod.recipeviewer.view.minus
import miragefairy2024.mod.recipeviewer.view.offset
import miragefairy2024.mod.recipeviewer.view.size
import miragefairy2024.mod.recipeviewer.views.AbsoluteView
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
import miragefairy2024.util.plusAssign
import miragefairy2024.util.toIngredientStack
import net.minecraft.core.RegistryAccess
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.ItemStack

object AthanorRecipeViewerCategoryCard : RecipeViewerCategoryCard<AthanorRecipe>() {
    override fun getId() = MirageFairy2024.identifier("athanor")
    override fun getName() = EnJa("Athanor", "アタノール")
    override fun getIcon() = AthanorRecipeCard.getIcon()
    override fun getWorkstations() = listOf<ItemStack>() // TODO AthanorCard.item().createItemStack()
    override fun getRecipeCodec(registryAccess: RegistryAccess): Codec<AthanorRecipe> = AthanorRecipeCard.serializer.codec().codec()
    private fun getFuelIngredientStack(): IngredientStack = BuiltInRegistries.ITEM.filter { FuelRegistry.get(it.defaultInstance) != 0 }.toIngredientStack()
    override fun getInputs(recipeEntry: RecipeEntry<AthanorRecipe>) = recipeEntry.recipe.inputs.map { input -> Input(input.ingredient.toIngredientStack(input.count), false) } + listOf(Input(getFuelIngredientStack(), true))
    override fun getOutputs(recipeEntry: RecipeEntry<AthanorRecipe>) = recipeEntry.recipe.outputs

    private val backgroundTexture = MirageFairy2024.identifier("textures/gui/container/athanor.png")
    private val backgroundTextureSize = IntPoint(256, 256)

    override fun createView(recipeEntry: RecipeEntry<AthanorRecipe>) = View {
        val imageBound = IntRectangle(17, 16, 142, 62)
        val bounds = imageBound.grow(6, 2)
        val p = bounds.offset
        view += AbsoluteView(bounds.size).configure {

            view += ImageView(ViewTexture(backgroundTexture, backgroundTextureSize, bounds))

            fun getInput(index: Int) = recipeEntry.recipe.inputs.getOrNull(index) ?: IngredientStack.EMPTY
            view += InputSlotView(getInput(0)).noBackground().noMargin().configure {
                position = AbsoluteView.Offset(IntPoint(40, 17) - p)
            }
            view += InputSlotView(getInput(1)).noBackground().noMargin().configure {
                position = AbsoluteView.Offset(IntPoint(18, 39) - p)
            }
            view += InputSlotView(getInput(2)).noBackground().noMargin().configure {
                position = AbsoluteView.Offset(IntPoint(62, 39) - p)
            }
            view += InputSlotView(getInput(3)).noBackground().noMargin().configure {
                position = AbsoluteView.Offset(IntPoint(40, 61) - p)
            }
            view += CatalystSlotView(getFuelIngredientStack()).noBackground().noMargin().configure {
                position = AbsoluteView.Offset(IntPoint(40, 39) - p)
            }
            view += FuelView().configure {
                position = AbsoluteView.Offset(IntPoint(90, 64) - p)
            }

            view += TexturedArrowView(IntPoint(24, 16)).configure {
                position = AbsoluteView.Offset(IntPoint(84, 39) - p)
                view.backgroundTexture = ViewTexture(MirageFairy2024.identifier("textures/gui/sprites/athanor_progress.png"), IntPoint(32, 32), IntRectangle(0, 16, 24, 16))
                view.foregroundTexture = ViewTexture(MirageFairy2024.identifier("textures/gui/sprites/athanor_progress.png"), IntPoint(32, 32), IntRectangle(0, 0, 24, 16))
                view.durationMilliSeconds = recipeEntry.recipe.duration * 50
            }
            view += TextView(recipeEntry.recipe.duration.toSecondsTextAsTicks()).configure {
                position = AbsoluteView.Offset(IntPoint(85, 19) - p)
                view.alignmentX = Alignment.CENTER
                view.color = ColorPair.DARK_GRAY
                view.shadow = false
            }

            fun getOutput(index: Int) = recipeEntry.recipe.outputs.getOrNull(index) ?: ItemStack.EMPTY
            view += OutputSlotView(getOutput(0)).noBackground().noMargin().configure {
                position = AbsoluteView.Offset(IntPoint(120, 30) - p)
            }
            view += OutputSlotView(getOutput(1)).noBackground().noMargin().configure {
                position = AbsoluteView.Offset(IntPoint(138, 30) - p)
            }
            view += OutputSlotView(getOutput(2)).noBackground().noMargin().configure {
                position = AbsoluteView.Offset(IntPoint(120, 48) - p)
            }
            view += OutputSlotView(getOutput(3)).noBackground().noMargin().configure {
                position = AbsoluteView.Offset(IntPoint(138, 48) - p)
            }

        }
    }

    context(ModContext)
    override fun init() {
        super.init()
        RecipeViewerEvents.recipeViewerCategoryCardRecipeManagerBridges += RecipeViewerCategoryCardRecipeManagerBridge(AthanorRecipeCard.recipeClass, AthanorRecipeCard.type, this)
    }
}
