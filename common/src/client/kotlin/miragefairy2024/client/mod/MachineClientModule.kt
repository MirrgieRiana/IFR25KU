package miragefairy2024.client.mod

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.client.lib.MachineScreen
import miragefairy2024.client.mod.recipeviewer.ScreenClassRegistry
import miragefairy2024.client.mod.recipeviewer.ViewRenderer
import miragefairy2024.client.mod.recipeviewer.ViewRendererRegistry
import miragefairy2024.client.util.registerHandledScreen
import miragefairy2024.mod.machine.AuraReflectorFurnaceCard
import miragefairy2024.mod.machine.AuraReflectorFurnaceScreenHandler
import miragefairy2024.mod.machine.BlueFuelView
import miragefairy2024.mod.machine.FermentationBarrelCard
import miragefairy2024.mod.machine.FermentationBarrelScreenHandler
import miragefairy2024.mod.machine.SimpleMachineCard
import miragefairy2024.mod.machine.SimpleMachineScreenHandler
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.util.invoke
import miragefairy2024.util.text
import mirrg.kotlin.helium.atMost
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.Rect2i
import java.util.Optional
import kotlin.math.roundToInt

context(ModContext)
fun initMachineClientModule() {
    FermentationBarrelCard.screenHandlerType.registerHandledScreen { gui, inventory, title -> FermentationBarrelScreen(FermentationBarrelCard, MachineScreen.Arguments(gui, inventory, title)) }
    AuraReflectorFurnaceCard.screenHandlerType.registerHandledScreen { gui, inventory, title -> AuraReflectorFurnaceScreen(AuraReflectorFurnaceCard, MachineScreen.Arguments(gui, inventory, title)) }

    ScreenClassRegistry.register(FermentationBarrelCard.screenHandlerType.key, FermentationBarrelScreen::class.java)
    ScreenClassRegistry.register(AuraReflectorFurnaceCard.screenHandlerType.key, AuraReflectorFurnaceScreen::class.java)

    ViewRendererRegistry.register(BlueFuelView::class.java, BlueFuelViewRenderer)
}

object BlueFuelViewRenderer : ViewRenderer<BlueFuelView> {
    override fun render(view: BlueFuelView, bounds: IntRectangle, graphics: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        val fuelMax = 20 * 10
        val fuel = fuelMax - (System.currentTimeMillis() / 50) % fuelMax - 1
        val fuelRate = fuel.toDouble() / fuelMax.toDouble()
        val h = (bounds.sizeY.toDouble() * fuelRate).roundToInt()
        graphics.blit(
            AuraReflectorFurnaceScreen.BLUE_FUEL_TEXTURE,
            bounds.x - 1,
            bounds.y - 1 + (bounds.sizeY - h),
            0F,
            bounds.sizeY.toFloat() - h.toFloat(),
            bounds.sizeX,
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
