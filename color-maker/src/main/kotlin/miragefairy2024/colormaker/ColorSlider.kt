package miragefairy2024.colormaker

import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter
import java.awt.event.MouseWheelEvent
import java.awt.image.BufferedImage
import javax.swing.JPanel
import kotlin.math.roundToInt

class ColorSlider(private val steps: Int, private val colorFunction: (Float) -> Int) : JPanel() {
    val value = ObservableValue(0F)
    val repaintGradientEvent = ObservableValue(Unit)
    private var gradientImage = BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB)

    init {
        preferredSize = Dimension(256 + 1, 20)

        addComponentListener(object : ComponentAdapter() {
            override fun componentShown(e: ComponentEvent) {
                repaintGradientEvent.fire(this@ColorSlider)
            }

            override fun componentResized(e: ComponentEvent) {
                repaintGradientEvent.fire(this@ColorSlider)
            }
        })
        addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                value.set((e.x.toFloat() / (width - 1)).coerceIn(0F, 1F), this@ColorSlider)
            }
        })
        addMouseMotionListener(object : MouseMotionAdapter() {
            override fun mouseDragged(e: MouseEvent) {
                value.set((e.x.toFloat() / (width - 1)).coerceIn(0F, 1F), this@ColorSlider)
            }
        })
        addMouseWheelListener(object : MouseAdapter() {
            override fun mouseWheelMoved(e: MouseWheelEvent) {
                value.set((value.get() + -e.preciseWheelRotation.toFloat() / steps.toFloat()).coerceIn(0F, 1F), this@ColorSlider)
            }
        })

        value.register { _, _, _ ->
            repaint()
        }

        repaintGradientEvent.register { _, _, _ ->
            if (width <= 0) return@register
            if (gradientImage.width != width) gradientImage = BufferedImage(width, 1, BufferedImage.TYPE_INT_RGB)

            val g = gradientImage.createGraphics()
            (0 until width).forEach { x ->
                gradientImage.setRGB(x, 0, colorFunction(x.toFloat() / (width - 1)))
            }
            g.dispose()

            repaint()
        }

    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        if (g !is Graphics2D) return

        // グラデーション
        g.drawImage(gradientImage, 0, 0, width, height, null)

        // つまみ
        val x = (value.get() * (width - 1)).roundToInt().coerceIn(0, width - 1)
        g.color = Color.WHITE
        g.fillPolygon(
            intArrayOf(x - 5, x + 5, x),
            intArrayOf(0, 0, 5),
            3
        )
        g.color = Color.BLACK
        g.drawPolygon(
            intArrayOf(x - 5, x + 5, x),
            intArrayOf(0, 0, 5),
            3
        )

    }
}
