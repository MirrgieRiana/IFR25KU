package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.ModContext
import miragefairy2024.client.mixins.api.SlotPositionSetter
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.register
import miragefairy2024.mod.recipeviewer.views.FixedWidgetView
import miragefairy2024.mod.recipeviewer.views.MenuSlotView
import miragefairy2024.mod.recipeviewer.views.TextButtonView
import miragefairy2024.mod.recipeviewer.views.XScrollView
import miragefairy2024.mod.recipeviewer.views.YListView
import miragefairy2024.mod.recipeviewer.views.YScrollView
import miragefairy2024.mod.recipeviewer.views.plusAssign
import miragefairy2024.util.Remover
import miragefairy2024.util.empty
import miragefairy2024.util.invoke
import miragefairy2024.util.register
import miragefairy2024.util.text
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.network.chat.FormattedText
import net.minecraft.util.FormattedCharSequence
import net.minecraft.world.inventory.Slot
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

private class PlacerTestView : FixedWidgetView(IntPoint(100, 100)) {
    var disposals = 0
    var detaches = 0
}

private val PLACER_RENDERING_PROXY: RenderingProxy = object : RenderingProxy {
    override fun calculateTextWidth(text: FormattedText) = throw UnsupportedOperationException()
    override fun calculateTextWidth(text: FormattedCharSequence) = throw UnsupportedOperationException()
    override fun getTextHeight() = throw UnsupportedOperationException()
    override fun wrapText(text: FormattedText, maxWidth: Int): List<FormattedCharSequence> = throw UnsupportedOperationException()
}

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VanillaViewPlacersTest {
    @BeforeAll
    fun initRegistry() {
        with(ModContext()) {
            initVanillaViewPlacers()
        }
        VANILLA_VIEW_PLACER_REGISTRY.register { context, view: PlacerTestView, bounds ->
            val widget: AbstractWidget = object : AbstractWidget(bounds.x, bounds.y, bounds.sizeX, bounds.sizeY, text { empty() }) {
                override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) = Unit
                override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) = Unit
            }
            val remover = context.addRenderableWidget(widget)
            object : ViewController {
                override fun setOffset(offset: IntPoint) = widget.setPosition(offset.x, offset.y)
                override fun detach() {
                    view.detaches += 1
                    remover.remove()
                }
                override fun dispose() {
                    view.disposals += 1
                }
            }
        }
    }

    @Test
    fun `配置を許可しないコンテキストでは Slot の座標を書き換える前に拒否するのだ`() {
        val context: VanillaViewPlacerContext = object : VanillaViewPlacerContext {
            override val origin = IntPoint.ZERO
            override val canPlaceMenuSlots = false
            override fun addRenderableWidget(widget: AbstractWidget) = throw AssertionError("No widget should be registered")
            override fun addRenderableOnly(widget: AbstractWidget) = throw AssertionError("No widget should be registered")
        }
        val slot = Slot(null, 0, 11, 22)
        val view = MenuSlotView(slot)
        view.initSizeCalculation(PLACER_RENDERING_PROXY, { 100 }, { 100 })

        val exception = Assertions.assertThrows(UnsupportedOperationException::class.java) {
            view.attachTo(IntPoint(30, 40)) { child, bounds -> VANILLA_VIEW_PLACER_REGISTRY.place(context, child, bounds) }
        }

        Assertions.assertEquals("MenuSlotView cannot be placed in this context", exception.message)
        Assertions.assertEquals(IntPoint(11, 22), IntPoint(slot.x, slot.y))
    }

    @Test
    fun `配置を許可するコンテキストでは Slot の初期配置と移動を中継するのだ`() {
        val context: VanillaViewPlacerContext = object : VanillaViewPlacerContext {
            override val origin = IntPoint(5, 10)
            override val canPlaceMenuSlots = true
            override fun addRenderableWidget(widget: AbstractWidget) = throw AssertionError("Slots must not register widgets")
            override fun addRenderableOnly(widget: AbstractWidget) = throw AssertionError("Slots must not register widgets")
        }
        val positions = mutableListOf<IntPoint>()
        val slot: Slot = object : Slot(null, 0, 0, 0), SlotPositionSetter {
            override fun `mirageFairy2024$setPosition`(x: Int, y: Int) {
                positions += IntPoint(x, y)
            }
        }
        val view = MenuSlotView(slot)
        view.initSizeCalculation(PLACER_RENDERING_PROXY, { 100 }, { 100 })
        val controller = view.attachTo(IntPoint(20, 30)) { child, bounds -> VANILLA_VIEW_PLACER_REGISTRY.place(context, child, bounds) }
        controller.setOffset(IntPoint(35, 50))

        Assertions.assertEquals(listOf(IntPoint(16, 21), IntPoint(31, 41)), positions)
        controller.dispose()
    }

    @Test
    fun `親で Slot を配置できても縦横スクロールと入れ子の内側では拒否するのだ`() {
        val context: VanillaViewPlacerContext = object : VanillaViewPlacerContext {
            override val origin = IntPoint.ZERO
            override val canPlaceMenuSlots = true
            override fun addRenderableWidget(widget: AbstractWidget) = throw AssertionError("Placement must fail before widget registration")
            override fun addRenderableOnly(widget: AbstractWidget) = throw AssertionError("Placement must fail before widget registration")
        }
        listOf(false, true).forEach { horizontal ->
            listOf(false, true).forEach { nested ->
                val slot = Slot(null, 0, 11, 22)
                val view = if (horizontal) XScrollView() else YScrollView()
                view += YListView().also { list ->
                    if (nested) {
                        list += YScrollView().also {
                            it += MenuSlotView(slot)
                        }
                    } else {
                        list += MenuSlotView(slot)
                    }
                }
                view.initSizeCalculation(PLACER_RENDERING_PROXY, { 100 }, { 100 })

                val exception = Assertions.assertThrows(UnsupportedOperationException::class.java) {
                    view.attachTo(IntPoint.ZERO) { child, bounds -> VANILLA_VIEW_PLACER_REGISTRY.place(context, child, bounds) }
                }

                Assertions.assertEquals("MenuSlotView cannot be placed in this context", exception.message)
                Assertions.assertEquals(IntPoint(11, 22), IntPoint(slot.x, slot.y))
            }
        }
    }

    @Test
    fun `文字ボタンをバニラへ登録して押下と移動と解除を中継するのだ`() {
        val widgets = mutableListOf<AbstractWidget>()
        val context: VanillaViewPlacerContext = object : VanillaViewPlacerContext {
            override val origin = IntPoint.ZERO
            override val canPlaceMenuSlots = false
            override fun addRenderableWidget(widget: AbstractWidget): Remover {
                widgets += widget
                return Remover { widgets.remove(widget) }
            }
            override fun addRenderableOnly(widget: AbstractWidget) = throw AssertionError("Buttons must accept input")
        }
        var presses = 0
        val view = TextButtonView(IntPoint(80, 20), text { "back"() })
        view.onPress.register {
            presses += 1
        }
        view.initSizeCalculation(PLACER_RENDERING_PROXY, { 400 }, { 240 })
        val controller = view.attachTo(IntPoint(10, 30)) { child, bounds -> VANILLA_VIEW_PLACER_REGISTRY.place(context, child, bounds) }
        val button = widgets.single() as Button
        Assertions.assertEquals(IntPoint(10, 30), IntPoint(button.x, button.y))
        Assertions.assertEquals(IntPoint(80, 20), IntPoint(button.width, button.height))
        Assertions.assertEquals(view.message, button.message)

        button.onPress()
        Assertions.assertEquals(1, presses)
        controller.setOffset(IntPoint(40, 50))
        Assertions.assertEquals(IntPoint(40, 50), IntPoint(button.x, button.y))
        controller.dispose()
        controller.detach()
        Assertions.assertTrue(widgets.isEmpty())
    }

    @Test
    fun `本番の縦横スクロール登録が子の配置と移動と破棄と解除を中継するのだ`() {
        listOf(false, true).forEach { horizontal ->
            val screenWidgets = mutableListOf<AbstractWidget>()
            val context: VanillaViewPlacerContext = object : VanillaViewPlacerContext {
                override val origin = IntPoint(10, 20)
                override val canPlaceMenuSlots = true
                override fun addRenderableWidget(widget: AbstractWidget): Remover {
                    screenWidgets += widget
                    return Remover { screenWidgets.remove(widget) }
                }
                override fun addRenderableOnly(widget: AbstractWidget) = throw AssertionError("Scroll containers must accept input")
            }
            val leaf = PlacerTestView()
            val view = if (horizontal) XScrollView() else YScrollView()
            view += leaf
            view.initSizeCalculation(PLACER_RENDERING_PROXY, { 50 }, { 50 })
            val controller = view.attachTo(context.origin) { child, bounds -> VANILLA_VIEW_PLACER_REGISTRY.place(context, child, bounds) }
            val widget = screenWidgets.single() as VanillaScrollViewWidget
            Assertions.assertEquals(if (horizontal) VanillaXScrollViewWidget::class.java else VanillaYScrollViewWidget::class.java, widget.javaClass)
            val child = widget.children().single() as AbstractWidget
            Assertions.assertEquals(IntPoint(10, 20), IntPoint(child.x, child.y))
            try {
                controller.setOffset(IntPoint(40, 60))
                Assertions.assertEquals(IntPoint(40, 60), IntPoint(widget.x, widget.y))
                Assertions.assertEquals(IntPoint(40, 60), IntPoint(child.x, child.y))
            } finally {
                controller.dispose()
                controller.detach()
            }
            Assertions.assertEquals(1, leaf.disposals)
            Assertions.assertEquals(1, leaf.detaches)
            Assertions.assertFalse(widget.isActive)
            Assertions.assertTrue(widget.children().isEmpty())
            Assertions.assertTrue(screenWidgets.isEmpty())
        }
    }
}
