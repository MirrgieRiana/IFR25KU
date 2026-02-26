package miragefairy2024.mod.machine

import miragefairy2024.MirageFairy2024
import miragefairy2024.mod.materials.MaterialCard
import miragefairy2024.mod.recipeviewer.RecipeViewerCategoryCard
import miragefairy2024.mod.recipeviewer.toSecondsTextAsTicks
import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.ColorPair
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.views.InputSlotView
import miragefairy2024.mod.recipeviewer.views.TextView
import miragefairy2024.mod.recipeviewer.views.View
import miragefairy2024.mod.recipeviewer.views.XListView
import miragefairy2024.mod.recipeviewer.views.XSpaceView
import miragefairy2024.mod.recipeviewer.views.configure
import miragefairy2024.mod.recipeviewer.views.minContentSizeX
import miragefairy2024.mod.recipeviewer.views.plusAssign
import miragefairy2024.util.EnJa
import miragefairy2024.util.createItemStack
import miragefairy2024.util.get
import miragefairy2024.util.toIngredientStack
import net.minecraft.core.RegistryAccess
import net.minecraft.core.registries.Registries

object AuraReflectorFurnaceFuelRecipeViewerCategoryCard : RecipeViewerCategoryCard<AuraReflectorFurnaceFuel>() {
    override fun getId() = MirageFairy2024.identifier("aura_reflector_furnace_fuel")
    override fun getName() = EnJa("Aura Reflector Furnace Fuel", "オーラ反射炉の燃料")
    override fun getIcon() = MaterialCard.PROMINITE.item().createItemStack()
    override fun getWorkstations() = listOf(AuraReflectorFurnaceCard.item().createItemStack())
    override fun getRecipeCodec(registryAccess: RegistryAccess) = AuraReflectorFurnaceFuel.CODEC
    override fun getInputs(recipeEntry: RecipeEntry<AuraReflectorFurnaceFuel>) = listOf(Input(recipeEntry.registryAccess[Registries.ITEM, recipeEntry.recipe.fuel].value().toIngredientStack(), false))

    override fun createRecipeEntries(registryAccess: RegistryAccess): Iterable<RecipeEntry<AuraReflectorFurnaceFuel>> {
        return AuraReflectorFurnaceRecipe.FUELS.map { (_, recipe) ->
            RecipeEntry(registryAccess, recipe.fuel.location(), recipe, true)
        }
    }

    override fun createView(recipeEntry: RecipeEntry<AuraReflectorFurnaceFuel>) = View {
        view += XListView().configure {
            view += InputSlotView(recipeEntry.registryAccess[Registries.ITEM, recipeEntry.recipe.fuel].value().toIngredientStack())
            view += XSpaceView(4)
            view += BlueFuelView().configure {
                position.alignmentY = Alignment.CENTER
            }
            view += XSpaceView(4)
            view += TextView(recipeEntry.recipe.duration.toSecondsTextAsTicks()).configure {
                position.alignmentY = Alignment.CENTER
                view.sizingX = Sizing.FILL
                view.alignmentX = Alignment.CENTER
                view.color = ColorPair.DARK_GRAY
                view.shadow = false
            }.minContentSizeX(48)
        }
    }
}
