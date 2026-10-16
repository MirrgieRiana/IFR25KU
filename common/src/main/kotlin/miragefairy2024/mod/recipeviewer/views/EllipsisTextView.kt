package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.ColorPair
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.util.Remover
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.util.FormattedCharSequence

/** 収まらない分を置き換える文字なのだ～🌱 */
private const val ELLIPSIS = "..."

/**
 * 親が与えた幅に収まる1行だけを見せるViewなのだ～🌱
 * 収まらなかった場合は、末尾へ省略記号を足すのだ～🌱
 * 折り返す幅が決まるのは[miragefairy2024.mod.recipeviewer.views.EllipsisTextView.calculateActualSize]のため、横幅を[miragefairy2024.mod.recipeviewer.views.EllipsisTextView.contentSize]では出せないのだ～🌱
 */
class EllipsisTextView(private val text: Component) : View {

    var color = ColorPair.DEFAULT

    var shadow = true

    var alignmentX = Alignment.START

    override val sizingX = Sizing.FILL

    override val sizingY = Sizing.WRAP

    private lateinit var renderingProxy: RenderingProxy

    /** 高さは1行の分なのだ～🌱 横幅は、折り返す幅が決まっていないため0なのだ～🌱 */
    override var contentSize = IntPoint.ZERO
        private set

    override fun calculateContentSize(renderingProxy: RenderingProxy) {
        this.renderingProxy = renderingProxy
        contentSize = IntPoint(0, renderingProxy.getTextHeight())
    }

    private var textView: TextView? = null

    override var actualSize = IntPoint.ZERO
        private set

    override fun calculateActualSize(regionSize: IntPoint) {
        val lines = renderingProxy.wrapText(text, regionSize.x)
        val firstLine = lines.firstOrNull() ?: FormattedCharSequence.EMPTY
        val visibleLine = if (lines.size < 2) firstLine else FormattedCharSequence.composite(firstLine, FormattedCharSequence.forward(ELLIPSIS, styleOf(lines[1])))
        textView = TextView(visibleLine).also {
            it.sizingX = Sizing.FILL
            it.color = color
            it.shadow = shadow
            it.alignmentX = alignmentX
            it.calculateContentSize(renderingProxy)
            it.calculateActualSize(IntPoint(regionSize.x, 0))
        }
        actualSize = IntPoint(regionSize.x, contentSize.y)
    }

    /** 省略記号は、続きの行の先頭と同じ見た目にするのだ～🌱 */
    private fun styleOf(line: FormattedCharSequence): Style {
        var style = Style.EMPTY
        line.accept { _, lineStyle, _ ->
            style = lineStyle
            false
        }
        return style
    }

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): Remover {
        return textView!!.attachTo(offset, viewPlacer)
    }

}
