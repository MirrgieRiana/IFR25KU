package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.evaluateActualSize
import miragefairy2024.mod.recipeviewer.view.sized
import miragefairy2024.util.ObservableValue
import net.minecraft.network.chat.Component

class TextFieldView : AbstractView(), PlaceableView {
    override var sizingX = Sizing.FILL
    override var sizingY = Sizing.WRAP
    val text = ObservableValue("")
    var hint: Component? = null
    override fun evaluateContentSizeXImpl() = 0
    override fun evaluateContentSizeYImpl() = 20
    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = viewPlacer.place(this, offset.sized(evaluateActualSize()))
}
