package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.util.EventRegistry
import miragefairy2024.util.register

/** 画面の tick を受け取り、登録した処理を呼び出すのだ～🌱 */
class TickView : FixedWidgetView(IntPoint.ZERO) {
    val onTick = EventRegistry<() -> Unit>()
}

fun TickView(listener: () -> Unit): TickView {
    return TickView().also {
        it.onTick.register(listener)
    }
}
