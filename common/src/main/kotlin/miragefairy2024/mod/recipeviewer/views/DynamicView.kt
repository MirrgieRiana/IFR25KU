package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.util.EventRegistry
import miragefairy2024.util.ObservableValue
import miragefairy2024.util.fire

/** [content] を動的に付け替え可能な View なのだ～🌱 */
class DynamicView : AbstractView() {

    override var sizingX = Sizing.FILL
    override var sizingY = Sizing.FILL

    override fun evaluateContentSizeXImpl() = 0
    override fun evaluateContentSizeYImpl() = 0

    val content: ObservableValue<View?> = ObservableValue(null)

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): ViewController {
        val onClosed = EventRegistry<() -> Unit>()
        var currentOffset = offset
        var childController = ViewController.NONE
        var disposed = false

        content.observeAndInitialize(onClosed) { _, childView ->
            if (disposed) return@observeAndInitialize

            val oldChildController = childController
            childController = ViewController.NONE
            oldChildController.dispose()
            oldChildController.detach()

            if (disposed || childView == null) return@observeAndInitialize
            childView.initSizeCalculation(renderingProxy, { evaluateActualSizeX() }, { evaluateActualSizeY() })
            val newChildController = childView.attachTo(currentOffset, viewPlacer)
            if (disposed) {
                newChildController.dispose()
                newChildController.detach()
                return@observeAndInitialize
            }
            childController = newChildController
        }

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
                onClosed.fire()
            }
        }
    }

}
