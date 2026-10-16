package miragefairy2024.client.mod.fairy

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.client.mod.recipeviewer.screen.createViewToast
import miragefairy2024.client.util.registerClientPacketReceiver
import miragefairy2024.mod.fairy.GAIN_FAIRY_DREAM_TRANSLATION
import miragefairy2024.mod.fairy.GainFairyDreamChannel
import miragefairy2024.mod.fairy.createFairyItemStack
import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.views.EllipsisTextView
import miragefairy2024.mod.recipeviewer.views.NinePatchImageView
import miragefairy2024.mod.recipeviewer.views.OutputSlotView
import miragefairy2024.mod.recipeviewer.views.StackView
import miragefairy2024.mod.recipeviewer.views.TextView
import miragefairy2024.mod.recipeviewer.views.View
import miragefairy2024.mod.recipeviewer.views.XListView
import miragefairy2024.mod.recipeviewer.views.XSpaceView
import miragefairy2024.mod.recipeviewer.views.YListView
import miragefairy2024.mod.recipeviewer.views.YSpaceView
import miragefairy2024.mod.recipeviewer.views.configure
import miragefairy2024.mod.recipeviewer.views.margin
import miragefairy2024.mod.recipeviewer.views.maxContentSizeX
import miragefairy2024.mod.recipeviewer.views.minContentSizeX
import miragefairy2024.mod.recipeviewer.views.noBackground
import miragefairy2024.mod.recipeviewer.views.noMargin
import miragefairy2024.mod.recipeviewer.views.plusAssign
import miragefairy2024.util.black
import miragefairy2024.util.darkBlue
import miragefairy2024.util.invoke
import miragefairy2024.util.text
import net.minecraft.client.Minecraft

/** 妖精の夢を見たことを知らせる枠の寸法なのだ～🌱 */
private const val TOAST_WIDTH = 160
private const val TOAST_HEIGHT = 32

private const val TOAST_MARGIN_X = 8

/** アイテムの絵と、文の間隔なのだ～🌱 */
private const val ITEM_NAME_GAP = 6

/** 2個の文の間隔なのだ～🌱 */
private const val TEXT_GAP = 2

private const val ITEM_NAME_MARGIN_LEFT = 4

/** アイテムの名前へ割り当てる横幅なのだ～🌱 */
private const val ITEM_NAME_WIDTH = TOAST_WIDTH - TOAST_MARGIN_X - 16 - ITEM_NAME_GAP - 10 - TOAST_MARGIN_X

private val TOAST_TEXTURE = MirageFairy2024.identifier("textures/gui/fairy_dream_toast.png")

context(ModContext)
fun initFairyClientModule() {

    // パケットハンドラ登録
    GainFairyDreamChannel.registerClientPacketReceiver { motif ->
        val itemStack = motif.createFairyItemStack()
        val rootView = View {
            view += StackView().configure {
                view.sizingX = Sizing.FILL
                view.sizingY = Sizing.FILL

                // 枠の絵は一辺が枠と同じ1枚だけであるため、四辺を0にして中央だけを敷くのだ～🌱
                view += NinePatchImageView(TOAST_TEXTURE, 0, TOAST_WIDTH, 0, 0, TOAST_HEIGHT, 0)

                view += XListView().configure {
                    view.sizingX = Sizing.FILL
                    view.sizingY = Sizing.FILL

                    view += OutputSlotView(itemStack).noBackground().noMargin().configure { position.alignmentY = Alignment.CENTER }

                    view += XSpaceView(ITEM_NAME_GAP)

                    view += YListView().configure {
                        position.alignmentY = Alignment.CENTER

                        view += TextView(text { GAIN_FAIRY_DREAM_TRANSLATION().black }).also { it.shadow = false }

                        view += YSpaceView(TEXT_GAP)

                        view += EllipsisTextView(itemStack.hoverName.darkBlue).also { it.shadow = false }
                            .configure { }
                            .minContentSizeX(ITEM_NAME_WIDTH)
                            .maxContentSizeX(ITEM_NAME_WIDTH)
                            .margin(ITEM_NAME_MARGIN_LEFT, 0, 0, 0)
                    }
                }.margin(TOAST_MARGIN_X, 0)
            }
        }
        Minecraft.getInstance().toasts.addToast(createViewToast(IntPoint(TOAST_WIDTH, TOAST_HEIGHT), rootView))
    }

    initMotifTableClientModule()
    initSoulStreamClientModule()
    initCollectionEnabledClientModule()

}
