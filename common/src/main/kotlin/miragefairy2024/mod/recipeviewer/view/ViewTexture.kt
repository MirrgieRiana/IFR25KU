package miragefairy2024.mod.recipeviewer.view

import net.minecraft.resources.ResourceLocation

data class ViewTexture(val id: ResourceLocation, val size: IntPoint, val bounds: IntRectangle)

fun ViewTexture(id: ResourceLocation, size: IntPoint) = ViewTexture(id, size, IntPoint.ZERO.sized(size))
