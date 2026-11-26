package miragefairy2024.mod.recipeviewer.vanilla

import miragefairy2024.util.EventRegistry

/**
 * バニラの View 画面のイベントを提供するのだ～🌱
 *
 * このイベントソースは、個々の View の配置が終わった後も、同じ画面を開き直した後も存続するのだ～🌱
 * 画面の表示期間に紐づく購読は [miragefairy2024.mod.recipeviewer.vanilla.ScreenEventSource.onDisposing] で解除しなければならないのだ～🌱
 * View の配置期間に紐づく購読は、その配置を破棄する際に解除しなければならないのだ～🌱
 */
class ScreenEventSource {

    /**
     * 最初の配置が完了したときに通知するのだ～🌱
     * このイベントの処理中に、このイベントへリスナーを登録する操作の挙動は保証しないのだ～🌱
     * [miragefairy2024.mod.recipeviewer.vanilla.ScreenEventSource.onOpened] または [miragefairy2024.mod.recipeviewer.vanilla.ScreenEventSource.onDisposing] の処理中に、いずれかのイベントを同期的に発火させる操作の挙動は保証しないのだ～🌱
     */
    val onOpened = EventRegistry<() -> Unit>()

    /**
     * 最初の配置が完了した画面を外す際に、配置を破棄する前に通知するのだ～🌱
     * このイベントの処理中に、このイベントへリスナーを登録する操作の挙動は保証しないのだ～🌱
     * [miragefairy2024.mod.recipeviewer.vanilla.ScreenEventSource.onOpened] または [miragefairy2024.mod.recipeviewer.vanilla.ScreenEventSource.onDisposing] の処理中に、いずれかのイベントを同期的に発火させる操作の挙動は保証しないのだ～🌱
     * この通知中に、通知元の画面の終了処理へ同期的に再入した場合の挙動は保証しないのだ～🌱
     */
    val onDisposing = EventRegistry<() -> Unit>()

    /** ホストがウィジェットを描画する直前に通知するのだ～🌱 */
    val onBeforeRender = EventRegistry<() -> Unit>()

    /** ホストの tick を通知するのだ～🌱 */
    val onTick = EventRegistry<() -> Unit>()

}
