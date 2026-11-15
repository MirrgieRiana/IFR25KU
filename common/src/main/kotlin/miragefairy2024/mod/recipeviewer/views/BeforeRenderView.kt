package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.ViewBeforeRenderEvent
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.ViewPlacerRegistry
import miragefairy2024.mod.recipeviewer.view.evaluateActualSize
import miragefairy2024.mod.recipeviewer.view.plus
import miragefairy2024.mod.recipeviewer.view.register
import miragefairy2024.mod.recipeviewer.view.sized

/** 子の配置を保ったまま、描画ルートの描画前通知に処理を登録するのだ～🌱 */
class BeforeRenderView(val listener: () -> Unit) : WrapperView(), PlaceableView {
    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): ViewController {
        return viewPlacer.place(this, offset.sized(evaluateActualSize())) + super.attachTo(offset, viewPlacer)
    }
}

fun <P> Child<P, *>.beforeRender(listener: () -> Unit) = this.wrap(BeforeRenderView(listener))

fun <C> ViewPlacerRegistry<C>.registerBeforeRender(event: (C) -> ViewBeforeRenderEvent) {
    this.register { context, view: BeforeRenderView, _ ->
        val remover = event(context).register(view.listener)
        object : ViewController {
            override fun setOffset(offset: IntPoint) = Unit
            override fun detach() = remover.remove()
        }
    }
}
