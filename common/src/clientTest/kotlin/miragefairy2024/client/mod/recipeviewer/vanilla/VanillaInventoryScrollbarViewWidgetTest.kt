package miragefairy2024.client.mod.recipeviewer.vanilla

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.BufferBuilder
import com.mojang.blaze3d.vertex.BufferUploader
import com.mojang.blaze3d.vertex.ByteBufferBuilder
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.MeshData
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
import com.mojang.blaze3d.vertex.VertexFormatElement
import miragefairy2024.MirageFairy2024
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.x2
import miragefairy2024.mod.recipeviewer.view.y2
import miragefairy2024.mod.recipeviewer.views.InventoryScrollbarView
import miragefairy2024.mod.recipeviewer.views.XInventoryScrollbarView
import miragefairy2024.mod.recipeviewer.views.YInventoryScrollbarView
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.resources.ResourceLocation
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.lwjgl.glfw.GLFW
import org.mockito.Mockito
import java.nio.ByteOrder
import kotlin.math.roundToInt

private val BACKGROUND_TEXTURE = MirageFairy2024.identifier("textures/gui/inventory_scrollbar_background.png")
private val THUMB_TEXTURE = MirageFairy2024.identifier("textures/gui/inventory_scrollbar_scroller.png")
private val DISABLED_THUMB_TEXTURE = MirageFairy2024.identifier("textures/gui/inventory_scrollbar_scroller_disabled.png")

private class ScrollbarGraphics : GuiGraphics(Minecraft.getInstance(), null) {
    val draws = mutableListOf<Pair<ResourceLocation, List<IntRectangle>>>()

    fun recordDraw(texture: ResourceLocation, mesh: MeshData) {
        val state = mesh.drawState()
        val format = state.format()
        Assertions.assertEquals(VertexFormat.Mode.QUADS, state.mode())
        Assertions.assertEquals(DefaultVertexFormat.POSITION_TEX, format)
        val vertices = mesh.vertexBuffer().order(ByteOrder.nativeOrder())
        val quads = (0 until state.vertexCount()).chunked(4).map { quad ->
            Assertions.assertEquals(4, quad.size)
            val positions = quad.map { index ->
                val vertex = index * format.vertexSize
                val uv = vertex + format.getOffset(VertexFormatElement.UV0)
                Assertions.assertTrue(vertices.getFloat(uv) in 0F..(15F / 16F))
                Assertions.assertTrue(vertices.getFloat(uv + Float.SIZE_BYTES) in 0F..(15F / 16F))
                val position = vertex + format.getOffset(VertexFormatElement.POSITION)
                Pair(vertices.getFloat(position).roundToInt(), vertices.getFloat(position + Float.SIZE_BYTES).roundToInt())
            }
            val left = positions.minOf { it.first }
            val top = positions.minOf { it.second }
            val right = positions.maxOf { it.first }
            val bottom = positions.maxOf { it.second }
            IntRectangle(left, top, right - left, bottom - top)
        }
        draws += Pair(texture, quads)
    }

    fun assertBackgroundOnly(background: IntRectangle) {
        Assertions.assertEquals(listOf(BACKGROUND_TEXTURE), draws.map { it.first })
        assertPaintedBounds(background, draws[0].second)
        Assertions.assertTrue(pose().clear())
    }

    fun assertBackgroundAndThumb(background: IntRectangle, thumb: IntRectangle, thumbTexture: ResourceLocation) {
        Assertions.assertEquals(listOf(BACKGROUND_TEXTURE, thumbTexture), draws.map { it.first })
        assertPaintedBounds(background, draws[0].second)
        assertPaintedBounds(thumb, draws[1].second)
        Assertions.assertTrue(pose().clear())
    }

    private fun assertPaintedBounds(bounds: IntRectangle, rectangles: List<IntRectangle>) {
        Assertions.assertEquals(bounds.x, rectangles.minOf { it.x })
        Assertions.assertEquals(bounds.y, rectangles.minOf { it.y })
        Assertions.assertEquals(bounds.x2, rectangles.maxOf { it.x2 })
        Assertions.assertEquals(bounds.y2, rectangles.maxOf { it.y2 })
        Assertions.assertEquals(bounds.sizeX * bounds.sizeY, rectangles.sumOf { it.sizeX * it.sizeY })
    }
}

private fun withScrollbarGraphics(block: (ScrollbarGraphics) -> Unit) {
    GuiTestEnvironment().use { environment ->
        ByteBufferBuilder(1024).use { buffer ->
            val tesselator: Tesselator = Mockito.mock(Tesselator::class.java)
            val mode = VertexFormat.Mode.QUADS
            val format = DefaultVertexFormat.POSITION_TEX
            Mockito.`when`(tesselator.begin(mode, format)).thenAnswer { BufferBuilder(buffer, mode, format) }
            Mockito.mockStatic(Tesselator::class.java).use { tesselators ->
                tesselators.`when`<Tesselator> { Tesselator.getInstance() }.thenReturn(tesselator)
                Mockito.mockStatic(BufferUploader::class.java).use { uploader ->
                    val graphics = ScrollbarGraphics()
                    var texture: ResourceLocation? = null
                    environment.renderSystem.`when`<Unit> { RenderSystem.setShaderTexture(Mockito.eq(0), Mockito.any(ResourceLocation::class.java)) }.thenAnswer {
                        texture = it.getArgument(1)
                        null
                    }
                    uploader.`when`<Unit> { BufferUploader.drawWithShader(Mockito.any(MeshData::class.java)) }.thenAnswer {
                        it.getArgument<MeshData>(0).use { mesh -> graphics.recordDraw(checkNotNull(texture), mesh) }
                        null
                    }
                    block(graphics)
                }
            }
        }
    }
}

class VanillaInventoryScrollbarViewWidgetTest {

    private fun createScrollbar(horizontal: Boolean, bounds: IntRectangle): Pair<InventoryScrollbarView, VanillaInventoryScrollbarViewWidget> {
        return if (horizontal) {
            val view = XInventoryScrollbarView()
            Pair(view, VanillaXInventoryScrollbarViewWidget(view, bounds))
        } else {
            val view = YInventoryScrollbarView()
            Pair(view, VanillaYInventoryScrollbarViewWidget(view, bounds))
        }
    }

    @Test
    fun `両軸でインベントリ風の背景とつまみを描きスクロール位置を読み書きするのだ`() {
        listOf(false, true).forEach { horizontal ->
            withScrollbarGraphics { graphics ->
                val bounds = if (horizontal) IntRectangle(10, 20, 100, 18) else IntRectangle(10, 20, 18, 100)
                val (view, widget) = createScrollbar(horizontal, bounds)
                val scrollOffset = view.scrollOffset
                view.contentLength = 300
                view.viewportLength = 100
                val mouseX = if (horizontal) 23.0 else 12.0
                val mouseY = if (horizontal) 22.0 else 33.0
                fun assertScrollbar(position: Int) {
                    graphics.draws.clear()
                    widget.render(graphics, mouseX.toInt(), mouseY.toInt(), 0F)
                    val thumb = if (horizontal) IntRectangle(11 + position, 21, 31, 16) else IntRectangle(11, 21 + position, 16, 31)
                    graphics.assertBackgroundAndThumb(bounds, thumb, THUMB_TEXTURE)
                }

                assertScrollbar(0)
                Assertions.assertTrue(widget.mouseScrolled(mouseX, mouseY, 0.0, -1.0))
                Assertions.assertEquals(15.0, scrollOffset.value)
                assertScrollbar(5)

                widget.isFocused = true
                Assertions.assertTrue(widget.keyPressed(if (horizontal) GLFW.GLFW_KEY_RIGHT else GLFW.GLFW_KEY_DOWN, 0, 0))
                Assertions.assertEquals(30.0, scrollOffset.value)
                assertScrollbar(10)

                Assertions.assertTrue(widget.mouseClicked(mouseX, mouseY, 0))
                val dragX = if (horizontal) 67.0 else 0.0
                val dragY = if (horizontal) 0.0 else 67.0
                Assertions.assertTrue(widget.mouseDragged(mouseX + dragX, mouseY + dragY, 0, dragX, dragY))
                Assertions.assertEquals(200.0, scrollOffset.value)
                assertScrollbar(67)
                Assertions.assertTrue(widget.mouseReleased(mouseX + dragX, mouseY + dragY, 0))

                scrollOffset.value = 100.0
                assertScrollbar(34)
                widget.dispose()
            }
        }
    }

    @Test
    fun `両軸で空内容のつまみと極小寸法の描画を確認するのだ`() {
        listOf(false, true).forEach { horizontal ->
            withScrollbarGraphics { graphics ->
                val bounds = if (horizontal) IntRectangle(10, 20, 100, 18) else IntRectangle(10, 20, 18, 100)
                val (view, widget) = createScrollbar(horizontal, bounds)
                view.viewportLength = 100
                widget.render(graphics, 12, 22, 0F)
                val disabledThumb = if (horizontal) IntRectangle(11, 21, 98, 16) else IntRectangle(11, 21, 16, 98)
                graphics.assertBackgroundAndThumb(bounds, disabledThumb, DISABLED_THUMB_TEXTURE)
                Assertions.assertFalse(widget.mouseClicked(12.0, 22.0, 0))

                (2..5).forEach { width ->
                    graphics.draws.clear()
                    widget.setSize(width, 6)
                    widget.render(graphics, 12, 22, 0F)
                    graphics.assertBackgroundOnly(IntRectangle(10, 20, width, 6))
                }

                graphics.draws.clear()
                view.contentLength = 300
                widget.setSize(if (horizontal) 0 else 18, if (horizontal) 18 else 0)
                widget.render(graphics, 12, 22, 0F)
                Assertions.assertTrue(graphics.draws.isEmpty())
                Assertions.assertFalse(widget.mouseClicked(12.0, 22.0, 0))

                widget.setSize(6, 6)
                widget.render(graphics, 12, 22, 0F)
                graphics.assertBackgroundAndThumb(IntRectangle(10, 20, 6, 6), IntRectangle(11, 21, 4, 4), THUMB_TEXTURE)
                Assertions.assertFalse(widget.mouseClicked(12.0, 22.0, 0))
                Assertions.assertFalse(widget.hasMouseCapture(0))
                widget.dispose()
            }
        }
    }
}
