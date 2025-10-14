package miragefairy2024.client.mod.recipeviewer.rei

import me.shedaniel.math.Rectangle
import me.shedaniel.rei.api.client.gui.Renderer
import me.shedaniel.rei.api.client.gui.widgets.Widget
import me.shedaniel.rei.api.client.gui.widgets.Widgets
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry
import me.shedaniel.rei.api.client.registry.display.DisplayCategory
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry
import me.shedaniel.rei.api.common.util.EntryIngredients
import me.shedaniel.rei.plugin.client.BuiltinClientPlugin
import miragefairy2024.ModContext
import miragefairy2024.ReusableInitializationEventRegistry
import miragefairy2024.client.mod.recipeviewer.ScreenClassRegistry
import miragefairy2024.client.mod.recipeviewer.renderingProxy
import miragefairy2024.mod.recipeviewer.RecipeViewerCategoryCard
import miragefairy2024.mod.recipeviewer.RecipeViewerCategoryCardRecipeManagerBridge
import miragefairy2024.mod.recipeviewer.RecipeViewerEvents
import miragefairy2024.mod.recipeviewer.rei.ReiSupport
import miragefairy2024.mod.recipeviewer.rei.SupportedDisplay
import miragefairy2024.mod.recipeviewer.view.ViewPlacerRegistry
import miragefairy2024.util.invoke
import miragefairy2024.util.plus
import miragefairy2024.util.text
import miragefairy2024.util.toEntryStack
import mirrg.kotlin.helium.max
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeInput

object ReiClientEvents {
    val onRegisterCategories = ReusableInitializationEventRegistry<(CategoryRegistry) -> Unit>()
    val onRegisterDisplays = ReusableInitializationEventRegistry<(DisplayRegistry) -> Unit>()
    val onRegisterScreens = ReusableInitializationEventRegistry<(ScreenRegistry) -> Unit>()
}

val REI_VIEW_PLACER_REGISTRY = ViewPlacerRegistry<MutableList<Widget>>()

context(ModContext)
fun initReiClientSupport() {
    RecipeViewerEvents.informationEntries.subscribe { informationEntry ->
        ReiClientEvents.onRegisterDisplays { _ ->
            BuiltinClientPlugin.getInstance().registerInformation(
                EntryIngredients.ofIngredient(informationEntry.input()),
                informationEntry.title,
            ) { list -> list.also { list2 -> list2 += listOf(text { "== "() + informationEntry.title + " =="() }) + informationEntry.contents } }
        }
    }

    RecipeViewerEvents.recipeViewerCategoryCards.subscribe { card ->
        ReiClientEvents.onRegisterCategories { registry ->
            ReiClientSupport.get(card).registerCategories(registry)
        }
        ReiClientEvents.onRegisterDisplays { registry ->
            ReiClientSupport.get(card).registerDisplays(registry)
        }
        ReiClientEvents.onRegisterScreens { registry ->
            ReiClientSupport.get(card).registerScreens(registry)
        }
    }

    RecipeViewerEvents.recipeViewerCategoryCardRecipeManagerBridges.subscribe { bridge ->
        ReiClientEvents.onRegisterDisplays { registry ->
            fun <I : RecipeInput, R : Recipe<I>> f(bridge: RecipeViewerCategoryCardRecipeManagerBridge<I, R>) {
                val support = ReiSupport.get(bridge.card)
                registry.registerRecipeFiller(bridge.recipeClass, bridge.recipeType) { holder ->
                    val recipeEntry = RecipeViewerCategoryCard.RecipeEntry(holder.id(), holder.value(), false)
                    SupportedDisplay(support, recipeEntry)
                }
            }
            f(bridge)
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

    private var heightCache = 0

    val displayCategory = object : DisplayCategory<SupportedDisplay<R>> {
        override fun getCategoryIdentifier() = ReiSupport.get(card).categoryIdentifier.first
        override fun getTitle(): Component = card.displayName
        override fun getIcon(): Renderer = card.getIcon().toEntryStack()
        override fun getDisplayWidth(display: SupportedDisplay<R>) = 5 + card.getView(renderingProxy, display.recipeEntry).actualSize.x + 5
        override fun getDisplayHeight() = 5 + heightCache + 5
        override fun setupDisplay(display: SupportedDisplay<R>, bounds: Rectangle): List<Widget> {
            val widgets = mutableListOf<Widget>()
            widgets += Widgets.createRecipeBase(bounds)
            val view = card.getView(renderingProxy, display.recipeEntry)
            view.attachTo(5 + bounds.x, 5 + bounds.y) { view2, bounds ->
                REI_VIEW_PLACER_REGISTRY.place(widgets, view2, bounds)
            }
            return widgets
        }
    }

    fun registerCategories(registry: CategoryRegistry) {
        registry.add(displayCategory)
        registry.addWorkstations(displayCategory.categoryIdentifier, *card.getWorkstations().map { it.toEntryStack() }.toTypedArray())
    }

    fun registerDisplays(registry: DisplayRegistry) {
        val recipeManager = registry.recipeManager
        val recipeEntries = card.createRecipeEntries()

        // 高さの事前計算
        heightCache = 0
        RecipeViewerEvents.recipeViewerCategoryCardRecipeManagerBridges.getAllImmediately().forEach { bridge ->
            if (bridge.card === card) {
                fun <I : RecipeInput, R : Recipe<I>> calculateMaxHeight(bridge: RecipeViewerCategoryCardRecipeManagerBridge<I, R>) {
                    recipeManager.getAllRecipesFor(bridge.recipeType).forEach {
                        val recipeEntry = RecipeViewerCategoryCard.RecipeEntry(it.id(), it.value(), false)
                        heightCache = heightCache max bridge.card.getView(renderingProxy, recipeEntry).actualSize.y
                    }
                }
                calculateMaxHeight(bridge)
            }
        }
        recipeEntries.forEach {
            heightCache = heightCache max card.getView(renderingProxy, it).actualSize.y
        }

        // レシピ登録
        recipeEntries.forEach {
            registry.add(SupportedDisplay(ReiSupport.get(card), it))
        }
    }

    fun registerScreens(registry: ScreenRegistry) {
        card.getScreenClickAreas().forEach {
            fun <C : AbstractContainerMenu, T : AbstractContainerScreen<C>> f(get: ScreenClassRegistry.ScreenClass<C, T>) {
                val rectangle = Rectangle(it.second.x, it.second.y, it.second.sizeX - 1, it.second.sizeY - 1)
                registry.registerContainerClickArea(rectangle, get.clazz, ReiSupport.get(card).categoryIdentifier.first)
            }
            f(ScreenClassRegistry.get(it.first))
        }
    }

}
