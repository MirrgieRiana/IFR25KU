package miragefairy2024.client.mod.fairy

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.client.util.ViewToast
import miragefairy2024.client.util.registerClientPacketReceiver
import miragefairy2024.mod.fairy.GAIN_FAIRY_DREAM_TRANSLATION
import miragefairy2024.mod.fairy.GainFairyDreamChannel
import miragefairy2024.mod.fairy.createFairyItemStack
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.ViewTexture
import miragefairy2024.mod.recipeviewer.views.AbsoluteView
import miragefairy2024.mod.recipeviewer.views.Child
import miragefairy2024.mod.recipeviewer.views.ImageView
import miragefairy2024.mod.recipeviewer.views.ItemView
import miragefairy2024.mod.recipeviewer.views.TextView
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
        val itemStack = motif.createFairyItemStack()
        val font = Minecraft.getInstance().font
        val lines = font.split(itemStack.hoverName.darkBlue, 160 - 8 - 16 - 6 - 10 - 8)
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
        val textX = 8 + 16 + 6
        val textY = (32 - (font.lineHeight * 2 + 2)) / 2
        val view = AbsoluteView(IntPoint(160, 32)).also {
            it += ImageView(ViewTexture(MirageFairy2024.identifier("textures/gui/fairy_dream_toast.png"), IntPoint(160, 32), IntRectangle(0, 0, 160, 32)))
            it += Child(AbsoluteView.Offset(IntPoint(8, (32 - 16) / 2)), ItemView(itemStack))
            it += Child(AbsoluteView.Offset(IntPoint(textX, textY)), TextView(text { GAIN_FAIRY_DREAM_TRANSLATION().black }).also { label -> label.shadow = false })
            it += Child(AbsoluteView.Offset(IntPoint(textX + 4, textY + font.lineHeight + 2)), TextView(name).also { label -> label.shadow = false })
        }
        Minecraft.getInstance().toasts.addToast(ViewToast(view))
    }

    initMotifTableClientModule()
    initSoulStreamClientModule()
    initCollectionEnabledClientModule()

}
