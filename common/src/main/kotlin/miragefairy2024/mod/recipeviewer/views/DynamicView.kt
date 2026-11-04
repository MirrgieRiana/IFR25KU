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
        var needsLoad = false

        fun load() {
            if (disposed) return
            needsLoad = true
            if (loading) return

            loading = true
            try {
                while (needsLoad) {
                    needsLoad = false
                    val oldChildController = childController
                    childController = ViewController.NONE
                    oldChildController.dispose()
                    oldChildController.detach()
                    if (disposed) break

                    val childView = content.value
                    childView.initSizeCalculation(renderingProxy, { evaluateActualSizeX() }, { evaluateActualSizeY() })
                    val newChildController = childView.attachTo(currentOffset, viewPlacer)
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
