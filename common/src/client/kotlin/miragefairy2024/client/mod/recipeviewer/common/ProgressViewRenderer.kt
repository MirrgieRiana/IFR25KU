package miragefairy2024.client.mod.recipeviewer.common

import miragefairy2024.client.mod.recipeviewer.ViewRenderer
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.ViewTexture
import miragefairy2024.mod.recipeviewer.view.x2
import miragefairy2024.mod.recipeviewer.view.y2
import miragefairy2024.mod.recipeviewer.views.ProgressDirection
import miragefairy2024.mod.recipeviewer.views.ProgressView
import net.minecraft.client.gui.GuiGraphics
import kotlin.math.roundToInt

object ProgressViewRenderer : ViewRenderer<ProgressView> {
    override fun render(view: ProgressView, bounds: IntRectangle, graphics: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        fun blit(texture: ViewTexture, x: Int, y: Int, source: IntRectangle) {
            graphics.blit(
                texture.id,
                x,
                y,
                source.x.toFloat(),
                source.y.toFloat(),
                source.sizeX,
                source.sizeY,
                texture.size.x,
                texture.size.y,
            )
        }

        blit(view.background, bounds.x, bounds.y, view.background.bounds)

        val progress = view.progress.coerceIn(0.0, 1.0)
        val source = view.foreground.bounds
        val visibleSizeX = if (view.direction == ProgressDirection.RIGHT || view.direction == ProgressDirection.LEFT) (source.sizeX * progress).roundToInt() else source.sizeX
        val visibleSizeY = if (view.direction == ProgressDirection.DOWN || view.direction == ProgressDirection.UP) (source.sizeY * progress).roundToInt() else source.sizeY
        if (visibleSizeX <= 0 || visibleSizeY <= 0) return

        // 伸びていく向きの反対側の端を固定するのだ～🌱
        val visibleX = if (view.direction == ProgressDirection.LEFT) source.x2 - visibleSizeX else source.x
        val visibleY = if (view.direction == ProgressDirection.UP) source.y2 - visibleSizeY else source.y
        blit(view.foreground, bounds.x + (visibleX - source.x), bounds.y + (visibleY - source.y), IntRectangle(visibleX, visibleY, visibleSizeX, visibleSizeY))
    }
}
