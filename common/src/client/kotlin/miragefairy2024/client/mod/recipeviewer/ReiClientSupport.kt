package miragefairy2024.client.mod.recipeviewer

import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import me.shedaniel.rei.api.client.gui.Renderer
import me.shedaniel.rei.api.client.gui.widgets.Widget
import me.shedaniel.rei.api.client.gui.widgets.Widgets
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry
import me.shedaniel.rei.api.client.registry.display.DisplayCategory
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry
import me.shedaniel.rei.api.common.entry.EntryIngredient
import me.shedaniel.rei.api.common.util.EntryIngredients
import me.shedaniel.rei.plugin.client.BuiltinClientPlugin
import miragefairy2024.InitializationEventRegistry
import miragefairy2024.ModContext
import miragefairy2024.client.mod.rei.ClientReiCategoryCard
import miragefairy2024.mod.HarvestNotation
import miragefairy2024.mod.HarvestNotationRecipeViewerCategoryCard
import miragefairy2024.mod.materials.MaterialCard
import miragefairy2024.mod.recipeviewer.RecipeViewerCategoryCard
import miragefairy2024.mod.recipeviewer.RecipeViewerEvents
import miragefairy2024.mod.recipeviewer.ReiSupport
import miragefairy2024.util.createItemStack
import miragefairy2024.util.invoke
import miragefairy2024.util.plus
import miragefairy2024.util.text
import miragefairy2024.util.toEntryStack
import net.minecraft.network.chat.Component

object ReiClientEvents {
    val onRegisterCategories = InitializationEventRegistry<(CategoryRegistry) -> Unit>()
    val onRegisterDisplays = InitializationEventRegistry<(DisplayRegistry) -> Unit>()
    val onRegisterScreens = InitializationEventRegistry<(ScreenRegistry) -> Unit>()
}

context(ModContext)
fun initReiClientSupport() {
    ReiClientEvents.onRegisterCategories {
        ClientReiCategoryCard.entries.forEach { card ->
            val category = card.createCategory()
            it.add(category)
            it.addWorkstations(category.categoryIdentifier, *card.getWorkstations().toTypedArray())
        }
        ReiClientSupport.instance.let { card ->
            card.registerCategories(it)
        }
    }
    ReiClientEvents.onRegisterDisplays {
        ClientReiCategoryCard.entries.forEach { card ->
            card.registerDisplays(it)
        }
        RecipeViewerEvents.informationEntries.forEach { informationEntry ->
            BuiltinClientPlugin.getInstance().registerInformation(
                EntryIngredients.ofIngredient(informationEntry.input()),
                informationEntry.title,
            ) { list -> list.also { list2 -> list2 += listOf(text { "== "() + informationEntry.title + " =="() }) + informationEntry.contents } }
        }
        ReiClientSupport.instance.let { card ->
            card.registerDisplays(it)
        }
    }
    ReiClientEvents.onRegisterScreens {
        ClientReiCategoryCard.entries.forEach { card ->
            card.registerScreens(it)
        }
        ReiClientSupport.instance.let { card ->
            card.registerScreens(it)
        }
    }
}

class ReiClientSupport<R> private constructor(val card: RecipeViewerCategoryCard<R>) {
    companion object {
        val instance by lazy { ReiClientSupport(HarvestNotationRecipeViewerCategoryCard) }
    }

    fun registerCategories(registry: CategoryRegistry) {
        val category = createCategory()
        registry.add(category)
        registry.addWorkstations(category.categoryIdentifier, *getWorkstations().toTypedArray())
    }

    fun registerDisplays(registry: DisplayRegistry) {
        HarvestNotation.getAll().forEach { (_, recipe) ->
            registry.add(ReiSupport.instance.Display(recipe))
        }
    }

    fun createCategory() = object : DisplayCategory<ReiSupport.Display> {
        override fun getCategoryIdentifier() = ReiSupport.instance.identifier.first
        override fun getTitle(): Component = text { ReiSupport.instance.translation() }
        override fun getIcon(): Renderer = MaterialCard.VEROPEDA_BERRIES.item().createItemStack().toEntryStack()
        override fun getDisplayWidth(display: ReiSupport.Display) = 136
        override fun getDisplayHeight() = 36
        override fun setupDisplay(display: ReiSupport.Display, bounds: Rectangle): List<Widget> {
            val p = bounds.location + Point(3, 3)
            return listOf(
                Widgets.createRecipeBase(bounds),

                Widgets.createSlotBackground(p + Point(15 - 8, 15 - 8)), // 入力スロット背景
                Widgets.createSlot(p + Point(15 - 8, 15 - 8)).entries(display.inputEntries[0]).disableBackground().markInput(), // 入力アイテム

                Widgets.createSlotBase(Rectangle(p.x + 28 + 15 - 8 - 5, p.y + 15 - 8 - 5, 16 * 5 + 2 * 4 + 10, 16 + 10)), // 出力スロット背景
                Widgets.createSlot(p + Point(28 + 15 - 8 + (16 + 2) * 0, 15 - 8)).entries(display.outputEntries.getOrNull(0) ?: EntryIngredient.empty()).disableBackground().markOutput(), // 出力アイテム
                Widgets.createSlot(p + Point(28 + 15 - 8 + (16 + 2) * 1, 15 - 8)).entries(display.outputEntries.getOrNull(1) ?: EntryIngredient.empty()).disableBackground().markOutput(), // 出力アイテム
                Widgets.createSlot(p + Point(28 + 15 - 8 + (16 + 2) * 2, 15 - 8)).entries(display.outputEntries.getOrNull(2) ?: EntryIngredient.empty()).disableBackground().markOutput(), // 出力アイテム
                Widgets.createSlot(p + Point(28 + 15 - 8 + (16 + 2) * 3, 15 - 8)).entries(display.outputEntries.getOrNull(3) ?: EntryIngredient.empty()).disableBackground().markOutput(), // 出力アイテム
                Widgets.createSlot(p + Point(28 + 15 - 8 + (16 + 2) * 4, 15 - 8)).entries(display.outputEntries.getOrNull(4) ?: EntryIngredient.empty()).disableBackground().markOutput(), // 出力アイテム
            )
        }
    }

    fun getWorkstations(): List<EntryIngredient> = listOf()

    fun registerScreens(registry: ScreenRegistry) = Unit
}
