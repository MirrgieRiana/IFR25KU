package miragefairy2024.client.mod.recipeviewer.vanilla

import com.mojang.blaze3d.platform.Window
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.BufferBuilder
import com.mojang.blaze3d.vertex.BufferUploader
import com.mojang.blaze3d.vertex.ByteBufferBuilder
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.MeshData
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
import com.mojang.blaze3d.vertex.VertexFormatElement
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.util.empty
import miragefairy2024.util.text
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics as GuiGraphicsExtractor // MIGRATION2612 ReplaceImport net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner
import net.minecraft.resources.ResourceLocation as Identifier // MIGRATION2612 ReplaceImport net.minecraft.resources.Identifier
import net.minecraft.util.FormattedCharSequence
import org.junit.jupiter.api.Assertions
import org.mockito.MockedStatic
import org.mockito.Mockito
import java.nio.ByteOrder

/** GPU 呼び出しをモックに置き換え、バニラのクリッピングとツールチップ予約を実行するのだ～🌱 */
internal class GuiTestEnvironment : AutoCloseable {
    private val minecraft: Minecraft = Mockito.mock(Minecraft::class.java)
    private val minecraftMock: MockedStatic<Minecraft> = Mockito.mockStatic(Minecraft::class.java)
    val renderSystem: MockedStatic<RenderSystem> = Mockito.mockStatic(RenderSystem::class.java)
    val events = mutableListOf<String>()
    val fills = mutableListOf<IntRectangle>()
    val outlines = mutableListOf<Outline>()
    val tooltips = mutableListOf<List<FormattedCharSequence>>()
    var render: (GuiGraphicsExtractor, Int, Int, Float) -> Unit = { _, _, _, _ -> }

    val screen: Screen = object : Screen(text { empty() }) {
        override fun render(guiGraphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTick: Float) {
            this@GuiTestEnvironment.render(guiGraphics, mouseX, mouseY, partialTick)
            events += "screen-end"
        }
    }

    val graphics: GuiGraphicsExtractor = object : GuiGraphicsExtractor(minecraft, null) {
        override fun fill(minX: Int, minY: Int, maxX: Int, maxY: Int, color: Int) {
            fills += IntRectangle(minX, minY, maxX - minX, maxY - minY)
            events += "fill"
        }

        override fun renderOutline(x: Int, y: Int, width: Int, height: Int, color: Int) {
            outlines += Outline(IntRectangle(x, y, width, height), color)
        }

        override fun drawString(font: Font?, text: FormattedCharSequence, x: Int, y: Int, color: Int, dropShadow: Boolean): Int {
            events += "text"
            return 0
        }

        override fun renderTooltip(font: Font?, tooltipLines: List<FormattedCharSequence>, tooltipPositioner: ClientTooltipPositioner, mouseX: Int, mouseY: Int) {
            tooltips += tooltipLines.toList()
            events += "tooltip"
        }
    }

    init {
        val window: Window = Mockito.mock(Window::class.java)
        Mockito.`when`(window.height).thenReturn(300)
        Mockito.`when`(window.guiScale).thenReturn(1.0)
        Mockito.`when`(minecraft.window).thenReturn(window)
        minecraft.screen = screen
        minecraftMock.`when`<Minecraft> { Minecraft.getInstance() }.thenReturn(minecraft)
    }

    data class Outline(val bounds: IntRectangle, val color: Int)

    data class TexturedVertex(val x: Float, val y: Float, val u: Float, val v: Float)

    data class TexturedDraw(val texture: Identifier, val quads: List<List<TexturedVertex>>)

    fun <T> withTexturedDraws(block: (MutableList<TexturedDraw>) -> T): T {
        return ByteBufferBuilder(1024).use { buffer ->
            val tesselator: Tesselator = Mockito.mock(Tesselator::class.java)
            val mode = VertexFormat.Mode.QUADS
            val format = DefaultVertexFormat.POSITION_TEX
            Mockito.`when`(tesselator.begin(mode, format)).thenAnswer { BufferBuilder(buffer, mode, format) }
            Mockito.mockStatic(Tesselator::class.java).use { tesselators ->
                tesselators.`when`<Tesselator> { Tesselator.getInstance() }.thenReturn(tesselator)
                Mockito.mockStatic(BufferUploader::class.java).use { uploader ->
                    val draws = mutableListOf<TexturedDraw>()
                    var texture: Identifier? = null
                    renderSystem.`when`<Unit> { RenderSystem.setShaderTexture(Mockito.eq(0), Mockito.any(Identifier::class.java)) }.thenAnswer {
                        texture = it.getArgument(1)
                        null
                    }
                    uploader.`when`<Unit> { BufferUploader.drawWithShader(Mockito.any(MeshData::class.java)) }.thenAnswer {
                        it.getArgument<MeshData>(0).use { mesh ->
                            val state = mesh.drawState()
                            Assertions.assertEquals(mode, state.mode())
                            Assertions.assertEquals(format, state.format())
                            val data = mesh.vertexBuffer().order(ByteOrder.nativeOrder())
                            val vertices = (0 until state.vertexCount()).map { index ->
                                val offset = index * format.vertexSize
                                val position = offset + format.getOffset(VertexFormatElement.POSITION)
                                val uv = offset + format.getOffset(VertexFormatElement.UV0)
                                TexturedVertex(
                                    data.getFloat(position), data.getFloat(position + Float.SIZE_BYTES),
                                    data.getFloat(uv), data.getFloat(uv + Float.SIZE_BYTES),
                                )
                            }
                            val quads = vertices.chunked(4)
                            Assertions.assertTrue(quads.all { quad -> quad.size == 4 })
                            draws += TexturedDraw(checkNotNull(texture), quads)
                        }
                        null
                    }
                    block(draws)
                }
            }
        }
    }

    override fun close() {
        try {
            renderSystem.close()
        } finally {
            minecraftMock.close()
        }
    }
}
