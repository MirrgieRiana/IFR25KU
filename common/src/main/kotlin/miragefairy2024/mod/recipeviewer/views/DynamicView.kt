package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.util.ObservableValue
import miragefairy2024.util.register

/**
 * [content] の変更に合わせて、割り当て領域内の子 View を差し替えるのだ～🌱
 * コンテンツの最小サイズは縦横とも `0` とし、子のサイズ変更を親のレイアウトへ伝播しないのだ～🌱
 *
 * @param content 表示する子 View と、その変更の通知なのだ～🌱
 */
class DynamicView(val content: ObservableValue<View>) : AbstractView() {

    override var sizingX = Sizing.FILL
    override var sizingY = Sizing.FILL

    override fun evaluateContentSizeXImpl() = 0
    override fun evaluateContentSizeYImpl() = 0

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): ViewController {
        var currentOffset = offset
        var childController = ViewController.NONE
        var disposed = false

        fun load() {
            if (disposed) return
            childController.dispose()
            childController.detach()

            val childView = content.value
            childView.initSizeCalculation(renderingProxy, { evaluateActualSizeX() }, { evaluateActualSizeY() })
            childController = childView.attachTo(currentOffset, viewPlacer)
        }

        val contentRemover = content.register { _, _ -> load() }
        load()

        return object : ViewController {
            override fun setOffset(offset: IntPoint) {
                currentOffset = offset
                childController.setOffset(offset)
            }

            override fun detach() {
                childController.detach()
            }

            override fun dispose() {
                if (disposed) return
                disposed = true
                contentRemover.remove()
                childController.dispose()
            }
        }
    }

}
