package miragefairy2024.mod.recipeviewer.view

import net.minecraft.resources.ResourceLocation

data class ViewTexture(val id: ResourceLocation, val size: IntPoint, val bounds: IntRectangle) {
    init {
        require(size.x > 0 && size.y > 0) { "Texture dimensions must be positive" }
        require(bounds.sizeX >= 0 && bounds.sizeY >= 0) { "Texture region dimensions must be non-negative" }
    }
}

fun ViewTexture(id: ResourceLocation, size: IntPoint): ViewTexture {
    require(size.x > 0 && size.y > 0) { "Texture dimensions must be positive" }
    return ViewTexture(id, size, IntPoint.ZERO.sized(size))
}
