package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.ViewTexture
import miragefairy2024.mod.recipeviewer.view.size

enum class ProgressDirection {
    UP, DOWN, LEFT, RIGHT,
}

/**
 * 背景の絵の上へ、進捗の割合だけ前景の絵を重ねる View なのだ～🌱
 * 前景のどこを見せるかは、描画の側が毎フレーム [miragefairy2024.mod.recipeviewer.views.ProgressView.progress] から出すのだ～🌱
 * 寸法は背景の絵の切り取る領域から決まるのだ～🌱
 */
class ProgressView(val background: ViewTexture, val foreground: ViewTexture) : FixedWidgetView(background.bounds.size) {

    /**
     * 0 から 1 までの進捗なのだ～🌱
     * 範囲の外の値は、描画の側が範囲の中へ丸めるのだ～🌱
     */
    var progress = 0.0

    /** 進捗が伸びていく向きなのだ～🌱 */
    var direction = ProgressDirection.RIGHT

}
