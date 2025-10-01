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
import miragefairy2024.mod.recipeviewer.RecipeViewerCategoryCard
import miragefairy2024.mod.recipeviewer.RecipeViewerEvents
import miragefairy2024.mod.recipeviewer.ReiSupport
import miragefairy2024.mod.recipeviewer.SupportedDisplay
import miragefairy2024.mod.recipeviewer.WidgetProxy
import miragefairy2024.util.invoke
import miragefairy2024.util.plus
import miragefairy2024.util.text
import miragefairy2024.util.toEntryIngredient
import miragefairy2024.util.toEntryStack
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient

object ReiClientEvents {
    val onRegisterCategories = InitializationEventRegistry<(CategoryRegistry) -> Unit>()
    val onRegisterDisplays = InitializationEventRegistry<(DisplayRegistry) -> Unit>()
    val onRegisterScreens = InitializationEventRegistry<(ScreenRegistry) -> Unit>()
}

context(ModContext)
fun initReiClientSupport() {
    ReiClientEvents.onRegisterDisplays {
        RecipeViewerEvents.informationEntries.forEach { informationEntry ->
            BuiltinClientPlugin.getInstance().registerInformation(
                EntryIngredients.ofIngredient(informationEntry.input()),
                informationEntry.title,
            ) { list -> list.also { list2 -> list2 += listOf(text { "== "() + informationEntry.title + " =="() }) + informationEntry.contents } }
        }
    }

    ReiClientEvents.onRegisterCategories {
        ClientReiCategoryCard.entries.forEach { card ->
            val category = card.createCategory()
            it.add(category)
            it.addWorkstations(category.categoryIdentifier, *card.getWorkstations().toTypedArray())
        }
    }
    ReiClientEvents.onRegisterDisplays {
        ClientReiCategoryCard.entries.forEach { card ->
            card.registerDisplays(it)
        }
    }
    ReiClientEvents.onRegisterScreens {
        ClientReiCategoryCard.entries.forEach { card ->
            card.registerScreens(it)
        }
    }

    ReiClientEvents.onRegisterCategories {
        RecipeViewerEvents.recipeViewerCategoryCards.forEach { card ->
            ReiClientSupport.get(card).registerCategories(it)
        }
    }
    ReiClientEvents.onRegisterDisplays {
        RecipeViewerEvents.recipeViewerCategoryCards.forEach { card ->
            ReiClientSupport.get(card).registerDisplays(it)
        }
    }
    ReiClientEvents.onRegisterScreens {
        RecipeViewerEvents.recipeViewerCategoryCards.forEach { card ->
            ReiClientSupport.get(card).registerScreens(it)
        }
    }
}

class ReiClientSupport<R> private constructor(val card: RecipeViewerCategoryCard<R>) {
    companion object {
        private val table = mutableMapOf<RecipeViewerCategoryCard<*>, ReiClientSupport<*>>()
        fun <R> get(card: RecipeViewerCategoryCard<R>): ReiClientSupport<R> {
            @Suppress("UNCHECKED_CAST")
            return table.getOrPut(card) { ReiClientSupport(card) } as ReiClientSupport<R>
        }
    }

    val displayCategory = object : DisplayCategory<SupportedDisplay<R>> {
        override fun getCategoryIdentifier() = ReiSupport.get(card).categoryIdentifier.first
        override fun getTitle(): Component = card.displayName
        override fun getIcon(): Renderer = card.getIcon().toEntryStack()
        override fun getDisplayWidth(display: SupportedDisplay<R>) = 136
        override fun getDisplayHeight() = 36
        override fun setupDisplay(display: SupportedDisplay<R>, bounds: Rectangle): List<Widget> {
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

    fun registerCategories(registry: CategoryRegistry) {
        registry.add(displayCategory)
        registry.addWorkstations(displayCategory.categoryIdentifier, *getWorkstations().toTypedArray())
    }

    fun registerDisplays(registry: DisplayRegistry) {
        card.getRecipes().forEach {
            registry.add(SupportedDisplay(ReiSupport.get(card), it))
        }
    }

    fun getWorkstations(): List<EntryIngredient> = listOf()

    fun registerScreens(registry: ScreenRegistry) = Unit

}

private fun getReiWidgetProxy(widgets: MutableList<Widget>): WidgetProxy {
    return object : WidgetProxy {
        override fun addInputSlotWidget(ingredient: Ingredient, x: Int, y: Int) {
            widgets += Widgets.createSlot(Point(x + 1, y + 1)).entries(ingredient.toEntryIngredient()).markInput()
        }

        override fun addCatalystSlotWidget(ingredient: Ingredient, x: Int, y: Int) {
            addInputSlotWidget(ingredient, x, y)
        }

        override fun addOutputSlotWidget(itemStack: ItemStack, x: Int, y: Int) {
            widgets += Widgets.createSlot(Point(x + 1, y + 1)).entries(itemStack.toEntryStack().toEntryIngredient()).markOutput()
        }
    }
}
