package miragefairy2024.client.mod

import dev.emi.emi.api.widget.Bounds
import dev.emi.emi.api.widget.Widget
import me.shedaniel.math.Rectangle
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.client.lib.MachineScreen
import miragefairy2024.client.mod.recipeviewer.EMI_VIEW_PLACER_REGISTRY
import miragefairy2024.client.mod.recipeviewer.REI_VIEW_PLACER_REGISTRY
import miragefairy2024.client.mod.recipeviewer.ScreenClassRegistry
import miragefairy2024.client.util.registerHandledScreen
import miragefairy2024.mod.machine.AuraReflectorFurnaceCard
import miragefairy2024.mod.machine.AuraReflectorFurnaceScreenHandler
import miragefairy2024.mod.machine.BlueFuelView
import miragefairy2024.mod.machine.FermentationBarrelCard
import miragefairy2024.mod.machine.FermentationBarrelScreenHandler
import miragefairy2024.mod.machine.SimpleMachineCard
import miragefairy2024.mod.machine.SimpleMachineScreenHandler
import miragefairy2024.mod.recipeviewer.register
import miragefairy2024.util.invoke
import miragefairy2024.util.text
import mirrg.kotlin.helium.atMost
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.renderer.Rect2i
import java.util.Optional
import kotlin.math.roundToInt

context(ModContext)
fun initMachineClientModule() {
    FermentationBarrelCard.screenHandlerType.registerHandledScreen { gui, inventory, title -> FermentationBarrelScreen(FermentationBarrelCard, MachineScreen.Arguments(gui, inventory, title)) }
    AuraReflectorFurnaceCard.screenHandlerType.registerHandledScreen { gui, inventory, title -> AuraReflectorFurnaceScreen(AuraReflectorFurnaceCard, MachineScreen.Arguments(gui, inventory, title)) }

    ScreenClassRegistry.register(FermentationBarrelCard.screenHandlerType.key, FermentationBarrelScreen::class.java)
    ScreenClassRegistry.register(AuraReflectorFurnaceCard.screenHandlerType.key, AuraReflectorFurnaceScreen::class.java)

    REI_VIEW_PLACER_REGISTRY.register { widgets, view: BlueFuelView, x, y ->
        widgets += BlueFuelReiWidget(x, y)
    }
    EMI_VIEW_PLACER_REGISTRY.register { (widgets, _), view: BlueFuelView, x, y ->
        widgets.add(BlueFuelEmiWidget(x, y))
    }
}

class BlueFuelReiWidget(x: Int, y: Int) : WidgetWithBounds() {
    private val rectangle = Rectangle(x, y, 13, 13)
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

class BlueFuelEmiWidget(x: Int, y: Int) : Widget() {
    private val rectangle = Bounds(x, y, 13, 13)
    override fun getBounds() = rectangle
    override fun render(draw: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        val fuelMax = 20 * 10
        val fuel = fuelMax - (System.currentTimeMillis() / 50) % fuelMax - 1
        val fuelRate = fuel.toDouble() / fuelMax.toDouble()
        val h = (rectangle.height.toDouble() * fuelRate).roundToInt()
        draw.blit(
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

abstract class SimpleMachineScreen<H : SimpleMachineScreenHandler>(card: SimpleMachineCard<*, *, *, *>, arguments: Arguments<H>) : MachineScreen<H>(card, arguments) {
    companion object {
        val PROGRESS_ARROW_TEXTURE = MirageFairy2024.identifier("textures/gui/sprites/progress.png")
    }

    abstract val arrowBound: Rect2i

    override fun renderBg(context: GuiGraphics, delta: Float, mouseX: Int, mouseY: Int) {
        super.renderBg(context, delta, mouseX, mouseY)

        if (menu.progressMax > 0) {
            val w = (arrowBound.width.toDouble() * (menu.progress.toDouble() / menu.progressMax.toDouble() atMost 1.0)).roundToInt()
            context.blit(
                PROGRESS_ARROW_TEXTURE,
                leftPos + arrowBound.x,
                topPos + arrowBound.y,
                0F,
                0F,
                w,
                arrowBound.height,
                32,
                32,
            )
        }
    }

    override fun renderTooltip(context: GuiGraphics, x: Int, y: Int) {
        super.renderTooltip(context, x, y)
        run {
            val bound = Rect2i(
                this.leftPos + arrowBound.x,
                this.topPos + arrowBound.y,
                arrowBound.width - 1,
                arrowBound.height - 1,
            )
            if (bound.contains(x, y)) {
                context.renderTooltip(font, listOf(text { "${menu.progress} / ${menu.progressMax}"() }), Optional.empty(), x, y + 17)
            }
        }
    }
}

class FermentationBarrelScreen(card: FermentationBarrelCard, arguments: Arguments<FermentationBarrelScreenHandler>) : SimpleMachineScreen<FermentationBarrelScreenHandler>(card, arguments) {
    override val arrowBound = Rect2i(76, 27, 24, 17)
}

class AuraReflectorFurnaceScreen(card: AuraReflectorFurnaceCard, arguments: Arguments<AuraReflectorFurnaceScreenHandler>) : SimpleMachineScreen<AuraReflectorFurnaceScreenHandler>(card, arguments) {
    companion object {
        val BLUE_FUEL_TEXTURE = MirageFairy2024.identifier("textures/gui/sprites/blue_fuel.png")
    }

    override val arrowBound = Rect2i(88, 34, 24, 17)
    val fuelBound = Rect2i(48, 37, 13, 13)

    override fun renderBg(context: GuiGraphics, delta: Float, mouseX: Int, mouseY: Int) {
        super.renderBg(context, delta, mouseX, mouseY)

        if (menu.fuelMax > 0) {
            val h = (fuelBound.height.toDouble() * (menu.fuel.toDouble() / menu.fuelMax.toDouble() atMost 1.0)).roundToInt()
            context.blit(
                BLUE_FUEL_TEXTURE,
                leftPos + fuelBound.x - 1,
                topPos + fuelBound.y - 1 + (fuelBound.height - h),
                0F,
                fuelBound.height.toFloat() - h.toFloat(),
                fuelBound.width,
                h,
                32,
                32,
            )
        }
    }
}
