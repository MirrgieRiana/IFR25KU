package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.evaluateActualSize
import miragefairy2024.mod.recipeviewer.view.plus
import miragefairy2024.mod.recipeviewer.view.sized

/** Vanilla の View で、子の配置を保ったまま描画ルートの描画前通知に処理を登録するのだ～🌱 */
class BeforeRenderView(val listener: () -> Unit) : WrapperView(), PlaceableView {
    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): ViewController {
        return viewPlacer.place(this, offset.sized(evaluateActualSize())) + super.attachTo(offset, viewPlacer)
    }
}

fun <P> Child<P, *>.beforeRender(listener: () -> Unit) = this.wrap(BeforeRenderView(listener))
