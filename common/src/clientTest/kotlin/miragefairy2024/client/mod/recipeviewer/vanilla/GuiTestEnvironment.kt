package miragefairy2024.client.mod.recipeviewer.vanilla

import com.mojang.blaze3d.platform.Window
import com.mojang.blaze3d.systems.RenderSystem
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.util.empty
import miragefairy2024.util.text
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner
import net.minecraft.util.FormattedCharSequence
import org.mockito.MockedStatic
import org.mockito.Mockito

/** GPU 呼び出しをモックに置き換え、バニラのクリッピングとツールチップ予約を実行するのだ～🌱 */
internal class GuiTestEnvironment : AutoCloseable {
    private val minecraft: Minecraft = Mockito.mock(Minecraft::class.java)
    private val minecraftMock: MockedStatic<Minecraft> = Mockito.mockStatic(Minecraft::class.java)
    val renderSystem: MockedStatic<RenderSystem> = Mockito.mockStatic(RenderSystem::class.java)
    val events = mutableListOf<String>()
    val fills = mutableListOf<IntRectangle>()
    val tooltips = mutableListOf<List<FormattedCharSequence>>()
    var render: (GuiGraphics, Int, Int, Float) -> Unit = { _, _, _, _ -> }

    val screen: Screen = object : Screen(text { empty() }) {
        override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
            this@GuiTestEnvironment.render(guiGraphics, mouseX, mouseY, partialTick)
            events += "screen-end"
        }
    }

    val graphics: GuiGraphics = object : GuiGraphics(minecraft, null) {
        override fun fill(minX: Int, minY: Int, maxX: Int, maxY: Int, color: Int) {
            fills += IntRectangle(minX, minY, maxX - minX, maxY - minY)
            events += "fill"
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

    override fun close() {
        try {
            renderSystem.close()
        } finally {
            minecraftMock.close()
        }
    }
}
