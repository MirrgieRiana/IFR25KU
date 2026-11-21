package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.evaluateActualSize
import miragefairy2024.mod.recipeviewer.view.plus
import miragefairy2024.mod.recipeviewer.view.sized
import miragefairy2024.util.EventRegistry
import miragefairy2024.util.register

class ClickableView : WrapperView(), PlaceableView {
    val onClick = EventRegistry<() -> Boolean>()
    var hoverOverlayColor = 0x80FFFFFF.toInt()

    /** `null` の場合は、すべてのマウスボタンでクリックを受け付けるのだ～🌱 */
    var mouseButton: Int? = 0
    var playClickSound = true

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): ViewController {
        return super.attachTo(offset, viewPlacer) + viewPlacer.place(this, offset.sized(evaluateActualSize()))
    }
}

fun <P> Child<P, *>.wrapClickable(block: Child<P, ClickableView>.() -> Unit) = this.wrap(ClickableView()).also { block(it) }

fun <P> Child<P, *>.onClick(listener: () -> Boolean) = this.wrapClickable { view.onClick.register(listener) }
