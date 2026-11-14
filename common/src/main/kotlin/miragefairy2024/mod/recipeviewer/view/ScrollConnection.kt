package miragefairy2024.mod.recipeviewer.view

import miragefairy2024.mod.recipeviewer.views.ScrollbarView
import miragefairy2024.mod.recipeviewer.views.SimpleScrollView
import miragefairy2024.util.ObservableValue
import miragefairy2024.util.Remover
import miragefairy2024.util.plus
import miragefairy2024.util.register

/** 表示領域の範囲をバーへ同期し、指定値を丸めず双方向同期する接続の [miragefairy2024.util.Remover] を返すのだ～🌱 */
fun connectScrollbar(scrollView: SimpleScrollView, scrollOffsetValue: ObservableValue<Double>, scrollbarView: ScrollbarView, scrollbarOffsetValue: ObservableValue<Double>): Remover {
    var updating = false
    var pending = false
    fun updateScrollbar() {
        if (updating) {
            pending = true
            return
        }
        updating = true
        try {
            do {
                pending = false
                val bounds = scrollView.scrollBounds.value
                scrollbarView.contentLength = bounds.contentLength
                scrollbarView.viewportLength = bounds.viewportLength
                scrollbarOffsetValue.value = scrollOffsetValue.value
            } while (pending)
        } finally {
            updating = false
        }
    }
    val boundsRemover = scrollView.scrollBounds.register { _, _ ->
        updateScrollbar()
    }
    val sourceRemover = scrollOffsetValue.register { _, _ ->
        updateScrollbar()
    }
    val targetRemover = scrollbarOffsetValue.register { _, _ ->
        if (!updating) {
            scrollOffsetValue.value = scrollbarOffsetValue.value
            updateScrollbar()
        }
    }
    val remover = boundsRemover + sourceRemover + targetRemover
    try {
        updateScrollbar()
    } catch (e: Throwable) {
        remover.remove()
        throw e
    }
    return remover
}
