package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntRectangle
import net.minecraft.resources.ResourceLocation

class ImageView(val textureId: ResourceLocation, val bound: IntRectangle) : FixedWidgetView(bound.sizeX, bound.sizeY)
