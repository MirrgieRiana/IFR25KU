package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.ColorPair
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.offset
import miragefairy2024.util.Remover
import miragefairy2024.util.flatten
import net.minecraft.network.chat.Component

/**
 * 親が与えた幅で折り返して、全部の行を縦に並べるViewなのだ～🌱
 * 折り返す幅が決まるのは[miragefairy2024.mod.recipeviewer.views.WrappedTextView.calculateActualSize]のため、高さを[miragefairy2024.mod.recipeviewer.views.WrappedTextView.contentSize]では出せないのだ～🌱
 *
 * 伸びた高さを親へ伝える経路は[miragefairy2024.mod.recipeviewer.views.AbstractView.calculateActualSizeY]なのだ～🌱
 * だから、縦が[miragefairy2024.mod.recipeviewer.view.Sizing.FILL]の親に包まれると、その親から上へは伸びた分が伝わらないのだ～🌱
 */
class WrappedTextView(private val text: Component) : View {

    var color = ColorPair.DEFAULT

    var shadow = true

    /** 折り返した行それぞれを、この幅の中のどこへ寄せるかなのだ～🌱 */
    var alignmentX = Alignment.START

    override val sizingX = Sizing.FILL

    override val sizingY = Sizing.WRAP

    private lateinit var renderingProxy: RenderingProxy

    /** 折り返す幅が決まっていないため、高さは0なのだ～🌱 */
    override val contentSize = IntPoint.ZERO

    override fun calculateContentSize(renderingProxy: RenderingProxy) {
        this.renderingProxy = renderingProxy
    }

    private var textViews = listOf<TextView>()

    override var actualSize = IntPoint.ZERO
        private set

    override fun calculateActualSize(regionSize: IntPoint) {
        textViews = renderingProxy.wrapText(text, regionSize.x).map { formattedCharSequence ->
            TextView(formattedCharSequence).also {
                it.sizingX = Sizing.FILL
                it.color = color
                it.shadow = shadow
                it.alignmentX = alignmentX
            }
        }
        textViews.forEach {
            it.calculateContentSize(renderingProxy)
            it.calculateActualSize(IntPoint(regionSize.x, 0))
        }
        actualSize = IntPoint(regionSize.x, textViews.sumOf { it.actualSize.y })
    }

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): Remover {
        var y = 0
        return textViews.map {
            val remover = it.attachTo(offset.offset(0, y), viewPlacer)
            y += it.actualSize.y
            remover
        }.flatten()
    }

}
