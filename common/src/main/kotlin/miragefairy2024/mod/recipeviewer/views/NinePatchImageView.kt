package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.evaluateActualSize
import miragefairy2024.mod.recipeviewer.view.sized
import net.minecraft.resources.ResourceLocation

/**
 * @param repeatX 上下の辺と中央を、true なら X 軸方向に反復描画し、false なら伸長描画するのだ～🌱
 * @param repeatY 左右の辺と中央を、true なら Y 軸方向に反復描画し、false なら伸長描画するのだ～🌱
 */
class NinePatchImageView(
    val textureId: ResourceLocation,
    val textureWidth: Int,
    val textureHeight: Int,
    val xStartSize: Int,
    val xMiddleSize: Int,
    val xEndSize: Int,
    val yStartSize: Int,
    val yMiddleSize: Int,
    val yEndSize: Int,
    val repeatX: Boolean,
    val repeatY: Boolean,
) : AbstractView(), PlaceableView {
    override var sizingX = Sizing.FILL
    override var sizingY = Sizing.FILL
    override fun evaluateContentSizeXImpl() = 0
    override fun evaluateContentSizeYImpl() = 0
    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = viewPlacer.place(this, offset.sized(evaluateActualSize()))
}
