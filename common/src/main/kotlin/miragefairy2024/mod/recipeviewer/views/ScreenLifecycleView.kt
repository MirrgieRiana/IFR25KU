package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.util.EventRegistry

/** 開閉の両方を受け取るには、最初の配置時と画面の終了時に同じ View がツリーに含まれている必要があるのだ～🌱 */
class ScreenLifecycleView : FixedWidgetView(IntPoint.ZERO) {
    /** 画面を開いて最初の配置が完了したときに通知するのだ～🌱 */
    val onOpened = EventRegistry<() -> Unit>()

    /** このイベントの処理中は、直接・間接を問わず、画面を同期的に開いたり閉じたりしてはいけないのだ～🌱 */
    val onDisposing = EventRegistry<() -> Unit>()
}
