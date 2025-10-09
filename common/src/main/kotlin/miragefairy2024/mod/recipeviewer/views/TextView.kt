package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.ColorPair
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.sized
import miragefairy2024.util.ObservableValue
import net.minecraft.network.chat.Component
import net.minecraft.util.FormattedCharSequence

class TextView : AbstractView(), PlaceableView {
    val text = ObservableValue(FormattedCharSequence.EMPTY) // TODO サイズ再計算
    var sizingX: Sizing = Sizing.Wrap
    var color = ColorPair.DEFAULT
    var shadow = true
    var alignmentX = Alignment.START
    var scroll = false
    var tooltip: List<Component>? = null
    override fun calculateMinSizeImpl() = IntPoint(sizingX.getMinSize(rendererProxy.calculateTextWidth(text.value)), rendererProxy.getTextHeight())
    override fun calculateSizeImpl(regionSize: IntPoint) = IntPoint(sizingX.getSize(rendererProxy.calculateTextWidth(text.value), regionSize.x), calculatedMinSize.y)
    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = viewPlacer.place(this, offset.sized(calculatedSize))
}

fun TextView(text: FormattedCharSequence) = TextView().also { it.text.value = text }
fun TextView(text: Component) = TextView(text.visualOrderText)
