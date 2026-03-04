package miragefairy2024.client.mod

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.client.lib.MachineScreen
import miragefairy2024.client.mod.recipeviewer.ScreenClassRegistry
import miragefairy2024.client.mod.recipeviewer.ViewRenderer
import miragefairy2024.client.mod.recipeviewer.ViewRendererRegistry
import miragefairy2024.client.util.registerHandledScreen
import miragefairy2024.mod.machine.AthanorCard
import miragefairy2024.mod.machine.AthanorScreenHandler
import miragefairy2024.mod.machine.AuraReflectorFurnaceCard
import miragefairy2024.mod.machine.AuraReflectorFurnaceScreenHandler
import miragefairy2024.mod.machine.BlueFuelView
import miragefairy2024.mod.machine.FermentationBarrelCard
import miragefairy2024.mod.machine.FermentationBarrelScreenHandler
import miragefairy2024.mod.machine.FuelView
import miragefairy2024.mod.machine.SimpleMachineCard
import miragefairy2024.mod.machine.SimpleMachineScreenHandler
import miragefairy2024.mod.machine.TexturedArrowView
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.util.invoke
import miragefairy2024.util.text
import mirrg.kotlin.helium.atMost
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.Rect2i
import net.minecraft.resources.ResourceLocation
import java.util.Optional
import kotlin.math.roundToInt

context(ModContext)
fun initMachineClientModule() {
    FermentationBarrelCard.screenHandlerType.registerHandledScreen { gui, inventory, title -> FermentationBarrelScreen(FermentationBarrelCard, MachineScreen.Arguments(gui, inventory, title)) }
    AuraReflectorFurnaceCard.screenHandlerType.registerHandledScreen { gui, inventory, title -> AuraReflectorFurnaceScreen(AuraReflectorFurnaceCard, MachineScreen.Arguments(gui, inventory, title)) }
    AthanorCard.screenHandlerType.registerHandledScreen { gui, inventory, title -> AthanorScreen(AthanorCard, MachineScreen.Arguments(gui, inventory, title)) }

    ScreenClassRegistry.register(FermentationBarrelCard.screenHandlerType.key, FermentationBarrelScreen::class.java)
    ScreenClassRegistry.register(AuraReflectorFurnaceCard.screenHandlerType.key, AuraReflectorFurnaceScreen::class.java)
    ScreenClassRegistry.register(AthanorCard.screenHandlerType.key, AthanorScreen::class.java)

    ViewRendererRegistry.register(FuelView::class.java, FuelViewRenderer)
    ViewRendererRegistry.register(BlueFuelView::class.java, BlueFuelViewRenderer)
    ViewRendererRegistry.register(TexturedArrowView::class.java, TexturedArrowViewRenderer)
}

object FuelViewRenderer : ViewRenderer<FuelView> {
    val TEXTURE = MirageFairy2024.identifier("textures/gui/sprites/fuel.png")
    override fun render(view: FuelView, bounds: IntRectangle, graphics: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        val fuelMax = 20 * 10
        val fuel = fuelMax - (System.currentTimeMillis() / 50) % fuelMax - 1
        val fuelRate = fuel.toDouble() / fuelMax.toDouble()
        val h = (bounds.sizeY.toDouble() * fuelRate).roundToInt()
        graphics.blit(
            TEXTURE,
            bounds.x,
            bounds.y,
            13F,
            0F,
            bounds.sizeX,
            bounds.sizeY,
            32,
            32,
        )
        graphics.blit(
            TEXTURE,
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

object BlueFuelViewRenderer : ViewRenderer<BlueFuelView> {
    val TEXTURE = MirageFairy2024.identifier("textures/gui/sprites/blue_fuel.png")
    override fun render(view: BlueFuelView, bounds: IntRectangle, graphics: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        val fuelMax = 20 * 10
        val fuel = fuelMax - (System.currentTimeMillis() / 50) % fuelMax - 1
        val fuelRate = fuel.toDouble() / fuelMax.toDouble()
        val h = (bounds.sizeY.toDouble() * fuelRate).roundToInt()
        graphics.blit(
            TEXTURE,
            bounds.x,
            bounds.y,
            13F,
            0F,
            bounds.sizeX,
            bounds.sizeY,
            32,
            32,
        )
        graphics.blit(
            TEXTURE,
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

object TexturedArrowViewRenderer : ViewRenderer<TexturedArrowView> {
    override fun render(view: TexturedArrowView, bounds: IntRectangle, graphics: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        val rate = view.durationMilliSeconds?.let {
            (System.nanoTime() / 1_000_000 % it.toLong()).toDouble() / it.toDouble()
        } ?: 1.0
        view.backgroundTexture?.let {
            graphics.blit(
                it.id,
                bounds.x,
                bounds.y,
                bounds.sizeX,
                bounds.sizeY,
                it.bounds.x.toFloat(),
                it.bounds.y.toFloat(),
                it.bounds.sizeX,
                it.bounds.sizeY,
                it.size.x,
                it.size.y,
            )
        }
        view.foregroundTexture?.let {
            graphics.blit(
                it.id,
                bounds.x,
                bounds.y,
                (bounds.sizeX.toDouble() * rate).roundToInt(),
                bounds.sizeY,
                it.bounds.x.toFloat(),
                it.bounds.y.toFloat(),
                (it.bounds.sizeX.toDouble() * rate).roundToInt(),
                it.bounds.sizeY,
                it.size.x,
                it.size.y,
            )
        }
    }
}

abstract class SimpleMachineScreen<H : SimpleMachineScreenHandler>(card: SimpleMachineCard<*, *, *, *>, arguments: Arguments<H>) : MachineScreen<H>(card, arguments) {
    companion object {
        val PROGRESS_ARROW_TEXTURE = MirageFairy2024.identifier("textures/gui/sprites/progress.png")
    }

    abstract val arrowTexture: ResourceLocation
    abstract val arrowBound: Rect2i

    override fun renderBg(context: GuiGraphics, delta: Float, mouseX: Int, mouseY: Int) {
        super.renderBg(context, delta, mouseX, mouseY)

        if (menu.progressMax > 0) {
            val w = (arrowBound.width.toDouble() * (menu.progress.toDouble() / menu.progressMax.toDouble() atMost 1.0)).roundToInt()
            context.blit(
                arrowTexture,
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
    override val arrowTexture = PROGRESS_ARROW_TEXTURE
    override val arrowBound = Rect2i(76, 27, 24, 17)
}

class AuraReflectorFurnaceScreen(card: AuraReflectorFurnaceCard, arguments: Arguments<AuraReflectorFurnaceScreenHandler>) : SimpleMachineScreen<AuraReflectorFurnaceScreenHandler>(card, arguments) {
    override val arrowTexture = PROGRESS_ARROW_TEXTURE
    override val arrowBound = Rect2i(88, 34, 24, 17)
    val fuelBound = Rect2i(48, 37, 13, 13)

    override fun renderBg(context: GuiGraphics, delta: Float, mouseX: Int, mouseY: Int) {
        super.renderBg(context, delta, mouseX, mouseY)

        if (menu.fuelMax > 0) {
            val h = (fuelBound.height.toDouble() * (menu.fuel.toDouble() / menu.fuelMax.toDouble() atMost 1.0)).roundToInt()
            context.blit(
                BlueFuelViewRenderer.TEXTURE,
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

class AthanorScreen(card: AthanorCard, arguments: Arguments<AthanorScreenHandler>) : SimpleMachineScreen<AthanorScreenHandler>(card, arguments) {
    override val arrowTexture = MirageFairy2024.identifier("textures/gui/sprites/athanor_progress.png")
    override val arrowBound = Rect2i(84, 39, 24, 16)
    val fuelBound = Rect2i(90, 64, 13, 13)

    override fun renderBg(context: GuiGraphics, delta: Float, mouseX: Int, mouseY: Int) {
        super.renderBg(context, delta, mouseX, mouseY)

        if (menu.fuelMax > 0) {
            val h = (fuelBound.height.toDouble() * (menu.fuel.toDouble() / menu.fuelMax.toDouble() atMost 1.0)).roundToInt()
            context.blit(
                FuelViewRenderer.TEXTURE,
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
