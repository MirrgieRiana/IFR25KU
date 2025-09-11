package miragefairy2024.client.mod.rei

import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import me.shedaniel.rei.api.client.gui.Renderer
import me.shedaniel.rei.api.client.gui.widgets.Widget
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds
import me.shedaniel.rei.api.client.gui.widgets.Widgets
import me.shedaniel.rei.api.client.registry.display.DisplayCategory
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry
import miragefairy2024.client.mod.AuraReflectorFurnaceScreen
import miragefairy2024.client.mod.FermentationBarrelScreen
import miragefairy2024.mod.machine.AuraReflectorFurnaceRecipe
import miragefairy2024.mod.machine.FermentationBarrelRecipe
import miragefairy2024.mod.machine.SimpleMachineRecipe
import miragefairy2024.mod.rei.AuraReflectorFurnaceReiCategoryCard
import miragefairy2024.mod.rei.FermentationBarrelReiCategoryCard
import miragefairy2024.mod.rei.SimpleMachineReiCategoryCard
import miragefairy2024.util.invoke
import miragefairy2024.util.minus
import miragefairy2024.util.plus
import miragefairy2024.util.text
import miragefairy2024.util.times
import miragefairy2024.util.toEntryIngredient
import miragefairy2024.util.toEntryStack
import miragefairy2024.util.translate
import mirrg.kotlin.helium.stripTrailingZeros
import mirrg.kotlin.hydrogen.formatAs
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.renderer.Rect2i
import net.minecraft.world.inventory.AbstractContainerMenu
import kotlin.math.roundToInt

abstract class SimpleMachineClientReiCategoryCard<R : SimpleMachineRecipe>(private val card: SimpleMachineReiCategoryCard<R>) : ClientReiCategoryCard<SimpleMachineReiCategoryCard.Display<R>>(card) {
    override fun registerDisplays(registry: DisplayRegistry) {
        registry.registerRecipeFiller(card.recipeCard.recipeClass, card.recipeCard.type) {
            SimpleMachineReiCategoryCard.Display(card, it.value)
        }
    }

    abstract val imageBound: Rectangle
    abstract val arrowPosition: Point
    abstract val durationTextPosition: Point
    private val textureId = "textures/gui/container/" * card.recipeCard.identifier * ".png"
    abstract val inputSlots: List<Point>
    abstract val outputSlots: List<Point>

    open fun createInputWidgets(offset: Point, display: SimpleMachineReiCategoryCard.Display<R>): List<Widget> {
        val inputIndices = card.getInputIndices(display.recipe)
        return inputSlots.mapIndexed { slotIndex, it ->
            val inputIndex = inputIndices.getOrNull(slotIndex)
            val inputEntry = if (inputIndex == null) null else display.inputEntries.getOrNull(inputIndex)
            Widgets.createSlot(offset + it).entries(inputEntry ?: listOf()).disableBackground().markInput()
        }
    }

    override fun createCategory() = object : DisplayCategory<SimpleMachineReiCategoryCard.Display<R>> {
        override fun getCategoryIdentifier() = card.identifier.first
        override fun getTitle() = text { card.translation() }
        override fun getIcon(): Renderer = card.recipeCard.getIcon().toEntryStack()
        override fun getDisplayWidth(display: SimpleMachineReiCategoryCard.Display<R>) = imageBound.width + 8 * 2 + 6
        override fun getDisplayHeight() = imageBound.height + 4 * 2 + 6
        override fun setupDisplay(display: SimpleMachineReiCategoryCard.Display<R>, bounds: Rectangle): List<Widget> {
            val p = bounds.location + Point(3, 3)
            val uv = Point(imageBound.x - 8, imageBound.y - 4)
            return listOf(
                Widgets.createRecipeBase(bounds),

                Widgets.createTexturedWidget(textureId, Rectangle(bounds.x + 3, bounds.y + 3, bounds.width - 6, bounds.height - 6), uv.x.toFloat(), uv.y.toFloat()),

                *createInputWidgets(p - uv, display).toTypedArray(),

                Widgets.createArrow(p + arrowPosition + Point(-1, -1) - uv).animationDurationTicks(display.recipe.duration.toDouble()),
                Widgets.createLabel(p + durationTextPosition - uv, text { translate("category.rei.campfire.time", (display.recipe.duration.toDouble() / 20.0 formatAs "%.2f").stripTrailingZeros()) }).centered().color(0xFF404040.toInt(), 0xFFBBBBBB.toInt()).noShadow(),

                *outputSlots.mapIndexed { index, it ->
                    Widgets.createSlot(p + it - uv).entries(display.outputEntries.getOrNull(index) ?: listOf()).disableBackground().markOutput()
                }.toTypedArray(),
            )
        }
    }

    override fun getWorkstations() = listOf(listOf(card.getMachine().toEntryStack()).toEntryIngredient())

    fun <C : AbstractContainerMenu, T : AbstractContainerScreen<C>> registerScreen(registry: ScreenRegistry, screenClass: Class<out T>, rectangle: Rectangle) {
        registry.registerContainerClickArea(Rectangle(rectangle.x - 1, rectangle.y - 1, rectangle.width + 2, rectangle.height + 1 + 2), screenClass, card.identifier.first)
    }
}

object FermentationBarrelClientReiCategoryCard : SimpleMachineClientReiCategoryCard<FermentationBarrelRecipe>(FermentationBarrelReiCategoryCard) {
    override val imageBound = Rectangle(30, 16, 120, 40)
    override val arrowPosition = Point(77, 28)
    override val durationTextPosition = Point(88, 15)
    override val inputSlots = listOf(Point(42, 17), Point(31, 39), Point(53, 39))
    override val outputSlots = listOf(Point(111, 28))
    override fun registerScreens(registry: ScreenRegistry) {
        registerScreen(registry, FermentationBarrelScreen::class.java, Rectangle(77, 28, 22, 15))
    }
}

object AuraReflectorFurnaceClientReiCategoryCard : SimpleMachineClientReiCategoryCard<AuraReflectorFurnaceRecipe>(AuraReflectorFurnaceReiCategoryCard) {
    override val imageBound = Rectangle(28, 16, 116, 54)
    override val arrowPosition = Point(89, 35)
    override val durationTextPosition = Point(108, 18)
    override val inputSlots = listOf(Point(29, 17), Point(47, 17), Point(65, 17))
    val fuelSlots = listOf(Point(47, 53))
    val fuelBound = Rect2i(48, 37, 13, 13)
    override val outputSlots = listOf(Point(123, 35))

    override fun createInputWidgets(offset: Point, display: SimpleMachineReiCategoryCard.Display<AuraReflectorFurnaceRecipe>): List<Widget> {
        val fuelInputIndices = AuraReflectorFurnaceReiCategoryCard.getFuelInputIndices(display.recipe)
        return super.createInputWidgets(offset, display) + fuelSlots.mapIndexed { slotIndex, it ->
            val inputIndex = fuelInputIndices.getOrNull(slotIndex)
            val inputEntry = if (inputIndex == null) null else display.inputEntries.getOrNull(inputIndex)
            Widgets.createSlot(offset + it).entries(inputEntry ?: listOf()).disableBackground().markInput()
        } + BlueFuelWidget(Rectangle(fuelBound.x + offset.x, fuelBound.y + offset.y, fuelBound.width, fuelBound.height))
    }

    override fun registerScreens(registry: ScreenRegistry) {
        registerScreen(registry, AuraReflectorFurnaceScreen::class.java, Rectangle(89, 35, 22, 15))
    }
}

class BlueFuelWidget(private val rectangle: Rectangle) : WidgetWithBounds() {
    override fun children() = listOf<GuiEventListener>()
    override fun getBounds() = rectangle
    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        val fuelMax = 20 * 10
        val fuel = fuelMax - (System.currentTimeMillis() / 50) % fuelMax - 1
        val fuelRate = fuel.toDouble() / fuelMax.toDouble()
        val h = (rectangle.height.toDouble() * fuelRate).roundToInt()
        context.blit(
            AuraReflectorFurnaceScreen.BLUE_FUEL_TEXTURE,
            rectangle.x - 1,
            rectangle.y - 1 + (rectangle.height - h),
            0F,
            rectangle.height.toFloat() - h.toFloat(),
            rectangle.width,
            h,
            32,
            32,
        )
    }
}
