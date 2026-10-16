package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.sized
import miragefairy2024.util.EventRegistry
import net.minecraft.network.chat.Component

/**
 * 文字を入力させるViewなのだ～🌱
 * 入力のウィジェットそのものは、このViewを受け取ったバックエンドが用意するのだ～🌱
 */
class TextBoxView(private val sizeY: Int) : AbstractView(), PlaceableView {

    var hint: Component? = null

    var text = ""

    val onChanged = EventRegistry<(String) -> Unit>()

    override var sizingX = Sizing.FILL

    override val sizingY = Sizing.WRAP

    override fun calculateContentSize() = IntPoint(0, sizeY)

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = viewPlacer.place(this, offset.sized(actualSize))

}
