package miragefairy2024.client.mod.recipeviewer

import miragefairy2024.client.mod.recipeviewer.emi.EmiViewRendererWidget
import miragefairy2024.client.mod.recipeviewer.rei.ReiViewRendererWidget
import miragefairy2024.client.mod.recipeviewer.vanilla.GuiTestEnvironment
import miragefairy2024.client.mod.recipeviewer.vanilla.VanillaViewRendererWidget
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.views.FixedWidgetView
import mirrg.kotlin.helium.Slot
import net.minecraft.SharedConstants
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.server.Bootstrap
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

private class StateTestView(val initialValue: Int) : FixedWidgetView(IntPoint(100, 100))

private class RendererWidgetOperations(val render: () -> Unit, val click: () -> Boolean, val key: () -> Boolean)

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ViewRendererStateTest {

    @BeforeAll
    fun initMinecraft() {
        SharedConstants.tryDetectVersion()
        Bootstrap.bootStrap()
    }

    private fun checkState(createWidget: (ViewRenderer<StateTestView, Int>, StateTestView, IntRectangle, GuiGraphics) -> RendererWidgetOperations) {
        GuiTestEnvironment().use { environment ->
            val initializedViews = mutableListOf<StateTestView>()
            val observedStates = mutableListOf<Slot<Int>>()
            val observedValues = mutableListOf<Int>()
            fun record(state: Slot<Int>) {
                observedStates += state
                observedValues += state.value
            }
            val renderer: ViewRenderer<StateTestView, Int> = object : ViewRenderer<StateTestView, Int> {
                override fun createState(view: StateTestView): Int {
                    initializedViews += view
                    return view.initialValue
                }

                override fun render(view: StateTestView, state: Slot<Int>, bounds: IntRectangle, graphics: GuiGraphics, mouseX: Int, mouseY: Int, isHovered: Boolean, delta: Float) {
                    record(state)
                    state.value += 1
                }

                override fun mouseClicked(view: StateTestView, state: Slot<Int>, bounds: IntRectangle, mouseX: Int, mouseY: Int, button: Int): Boolean {
                    record(state)
                    state.value += 10
                    return true
                }

                override fun keyPressed(view: StateTestView, state: Slot<Int>, bounds: IntRectangle, keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
                    record(state)
                    state.value += 100
                    return true
                }
            }
            val firstView = StateTestView(10)
            val secondView = StateTestView(20)
            val bounds = IntRectangle(10, 20, 100, 100)
            val first = createWidget(renderer, firstView, bounds, environment.graphics)
            val second = createWidget(renderer, secondView, bounds, environment.graphics)
            Assertions.assertEquals(listOf(firstView, secondView), initializedViews)

            first.render()
            Assertions.assertTrue(first.click())
            Assertions.assertTrue(first.key())
            first.render()
            second.render()
            first.render()
            Assertions.assertEquals(listOf(firstView, secondView), initializedViews)

            val recreated = createWidget(renderer, firstView, bounds, environment.graphics)
            recreated.render()
            Assertions.assertEquals(listOf(firstView, secondView, firstView), initializedViews)
            Assertions.assertEquals(listOf(10, 11, 21, 121, 20, 122, 10), observedValues)
            listOf(1, 2, 3, 5).forEach {
                Assertions.assertSame(observedStates[0], observedStates[it])
            }
            Assertions.assertNotSame(observedStates[0], observedStates[4])
            Assertions.assertNotSame(observedStates[0], observedStates[6])
            Assertions.assertNotSame(observedStates[4], observedStates[6])
        }
    }

    @Test
    fun `バニラの描画と入力で状態を共有し配置と再生成ごとに分離するのだ`() {
        checkState { renderer, view, bounds, graphics ->
            val widget = VanillaViewRendererWidget(renderer, view, bounds)
            RendererWidgetOperations(
                { widget.render(graphics, 30, 40, 0.0F) },
                { widget.mouseClicked(30.0, 40.0, 0) },
                { widget.keyPressed(32, 0, 0) },
            )
        }
    }

    @Test
    fun `REIの描画と入力で状態を共有し配置と再生成ごとに分離するのだ`() {
        checkState { renderer, view, bounds, graphics ->
            val widget = ReiViewRendererWidget(renderer, view, bounds)
            RendererWidgetOperations(
                { widget.render(graphics, 30, 40, 0.0F) },
                { widget.mouseClicked(30.0, 40.0, 0) },
                { widget.keyPressed(32, 0, 0) },
            )
        }
    }

    @Test
    fun `EMIの描画と入力で状態を共有し配置と再生成ごとに分離するのだ`() {
        checkState { renderer, view, bounds, graphics ->
            val widget = EmiViewRendererWidget(renderer, view, bounds)
            RendererWidgetOperations(
                { widget.render(graphics, 30, 40, 0.0F) },
                { widget.mouseClicked(30, 40, 0) },
                { widget.keyPressed(32, 0, 0) },
            )
        }
    }

}
