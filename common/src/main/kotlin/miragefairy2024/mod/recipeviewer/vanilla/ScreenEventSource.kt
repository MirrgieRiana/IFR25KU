package miragefairy2024.mod.recipeviewer.vanilla

import miragefairy2024.util.EventRegistry

/**
 * バニラの View 画面で 1 回の表示に対応するイベントなのだ～🌱
 *
 * このイベントソースは、個々の View の配置が終わった後も画面の終了まで存続するのだ～🌱
 * 購読側は、登録したすべてのリスナーを [miragefairy2024.mod.recipeviewer.vanilla.ScreenEventSource.onDisposing] で解除しなければならないのだ～🌱
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

    val onBeforeRender = EventRegistry<() -> Unit>()

    val onTick = EventRegistry<() -> Unit>()

}
