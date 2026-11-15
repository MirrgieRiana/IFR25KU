package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.util.EventRegistry
import miragefairy2024.util.ObservableValue
import miragefairy2024.util.fire
import miragefairy2024.util.observeAndInitialize

/**
 * [content] を動的に付け替え可能な View なのだ～🌱
 * [content] が空なら、両軸とも [Sizing.WRAP] で寸法は 0 なのだ～🌱
 */
class DynamicView : View {

    val content: ObservableValue<View?> = ObservableValue(null)

    override val sizingX get() = content.value?.sizingX ?: Sizing.WRAP
    override val sizingY get() = content.value?.sizingY ?: Sizing.WRAP

    private lateinit var initializeContentSizeCalculation: (View) -> Unit
    private var initializedContent: View? = null

    override fun initSizeCalculation(renderingProxy: RenderingProxy, regionSizeXProvider: () -> Int, regionSizeYProvider: () -> Int) {
        initializeContentSizeCalculation = { it.initSizeCalculation(renderingProxy, regionSizeXProvider, regionSizeYProvider) }
        initializedContent = null
        getInitializedContent()
    }

    private fun getInitializedContent(): View? {
        val childView = content.value
        if (initializedContent !== childView) {
            if (childView != null) initializeContentSizeCalculation(childView)
            initializedContent = childView
        }
        return childView
    }

    override fun evaluateContentSizeX() = getInitializedContent()?.evaluateContentSizeX() ?: 0
    override fun evaluateContentSizeY() = getInitializedContent()?.evaluateContentSizeY() ?: 0
    override fun evaluateActualSizeX() = getInitializedContent()?.evaluateActualSizeX() ?: 0
    override fun evaluateActualSizeY() = getInitializedContent()?.evaluateActualSizeY() ?: 0

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): ViewController {
        val onClosed = EventRegistry<() -> Unit>()
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
                    val childView = getInitializedContent() ?: break
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

        content.observeAndInitialize(onClosed) { _, _ -> load() }

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
