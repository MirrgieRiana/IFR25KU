package miragefairy2024.colormaker

import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.awt.event.MouseAdapter
import java.awt.event.MouseWheelEvent
import javax.swing.JPanel
import kotlin.math.roundToInt

class PanelSliderField(private val min: Int, private val max: Int, private val colorFunction: (Int) -> Int) : JPanel() {
    val value = ObservableValue(min)
    val repaintGradientEvent = ObservableValue(Unit)

    init {

        // レイアウト
        layout = GridBagLayout().also {
            it.columnWidths = intArrayOf(300, 50)
            it.rowHeights = intArrayOf(0)
            it.columnWeights = doubleArrayOf(0.0, 0.0)
            it.rowWeights = doubleArrayOf(0.0)
        }

        // スライダーを追加
        add(ColorSlider(max - min + 1) { colorFunction((it * (max - min) + min).roundToInt()) }.also { c ->
            c.value.register { _, it, _ ->
                if (value.modifying) return@register
                value.set((it * (max - min) + min).roundToInt(), c)
            }
            value.register { _, it, source ->
                if (source == c) return@register
                c.value.set((it.toFloat() - min.toFloat()) / (max - min).toFloat())
            }
            repaintGradientEvent.register { _, _, _ ->
                c.repaintGradientEvent.fire()
            }
        }, GridBagConstraints().also {
            it.insets = Insets(1, 5, 1, 5)
            it.fill = GridBagConstraints.HORIZONTAL
            it.gridx = 0
            it.gridy = 0
        })

        // 入力欄
        add(
            ParsingTextField(
                min,
                { it.trim().toIntOrNull()?.takeIf { a -> a in min..max } },
                { "$it" },
            ).also { c ->
                c.columns = 5
                c.value.register { _, it, _ ->
                    if (value.modifying) return@register
                    value.set(it, c)
                }
                c.addMouseWheelListener(object : MouseAdapter() {
                    override fun mouseWheelMoved(e: MouseWheelEvent) {
                        value.set((value.get() + -e.preciseWheelRotation.toInt()).coerceIn(min, max))
                    }
                })
                value.register { _, it, source ->
                    if (source == c) return@register
                    c.value.set(it)
                }
            }, GridBagConstraints().also {
                it.fill = GridBagConstraints.HORIZONTAL
                it.gridx = 1
                it.gridy = 0
            }
        )

        value.register { _, _, _ ->
            repaintGradientEvent.fire()
        }
    }
}
