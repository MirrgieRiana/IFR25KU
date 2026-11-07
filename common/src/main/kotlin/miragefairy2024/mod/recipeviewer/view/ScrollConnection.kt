package miragefairy2024.mod.recipeviewer.view

import miragefairy2024.mod.recipeviewer.views.ScrollView
import miragefairy2024.mod.recipeviewer.views.ScrollbarView
import miragefairy2024.util.Remover
import miragefairy2024.util.plus
import miragefairy2024.util.register

/** 表示領域の範囲をバーへ同期し、現在値を双方向同期する接続の [miragefairy2024.util.Remover] を返すのだ～🌱 */
fun connectScrollbar(scrollView: ScrollView, scrollbarView: ScrollbarView): Remover {
    val source = scrollView.scrollState
    val target = scrollbarView.scrollState
    var updating = false
    fun updateScrollbar() {
        if (updating) return
        updating = true
        try {
            target.setBounds(source.contentLength, source.viewportLength)
            target.value = source.value
        } finally {
            updating = false
        }
    }
    updateScrollbar()
    val sourceRemover = source.onChange.register { updateScrollbar() }
    val targetRemover = target.onChange.register {
        if (!updating) {
            source.value = target.value
            updateScrollbar()
        }
    }
    return sourceRemover + targetRemover
}
