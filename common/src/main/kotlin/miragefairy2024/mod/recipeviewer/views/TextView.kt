package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.ColorPair
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.sized
import miragefairy2024.util.ObservableValue
import mirrg.kotlin.helium.atLeast
import mirrg.kotlin.helium.atMost
import net.minecraft.network.chat.Component
import net.minecraft.util.FormattedCharSequence

class TextView : AbstractView(), PlaceableView {
    val text = ObservableValue(FormattedCharSequence.EMPTY) // TODO サイズ再計算
    var minWidth = 0
    var color: ColorPair? = null
    var shadow = true
    var alignmentX = Alignment.START
    var tooltip: List<Component>? = null
    override fun calculateMinSizeImpl() = IntPoint(minWidth, rendererProxy.getTextHeight())
    override fun calculateSizeImpl(regionSize: IntPoint) = IntPoint(rendererProxy.calculateTextWidth(text.value) atMost regionSize.x atLeast minWidth, calculatedMinSize.y)
    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = viewPlacer.place(this, offset.sized(calculatedSize))
}

fun TextView(text: FormattedCharSequence) = TextView().also { it.text.value = text }
fun TextView(text: Component) = TextView(text.visualOrderText)
