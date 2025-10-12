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
    override var sizingX = Sizing.WRAP
    override val sizingY = Sizing.WRAP
    var color = ColorPair.DEFAULT
    var shadow = true
    var alignmentX = Alignment.START
    var scroll = false
    var tooltip: List<Component>? = null
    override fun calculateContentSize() = IntPoint(renderingProxy.calculateTextWidth(text.value), renderingProxy.getTextHeight())
    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = viewPlacer.place(this, offset.sized(actualSize))
}

fun TextView(text: FormattedCharSequence) = TextView().also { it.text.value = text }
fun TextView(text: Component) = TextView(text.visualOrderText)
