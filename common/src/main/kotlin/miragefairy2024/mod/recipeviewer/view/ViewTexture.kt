package miragefairy2024.mod.recipeviewer.view

import net.minecraft.resources.ResourceLocation as Identifier // MIGRATION2612 ReplaceImport net.minecraft.resources.Identifier

data class ViewTexture(val id: Identifier, val size: IntPoint, val bounds: IntRectangle)

fun ViewTexture(id: Identifier, size: IntPoint) = ViewTexture(id, size, IntPoint.ZERO.sized(size))
