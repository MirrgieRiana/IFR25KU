package miragefairy2024.mod.fairy

import miragefairy2024.MirageFairy2024
import miragefairy2024.clientProxy
import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.ViewTexture
import miragefairy2024.mod.recipeviewer.views.AbsoluteView
import miragefairy2024.mod.recipeviewer.views.ImageView
import miragefairy2024.mod.recipeviewer.views.ItemView
import miragefairy2024.mod.recipeviewer.views.TextView
import miragefairy2024.mod.recipeviewer.views.View
import miragefairy2024.mod.recipeviewer.views.XListView
import miragefairy2024.mod.recipeviewer.views.XSpaceView
import miragefairy2024.mod.recipeviewer.views.YListView
import miragefairy2024.mod.recipeviewer.views.YSpaceView
import miragefairy2024.mod.recipeviewer.views.configure
import miragefairy2024.mod.recipeviewer.views.margin
import miragefairy2024.mod.recipeviewer.views.plusAssign
import miragefairy2024.util.black
import miragefairy2024.util.darkBlue
import miragefairy2024.util.invoke
import miragefairy2024.util.text
import net.minecraft.world.item.ItemStack

class FairyDreamToastViewWrapper(itemStack: ItemStack) {

    val view = View {
        val imageSize = IntPoint(160, 32)
        val displayName = itemStack.hoverName.darkBlue
        val maxNameWidth = imageSize.x - 8 - 16 - 6 - 4 - 8
        val name = clientProxy!!.ellipsizeText(displayName, maxNameWidth, text { "..."().darkBlue })
        view += AbsoluteView(imageSize).configure {
            view += ImageView(ViewTexture(MirageFairy2024.identifier("textures/gui/fairy_dream_toast.png"), imageSize))
            view += XListView().configure {
                view.sizingX = Sizing.FILL
                view.sizingY = Sizing.FILL

                view += ItemView(itemStack).configure {
                    position.alignmentY = Alignment.CENTER
                    view.showDecorations = false
                }
                view += XSpaceView(6)
                view += YListView().configure {
                    position.alignmentY = Alignment.CENTER

                    view += TextView(text { GAIN_FAIRY_DREAM_TRANSLATION().black }).configure {
                        view.shadow = false
                    }
                    view += YSpaceView(2)
                    view += TextView(name).configure {
                        view.shadow = false
                    }.margin(4, 0, 0, 0)
                }
            }.margin(8, 8, 0, 0)
        }
    }

}
