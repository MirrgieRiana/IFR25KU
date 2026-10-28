package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.ModContext
import miragefairy2024.client.mixins.api.SlotPositionSetter
import miragefairy2024.client.mixins.api.SlotViewClipping
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.views.MarginView
import miragefairy2024.mod.recipeviewer.views.MenuSlotView
import miragefairy2024.mod.recipeviewer.views.YScrollView
import miragefairy2024.mod.recipeviewer.views.plusAssign
import miragefairy2024.util.Remover
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.network.chat.FormattedText
import net.minecraft.util.FormattedCharSequence
import net.minecraft.world.SimpleContainer
import net.minecraft.world.inventory.Slot
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.util.function.Supplier

private val SLOT_RENDERING_PROXY: RenderingProxy = object : RenderingProxy {
    override fun calculateTextWidth(text: FormattedText) = throw UnsupportedOperationException()
    override fun calculateTextWidth(text: FormattedCharSequence) = throw UnsupportedOperationException()
    override fun getTextHeight() = throw UnsupportedOperationException()
    override fun wrapText(text: FormattedText, maxWidth: Int): List<FormattedCharSequence> = throw UnsupportedOperationException()
}

private class ClippedTestSlot : Slot(SimpleContainer(1), 0, 0, 0), SlotPositionSetter, SlotViewClipping {
    var position = IntPoint.ZERO
        private set
    private var clipBounds: Supplier<IntRectangle>? = null

    override fun `mirageFairy2024$setPosition`(x: Int, y: Int) {
        position = IntPoint(x, y)
    }

    override fun `mirageFairy2024$getClipBounds`() = clipBounds
    override fun `mirageFairy2024$setClipBounds`(clipBounds: Supplier<IntRectangle>?) {
        this.clipBounds = clipBounds
    }
}

private class SlotTestContext : VanillaViewPlacerContext {
    override val origin = IntPoint(10, 20)
    var clippingBounds: IntRectangle? = null
    val widgets = mutableListOf<AbstractWidget>()

    override fun getClipBounds() = clippingBounds
    override fun addRenderableWidget(widget: AbstractWidget): Remover {
        widgets += widget
        return Remover {
            widgets.remove(widget)
        }
    }

    override fun addRenderableOnly(widget: AbstractWidget) = addRenderableWidget(widget)
}

class ScrollMenuSlotViewTest {
    companion object {
        @JvmStatic
        @BeforeAll
        fun initializePlacers() {
            with(ModContext()) {
                initVanillaViewPlacers()
            }
        }
    }

    @Test
    fun `部分的に見えるスロットも表示領域の境界で入力を制限するのだ`() {
        val slot = ClippedTestSlot()
        slot.`mirageFairy2024$setClipBounds`(Supplier { IntRectangle(10, 20, 16, 16) })

        assertTrue(slot.`mirageFairy2024$isInsideClip`(10.0, 20.0))
        assertTrue(slot.`mirageFairy2024$isInsideClip`(25.9, 35.9))
        assertFalse(slot.`mirageFairy2024$isInsideClip`(9.9, 20.0))
        assertFalse(slot.`mirageFairy2024$isInsideClip`(26.0, 20.0))
        assertFalse(slot.`mirageFairy2024$isInsideClip`(10.0, 19.9))
        assertFalse(slot.`mirageFairy2024$isInsideClip`(10.0, 36.0))
    }

    @Test
    fun `通常の領域への再配置では以前のクリッピングを解除するのだ`() {
        val context = SlotTestContext()
        val slot = ClippedTestSlot()
        slot.`mirageFairy2024$setClipBounds`(Supplier { IntRectangle.ZERO })

        val controller = VANILLA_VIEW_PLACER_REGISTRY.place(context, MenuSlotView(slot), IntRectangle(10, 20, 18, 18))

        assertNull(slot.`mirageFairy2024$getClipBounds`())
        assertTrue(slot.`mirageFairy2024$isInsideClip`(-100.0, -100.0))
        controller.dispose()
        assertThrows(UnsupportedOperationException::class.java) { controller.detach() }
    }

}
