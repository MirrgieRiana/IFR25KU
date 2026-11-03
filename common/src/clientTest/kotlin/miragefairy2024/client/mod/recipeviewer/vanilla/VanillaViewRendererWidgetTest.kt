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
import net.minecraft.client.gui.navigation.FocusNavigationEvent
import net.minecraft.client.gui.navigation.ScreenDirection
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

private class RendererTestView : FixedWidgetView(IntPoint(100, 100))

class VanillaViewRendererWidgetTest {

    @Test
    fun `描画専用の View は Tab と矢印キーのフォーカス候補にしないのだ`() {
        fun <V : PlaceableView> check(view: V, renderer: ViewRenderer<V>) {
            val widget = VanillaViewRendererWidget(renderer, view, IntRectangle(10, 20, 100, 100))
            assertNull(widget.nextFocusPath(FocusNavigationEvent.TabNavigation(true)))
            assertNull(widget.nextFocusPath(FocusNavigationEvent.ArrowNavigation(ScreenDirection.DOWN)))
        }
        val textureId = ResourceLocation.fromNamespaceAndPath("test", "background")
        val texture = ViewTexture(textureId, IntPoint(16, 16))
        check(TooltipBackgroundView(), TooltipBackgroundViewRenderer)
        check(FilledRectangleView(), FilledRectangleViewRenderer)
        check(NinePatchImageView(textureId, 16, 16, 4, 8, 4, 4, 8, 4, false), NinePatchImageViewRenderer)
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
        assertNotNull(clickableWidget.nextFocusPath(event))

        val view = ImageButtonView(IntPoint(16, 16))
        val buttonWidget = VanillaViewRendererWidget(ImageButtonViewRenderer, view, bounds)
        val path = buttonWidget.nextFocusPath(event)
        assertNotNull(path)
        path!!.applyFocus(true)
        assertTrue(buttonWidget.isFocused)
        assertNull(buttonWidget.nextFocusPath(event))
        path.applyFocus(false)
        view.enabled.value = false
        assertNull(buttonWidget.nextFocusPath(event))
        view.enabled.value = true
        assertNotNull(buttonWidget.nextFocusPath(event))
    }

    @Test
    fun `処理したクリックに対応するドラッグと解放だけを消費するのだ`() {
        var consumeClick = false
        val renderer: ViewRenderer<RendererTestView> = object : ViewRenderer<RendererTestView> {
            override fun mouseClicked(view: RendererTestView, bounds: IntRectangle, mouseX: Int, mouseY: Int, button: Int) = consumeClick
        }
        val widget = VanillaViewRendererWidget(renderer, RendererTestView(), IntRectangle(10, 20, 100, 100))
        assertFalse(widget.mouseClicked(30.0, 40.0, 0))
        assertFalse(widget.mouseDragged(30.0, 40.0, 0, 1.0, 1.0))
        assertFalse(widget.mouseReleased(30.0, 40.0, 0))

        consumeClick = true
        assertTrue(widget.mouseClicked(30.0, 40.0, 0))
        assertFalse(widget.mouseDragged(30.0, 40.0, 1, 1.0, 1.0))
        assertFalse(widget.mouseReleased(30.0, 40.0, 1))
        assertTrue(widget.mouseDragged(300.0, 400.0, 0, 1.0, 1.0))
        assertTrue(widget.mouseReleased(300.0, 400.0, 0))
        assertFalse(widget.mouseReleased(300.0, 400.0, 0))

        assertTrue(widget.mouseClicked(30.0, 40.0, 0))
        consumeClick = false
        assertFalse(widget.mouseClicked(30.0, 40.0, 0))
        assertFalse(widget.mouseDragged(30.0, 40.0, 0, 1.0, 1.0))
        assertFalse(widget.mouseReleased(30.0, 40.0, 0))
    }

}
