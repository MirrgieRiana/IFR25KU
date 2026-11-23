package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.client.mod.recipeviewer.ViewRenderer
import miragefairy2024.client.mod.recipeviewer.common.ClickableViewRenderer
import miragefairy2024.client.mod.recipeviewer.common.FilledRectangleViewRenderer
import miragefairy2024.client.mod.recipeviewer.common.ImageButtonViewRenderer
import miragefairy2024.client.mod.recipeviewer.common.ImageViewRenderer
import miragefairy2024.client.mod.recipeviewer.common.ItemViewRenderer
import miragefairy2024.client.mod.recipeviewer.common.MultiLineTextViewRenderer
import miragefairy2024.client.mod.recipeviewer.common.NinePatchImageViewRenderer
import miragefairy2024.client.mod.recipeviewer.common.ProgressViewRenderer
import miragefairy2024.client.mod.recipeviewer.common.TextViewRenderer
import miragefairy2024.client.mod.recipeviewer.common.TooltipBackgroundViewRenderer
import miragefairy2024.client.mod.recipeviewer.common.TooltipViewRenderer
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.ViewTexture
import miragefairy2024.mod.recipeviewer.views.ClickableView
import miragefairy2024.mod.recipeviewer.views.FilledRectangleView
import miragefairy2024.mod.recipeviewer.views.FixedWidgetView
import miragefairy2024.mod.recipeviewer.views.ImageButtonView
import miragefairy2024.mod.recipeviewer.views.ImageView
import miragefairy2024.mod.recipeviewer.views.ItemView
import miragefairy2024.mod.recipeviewer.views.MultiLineTextView
import miragefairy2024.mod.recipeviewer.views.NinePatchImageView
import miragefairy2024.mod.recipeviewer.views.ProgressView
import miragefairy2024.mod.recipeviewer.views.TextView
import miragefairy2024.mod.recipeviewer.views.TooltipBackgroundView
import miragefairy2024.mod.recipeviewer.views.TooltipView
import mirrg.kotlin.helium.Slot
import net.minecraft.SharedConstants
import net.minecraft.client.gui.navigation.FocusNavigationEvent
import net.minecraft.client.gui.navigation.ScreenDirection
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.Bootstrap
import net.minecraft.world.item.ItemStack
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

private class RendererTestView : FixedWidgetView(IntPoint(100, 100))

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VanillaViewRendererWidgetTest {

    @BeforeAll
    fun initMinecraft() {
        SharedConstants.tryDetectVersion()
        Bootstrap.bootStrap()
    }

    @Test
    fun `描画専用の View は Tab と矢印キーのフォーカス候補にしないのだ`() {
        fun <V : PlaceableView> check(view: V, renderer: ViewRenderer<V, Unit>) {
            val widget = VanillaViewRendererWidget(renderer, view, IntRectangle(10, 20, 100, 100))
            Assertions.assertNull(widget.nextFocusPath(FocusNavigationEvent.TabNavigation(true)))
            Assertions.assertNull(widget.nextFocusPath(FocusNavigationEvent.ArrowNavigation(ScreenDirection.DOWN)))
        }
        val textureId = ResourceLocation.fromNamespaceAndPath("test", "background")
        val texture = ViewTexture(textureId, IntPoint(16, 16))
        check(TooltipBackgroundView(), TooltipBackgroundViewRenderer)
        check(FilledRectangleView(), FilledRectangleViewRenderer)
        check(NinePatchImageView(textureId, 16, 16, 4, 8, 4, 4, 8, 4, false, false), NinePatchImageViewRenderer)
        check(ImageView(texture), ImageViewRenderer)
        check(ItemView(ItemStack.EMPTY), ItemViewRenderer)
        check(TextView(), TextViewRenderer)
        check(MultiLineTextView(), MultiLineTextViewRenderer)
        check(ProgressView(texture, texture), ProgressViewRenderer)
        check(TooltipView(), TooltipViewRenderer)
    }

    @Test
    fun `クリック可能な View のフォーカスを維持し無効な画像ボタンは候補にしないのだ`() {
        val bounds = IntRectangle(10, 20, 100, 100)
        val event = FocusNavigationEvent.TabNavigation(true)
        val clickableWidget = VanillaViewRendererWidget(ClickableViewRenderer, ClickableView(), bounds)
        Assertions.assertNotNull(clickableWidget.nextFocusPath(event))

        val view = ImageButtonView(IntPoint(16, 16))
        val buttonWidget = VanillaViewRendererWidget(ImageButtonViewRenderer, view, bounds)
        val path = buttonWidget.nextFocusPath(event)
        Assertions.assertNotNull(path)
        path!!.applyFocus(true)
        Assertions.assertTrue(buttonWidget.isFocused)
        Assertions.assertNull(buttonWidget.nextFocusPath(event))
        path.applyFocus(false)
        view.enabled.value = false
        Assertions.assertNull(buttonWidget.nextFocusPath(event))
        view.enabled.value = true
        Assertions.assertNotNull(buttonWidget.nextFocusPath(event))
    }

    @Test
    fun `処理したクリックに対応するドラッグと解放だけを消費するのだ`() {
        var consumeClick = false
        val renderer: ViewRenderer<RendererTestView, Unit> = object : ViewRenderer<RendererTestView, Unit> {
            override fun createState(view: RendererTestView) = Unit
            override fun mouseClicked(view: RendererTestView, state: Slot<Unit>, bounds: IntRectangle, mouseX: Int, mouseY: Int, button: Int) = consumeClick
        }
        val widget = VanillaViewRendererWidget(renderer, RendererTestView(), IntRectangle(10, 20, 100, 100))
        Assertions.assertFalse(widget.mouseClicked(30.0, 40.0, 0))
        Assertions.assertFalse(widget.mouseDragged(30.0, 40.0, 0, 1.0, 1.0))
        Assertions.assertFalse(widget.mouseReleased(30.0, 40.0, 0))

        consumeClick = true
        Assertions.assertTrue(widget.mouseClicked(30.0, 40.0, 0))
        Assertions.assertFalse(widget.mouseDragged(30.0, 40.0, 1, 1.0, 1.0))
        Assertions.assertFalse(widget.mouseReleased(30.0, 40.0, 1))
        Assertions.assertTrue(widget.mouseDragged(300.0, 400.0, 0, 1.0, 1.0))
        Assertions.assertTrue(widget.mouseReleased(300.0, 400.0, 0))
        Assertions.assertFalse(widget.mouseReleased(300.0, 400.0, 0))

        Assertions.assertTrue(widget.mouseClicked(30.0, 40.0, 0))
        consumeClick = false
        Assertions.assertFalse(widget.mouseClicked(30.0, 40.0, 0))
        Assertions.assertFalse(widget.mouseDragged(30.0, 40.0, 0, 1.0, 1.0))
        Assertions.assertFalse(widget.mouseReleased(30.0, 40.0, 0))
    }

}
