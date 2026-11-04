package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.util.ObservableValue
import miragefairy2024.util.register

/** [content] を動的に付け替え可能な View なのだ～🌱 */
class DynamicView(val content: ObservableValue<View>) : AbstractView() {

    override var sizingX = Sizing.FILL
    override var sizingY = Sizing.FILL

    override fun evaluateContentSizeXImpl() = 0
    override fun evaluateContentSizeYImpl() = 0

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): ViewController {
        var currentOffset = offset
        var childController = ViewController.NONE
        var disposed = false
        var loading = false
        var needsLoad: Boolean

        fun load() {
            // 既に破棄済みの場合は、ロードをしないのだ～🌱
            if (disposed) return
            // 既にロード中の場合はフラグだけ立てて、そうでなければフラグを立てつつロードをするのだ～🌱
            needsLoad = true
            if (loading) return

            // ロード中フラグの管理なのだ～🌱
            loading = true
            try {

                // ロードの必要性が解消されない限り、ロードを繰り返すのだ～🌱
                while (needsLoad) {
                    needsLoad = false

                    // 古い子を破棄し、子の破棄中に破棄を食らった場合はロードをせずに終わるのだ～🌱
                    val oldChildController = childController
                    childController = ViewController.NONE
                    oldChildController.dispose()
                    oldChildController.detach()
                    if (disposed) break

                    // 子を実際にロードするのだ～🌱
                    val childView = content.value
                    childView.initSizeCalculation(renderingProxy, { evaluateActualSizeX() }, { evaluateActualSizeY() })
                    val newChildController = childView.attachTo(currentOffset, viewPlacer)
                    // 子のロード中に破棄された場合は、即子を破棄して抜けるのだ～🌱
                    if (disposed) {
                        newChildController.dispose()
                        newChildController.detach()
                        break
                    }
                    childController = newChildController

                }

            } finally {
                loading = false
            }
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
                disposed = true
                childController.dispose()
                contentRemover.remove()
            }
        }
    }

}
