package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.util.EventRegistry
import miragefairy2024.util.register

/** Vanilla の View の描画に使う値を、描画直前に更新する処理を登録するのだ～🌱 */
class BeforeRenderView(listener: () -> Unit) : FixedWidgetView(IntPoint.ZERO) {
    val onBeforeRender = EventRegistry<() -> Unit>().also { it.register(listener) }
}
