package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.evaluateActualSize
import miragefairy2024.mod.recipeviewer.view.plus
import miragefairy2024.mod.recipeviewer.view.sized

/** Vanilla の View の描画に使う値を、描画直前に更新する処理を登録するのだ～🌱 */
class BeforeRenderView(val listener: () -> Unit) : WrapperView(), PlaceableView {
    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): ViewController {
        return viewPlacer.place(this, offset.sized(evaluateActualSize())) + super.attachTo(offset, viewPlacer)
    }
}

fun <P> Child<P, *>.beforeRender(listener: () -> Unit) = this.wrap(BeforeRenderView(listener))
