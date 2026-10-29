package miragefairy2024.client.mod.fairy

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.client.util.ViewToast
import miragefairy2024.client.util.registerClientPacketReceiver
import miragefairy2024.mod.fairy.GAIN_FAIRY_DREAM_TRANSLATION
import miragefairy2024.mod.fairy.GainFairyDreamChannel
import miragefairy2024.mod.fairy.createFairyItemStack
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
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Style
import net.minecraft.util.FormattedCharSequence

context(ModContext)
fun initFairyClientModule() {

    // パケットハンドラ登録
    GainFairyDreamChannel.registerClientPacketReceiver { motif ->
        val imageSize = IntPoint(160, 32)
        val itemStack = motif.createFairyItemStack()
        val font = Minecraft.getInstance().font
        val lines = font.split(itemStack.hoverName.darkBlue, imageSize.x - 8 - 16 - 6 - 10 - 8)
        val firstLine = lines.firstOrNull() ?: FormattedCharSequence.EMPTY
        val name = if (lines.size >= 2) {
            var style: Style? = null
            lines[1].accept { _, style2, _ ->
                style = style2
                false
            }
            val ellipsisStyle = style
            if (ellipsisStyle != null) FormattedCharSequence.composite(firstLine, FormattedCharSequence.forward("...", ellipsisStyle)) else firstLine
        } else {
            firstLine
        }
        val rootView = View {
            view += AbsoluteView(imageSize).configure {
                view += ImageView(ViewTexture(MirageFairy2024.identifier("textures/gui/fairy_dream_toast.png"), imageSize))
                view += XListView().configure {
                    view.sizingX = Sizing.FILL
                    view.sizingY = Sizing.FILL

                    view += ItemView(itemStack).configure {
                        position.alignmentY = Alignment.CENTER
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
        Minecraft.getInstance().toasts.addToast(ViewToast(rootView))
    }

    initMotifTableClientModule()
    initSoulStreamClientModule()
    initCollectionEnabledClientModule()

}
