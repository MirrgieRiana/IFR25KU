package miragefairy2024.client.mod.recipeviewer

import dev.emi.emi.api.widget.Bounds
import dev.emi.emi.api.widget.Widget
import io.wispforest.owo.ui.base.BaseComponent
import io.wispforest.owo.ui.core.Component
import io.wispforest.owo.ui.core.OwoUIAdapter
import io.wispforest.owo.ui.core.OwoUIDrawContext
import io.wispforest.owo.ui.core.ParentComponent
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.util.ScissorStack
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import java.util.function.BiFunction
import io.wispforest.owo.ui.core.Component as OwoComponent

/**
 * EMIの内部仕様のため、 mouseClicked および keyPressed を除くUIイベントは通知されません。
 */
class EmiUIAdapter<T : ParentComponent>(private val bounds: Bounds, rootComponentMaker: BiFunction<Sizing, Sizing, T>) : Widget() {
    val adapter: OwoUIAdapter<T> = OwoUIAdapter.createWithoutScreen(bounds.x(), bounds.y(), bounds.width(), bounds.height(), rootComponentMaker)

    init {
        adapter.inspectorZOffset = 900
        if (Minecraft.getInstance().screen != null) {
            ScreenEvents.remove(Minecraft.getInstance().screen).register { screen -> adapter.dispose() }
        }
    }

    fun prepare() = adapter.inflateAndMount()
    fun rootComponent(): T = adapter.rootComponent
    fun wrap(widget: Widget): OwoComponent = EmiWidgetComponent(widget)

    override fun getBounds(): Bounds = bounds

    override fun mouseClicked(mouseX: Int, mouseY: Int, button: Int) = adapter.mouseClicked((mouseX - adapter.x()).toDouble(), (mouseY - adapter.y()).toDouble(), button)
    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int) = adapter.keyPressed(keyCode, scanCode, modifiers)

    override fun render(draw: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        ScissorStack.push(adapter.x(), adapter.y(), adapter.width(), adapter.height(), draw.pose())
        adapter.render(draw, mouseX, mouseY, delta)
        ScissorStack.pop()
        draw.flush()
    }
}

class EmiWidgetComponent(private val widget: Widget) : BaseComponent() {
    private val bounds: Bounds = widget.bounds

    init {
        horizontalSizing.set(Sizing.fixed(bounds.width()))
        verticalSizing.set(Sizing.fixed(bounds.height()))
        mouseEnter().subscribe {
            focusHandler()!!.focus(this, Component.FocusSource.KEYBOARD_CYCLE)
        }
        mouseLeave().subscribe {
            focusHandler()!!.focus(null, null)
        }
    }

    override fun draw(context: OwoUIDrawContext, mouseX: Int, mouseY: Int, partialTicks: Float, delta: Float) = widget.render(context, mouseX, mouseY, partialTicks)
    override fun drawFocusHighlight(context: OwoUIDrawContext?, mouseX: Int, mouseY: Int, partialTicks: Float, delta: Float) = Unit
    override fun determineHorizontalContentSize(sizing: Sizing): Int = bounds.width()
    override fun determineVerticalContentSize(sizing: Sizing): Int = bounds.height()
    override fun onMouseDown(mouseX: Double, mouseY: Double, button: Int) = widget.mouseClicked(mouseX.toInt() + widget.bounds.x, mouseY.toInt() + widget.bounds.y, button) || super.onMouseDown(mouseX, mouseY, button)
    override fun canFocus(source: Component.FocusSource) = true
}
