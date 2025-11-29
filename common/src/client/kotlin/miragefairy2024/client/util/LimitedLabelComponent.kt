package miragefairy2024.client.util

import io.wispforest.owo.ui.base.BaseComponent
import io.wispforest.owo.ui.core.AnimatableProperty
import io.wispforest.owo.ui.core.Color
import io.wispforest.owo.ui.core.HorizontalAlignment
import io.wispforest.owo.ui.core.OwoUIDrawContext
import io.wispforest.owo.ui.core.Size
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.VerticalAlignment
import io.wispforest.owo.util.Observable
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.util.FormattedCharSequence
import java.util.function.Function
import kotlin.math.min

// ■■ from io.wispforest.owo.ui.component.LabelComponent
// ■■ https://github.com/wisp-forest/owo-lib/blob/1.20.2/src/main/java/io/wispforest/owo/ui/component/LabelComponent.java
open class LimitedLabelComponent(protected var text: Component) : BaseComponent() {
    protected val textRenderer = Minecraft.getInstance().font
    protected var wrappedText: List<FormattedCharSequence>
    protected var verticalTextAlignment = VerticalAlignment.TOP
    protected var horizontalTextAlignment = HorizontalAlignment.LEFT
    protected val color = AnimatableProperty.of(Color.WHITE)
    protected val lineHeight = Observable.of(textRenderer.lineHeight)
    protected var shadow = false
    protected var maxWidth: Int
    protected var textClickHandler = Function { style: Style? -> OwoUIDrawContext.utilityScreen().handleComponentClicked(style) }

    init {
        wrappedText = ArrayList()
        maxWidth = Int.MAX_VALUE
        lineHeight.observe { `$`: Int? -> notifyParentIfMounted() }
    }

    fun text(text: Component): LimitedLabelComponent {
        this.text = text
        notifyParentIfMounted()
        return this
    }

    fun text(): Component {
        return text
    }

    fun maxWidth(maxWidth: Int): LimitedLabelComponent {
        this.maxWidth = maxWidth
        notifyParentIfMounted()
        return this
    }

    fun maxWidth(): Int {
        return maxWidth
    }

    fun shadow(shadow: Boolean): LimitedLabelComponent {
        this.shadow = shadow
        return this
    }

    fun shadow(): Boolean {
        return shadow
    }

    fun color(color: Color): LimitedLabelComponent {
        this.color.set(color)
        return this
    }

    fun color(): AnimatableProperty<Color> {
        return color
    }

    fun verticalTextAlignment(verticalAlignment: VerticalAlignment): LimitedLabelComponent {
        verticalTextAlignment = verticalAlignment
        return this
    }

    fun verticalTextAlignment(): VerticalAlignment {
        return verticalTextAlignment
    }

    fun horizontalTextAlignment(horizontalAlignment: HorizontalAlignment): LimitedLabelComponent {
        horizontalTextAlignment = horizontalAlignment
        return this
    }

    fun horizontalTextAlignment(): HorizontalAlignment {
        return horizontalTextAlignment
    }

    fun lineHeight(lineHeight: Int): LimitedLabelComponent {
        this.lineHeight.set(lineHeight)
        return this
    }

    fun lineHeight(): Int {
        return lineHeight.get()
    }

    fun textClickHandler(textClickHandler: Function<Style?, Boolean>): LimitedLabelComponent {
        this.textClickHandler = textClickHandler
        return this
    }

    fun textClickHandler(): Function<Style?, Boolean> {
        return textClickHandler
    }

    override fun determineHorizontalContentSize(sizing: Sizing): Int {
        var widestText = 0
        for (line in wrappedText) {
            val width = textRenderer.width(line)
            if (width > widestText) widestText = width
        }
        return if (widestText > maxWidth) {
            wrapLines()
            determineHorizontalContentSize(sizing)
        } else {
            widestText
        }
    }

    override fun determineVerticalContentSize(sizing: Sizing): Int {
        wrapLines()
        return wrappedText.size * (this.lineHeight() + 2) - 2
    }

    override fun inflate(space: Size) {
        wrapLines()
        super.inflate(space)
    }

    private fun wrapLines() {
        wrappedText = textRenderer.split(text, if (horizontalSizing.get().isContent) maxWidth else width)

        // ■■ 追加分 {
        if (wrappedText.size >= 2) {
            var style: Style? = null
            wrappedText[1].accept { _, style2, _ ->
                style = style2
                false
            }
            wrappedText = if (style != null) {
                listOf(FormattedCharSequence.composite(wrappedText[0], FormattedCharSequence.forward("...", style!!)))
            } else {
                listOf(wrappedText[0])
            }
        }
        // ■■ }

    }

    override fun update(delta: Float, mouseX: Int, mouseY: Int) {
        super.update(delta, mouseX, mouseY)
        color.update(delta)
    }

    override fun draw(context: OwoUIDrawContext, mouseX: Int, mouseY: Int, partialTicks: Float, delta: Float) {
        val matrices = context.pose()
        matrices.pushPose()
        matrices.translate(0.0, 1 / Minecraft.getInstance().window.guiScale, 0.0)
        var x = x
        var y = y
        if (horizontalSizing.get().isContent) {
            x += horizontalSizing.get().value
        }
        if (verticalSizing.get().isContent) {
            y += verticalSizing.get().value
        }
        when (verticalTextAlignment) {
            VerticalAlignment.CENTER -> y += (height - (wrappedText.size * (this.lineHeight() + 2) - 2)) / 2
            VerticalAlignment.BOTTOM -> y += height - (wrappedText.size * (this.lineHeight() + 2) - 2)
            else -> Unit
        }
        val lambdaX = x
        val lambdaY = y
        @Suppress("DEPRECATION")
        context.drawManaged {
            for (i in wrappedText.indices) {
                val renderText = wrappedText[i]
                var renderX = lambdaX
                when (horizontalTextAlignment) {
                    HorizontalAlignment.CENTER -> renderX += (width - textRenderer.width(renderText)) / 2
                    HorizontalAlignment.RIGHT -> renderX += width - textRenderer.width(renderText)
                    else -> Unit
                }
                var renderY = lambdaY + i * (this.lineHeight() + 2)
                renderY += this.lineHeight() - textRenderer.lineHeight
                context.drawString(textRenderer, renderText, renderX, renderY, color.get().argb(), shadow)
            }
        }
        matrices.popPose()
    }

    override fun drawTooltip(context: OwoUIDrawContext, mouseX: Int, mouseY: Int, partialTicks: Float, delta: Float) {
        super.drawTooltip(context, mouseX, mouseY, partialTicks, delta)
        if (!isInBoundingBox(mouseX.toDouble(), mouseY.toDouble())) return
        context.renderComponentHoverEffect(textRenderer, styleAt(mouseX - x, mouseY - y), mouseX, mouseY)
    }

    override fun onMouseDown(mouseX: Double, mouseY: Double, button: Int): Boolean {
        return textClickHandler.apply(styleAt(mouseX.toInt(), mouseY.toInt())) or super.onMouseDown(mouseX, mouseY, button)
    }

    protected fun styleAt(mouseX: Int, mouseY: Int): Style? {
        return textRenderer.splitter.componentStyleAtWidth(wrappedText[min((mouseY / (this.lineHeight() + 2)).toDouble(), (wrappedText.size - 1).toDouble()).toInt()], mouseX)
    }

}
