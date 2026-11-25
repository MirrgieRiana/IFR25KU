package miragefairy2024.mod.recipeviewer.vanilla

import miragefairy2024.util.EventRegistry

/** バニラの View 画面で 1 回の表示に対応するイベントなのだ～🌱 */
class ScreenEventSource {

    /**
     * 最初の配置が完了したときに通知するのだ～🌱
     * 処理中は直接・間接を問わず同期的に画面を開閉してはいけないのだ～🌱
     */
    val onOpened = EventRegistry<() -> Unit>()

    /**
     * 最初の配置が完了した画面を外す際に、配置を破棄する前に通知するのだ～🌱
     * 処理中は直接・間接を問わず同期的に画面を開閉してはいけないのだ～🌱
     */
    val onDisposing = EventRegistry<() -> Unit>()

    val onBeforeRender = EventRegistry<() -> Unit>()

    val onTick = EventRegistry<() -> Unit>()

}
