package miragefairy2024.mod.recipeviewer.vanilla

import miragefairy2024.util.EventRegistry

/**
 * バニラの View の表示を管理するホストのイベントを提供するのだ～🌱
 *
 * このイベントソースは、個々の View の配置が終わった後も、同じ画面を開き直した後も存続するのだ～🌱
 * 画面の表示中に購読するリスナーは [miragefairy2024.mod.recipeviewer.vanilla.ScreenEventSource.onDisposing] で解除しなければならないのだ～🌱
 * View の配置中に購読するリスナーは、その配置を解除する際に解除しなければならないのだ～🌱
 */
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

    /** ホストがウィジェットを描画する直前に通知するのだ～🌱 */
    val onBeforeRender = EventRegistry<() -> Unit>()

    /** ホストの tick を通知するのだ～🌱 */
    val onTick = EventRegistry<() -> Unit>()

}
