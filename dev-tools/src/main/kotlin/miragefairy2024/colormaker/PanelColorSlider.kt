package miragefairy2024.colormaker

import java.awt.Color
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.awt.MouseInfo
import java.awt.Rectangle
import java.awt.Robot
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.JPanel
import javax.swing.JToggleButton
import javax.swing.Timer
import kotlin.math.roundToInt

class PanelColorSlider : JPanel() {
    private lateinit var sliderR: PanelSliderField
    private lateinit var sliderG: PanelSliderField
    private lateinit var sliderB: PanelSliderField
    private lateinit var sliderH: PanelSliderField
    private lateinit var sliderS: PanelSliderField
    private lateinit var sliderV: PanelSliderField

    val value: ObservableValue<ColorValue> = ObservableValue(Rgb(Color.white))
    val repaintGradientEvent = ObservableValue(Unit)

    init {

        layout = GridBagLayout().also {
            it.columnWidths = intArrayOf(0, 0, 0, 0)
            it.rowHeights = intArrayOf(0, 0, 0, 0)
            it.columnWeights = doubleArrayOf(1.0, Double.MIN_VALUE, 1.0, Double.MIN_VALUE)
            it.rowWeights = doubleArrayOf(0.0, 0.0, 0.0, 0.0)
        }

        add(PanelSliderField(0, 255) { Rgb(Color(it, sliderG.value.get(), sliderB.value.get())).color.rgb and 0xFFFFFF }.also { c ->
            c.value.register { _, _, _ ->
                if (value.modifying) return@register
                value.set(Rgb(Color(sliderR.value.get(), sliderG.value.get(), sliderB.value.get())), c)
            }
            value.register { _, it, source ->
                if (source == c) return@register
                c.value.set(it.r)
            }
            repaintGradientEvent.register { _, _, _ ->
                c.repaintGradientEvent.fire()
            }
            sliderR = c
        }, GridBagConstraints().also {
            it.insets = Insets(0, 0, 5, 0)
            it.fill = GridBagConstraints.BOTH
            it.gridx = 0
            it.gridy = 0
            it.gridwidth = 2
            it.gridheight = 1
        })

        add(PanelSliderField(0, 255) { Rgb(Color(sliderR.value.get(), it, sliderB.value.get())).color.rgb and 0xFFFFFF }.also { c ->
            c.value.register { _, _, _ ->
                if (value.modifying) return@register
                value.set(Rgb(Color(sliderR.value.get(), sliderG.value.get(), sliderB.value.get())), c)
            }
            value.register { _, it, source ->
                if (source == c) return@register
                c.value.set(it.g)
            }
            repaintGradientEvent.register { _, _, _ ->
                c.repaintGradientEvent.fire()
            }
            sliderG = c
        }, GridBagConstraints().also {
            it.insets = Insets(0, 0, 5, 0)
            it.fill = GridBagConstraints.BOTH
            it.gridx = 0
            it.gridy = 1
            it.gridwidth = 2
            it.gridheight = 1
        })

        add(PanelSliderField(0, 255) { Rgb(Color(sliderR.value.get(), sliderG.value.get(), it)).color.rgb and 0xFFFFFF }.also { c ->
            c.value.register { _, _, _ ->
                if (value.modifying) return@register
                value.set(Rgb(Color(sliderR.value.get(), sliderG.value.get(), sliderB.value.get())), c)
            }
            value.register { _, it, source ->
                if (source == c) return@register
                c.value.set(it.b)
            }
            repaintGradientEvent.register { _, _, _ ->
                c.repaintGradientEvent.fire()
            }
            sliderB = c
        }, GridBagConstraints().also {
            it.insets = Insets(0, 0, 5, 0)
            it.fill = GridBagConstraints.BOTH
            it.gridx = 0
            it.gridy = 2
            it.gridwidth = 2
            it.gridheight = 1
        })

        add(PanelSliderField(0, 360) { Hsv(it, sliderS.value.get(), sliderV.value.get()).color.rgb and 0xFFFFFF }.also { c ->
            c.value.register { _, _, _ ->
                if (value.modifying) return@register
                value.set(Hsv(sliderH.value.get(), sliderS.value.get(), sliderV.value.get()), c)
            }
            value.register { _, it, source ->
                if (source == c) return@register
                c.value.set(it.h)
            }
            repaintGradientEvent.register { _, _, _ ->
                c.repaintGradientEvent.fire()
            }
            sliderH = c
        }, GridBagConstraints().also {
            it.insets = Insets(0, 0, 5, 0)
            it.fill = GridBagConstraints.BOTH
            it.gridx = 2
            it.gridy = 0
            it.gridwidth = 2
            it.gridheight = 1
        })

        add(PanelSliderField(0, 100) { Hsv(sliderH.value.get(), it, sliderV.value.get()).color.rgb and 0xFFFFFF }.also { c ->
            c.value.register { _, _, _ ->
                if (value.modifying) return@register
                value.set(Hsv(sliderH.value.get(), sliderS.value.get(), sliderV.value.get()), c)
            }
            value.register { _, it, source ->
                if (source == c) return@register
                c.value.set(it.s)
            }
            repaintGradientEvent.register { _, _, _ ->
                c.repaintGradientEvent.fire()
            }
            sliderS = c
        }, GridBagConstraints().also {
            it.insets = Insets(0, 0, 5, 0)
            it.fill = GridBagConstraints.BOTH
            it.gridx = 2
            it.gridy = 1
            it.gridwidth = 2
            it.gridheight = 1
        })

        add(PanelSliderField(0, 100) { Hsv(sliderH.value.get(), sliderS.value.get(), it).color.rgb and 0xFFFFFF }.also { c ->
            c.value.register { _, _, _ ->
                if (value.modifying) return@register
                value.set(Hsv(sliderH.value.get(), sliderS.value.get(), sliderV.value.get()), c)
            }
            value.register { _, it, source ->
                if (source == c) return@register
                c.value.set(it.v)
            }
            repaintGradientEvent.register { _, _, _ ->
                c.repaintGradientEvent.fire()
            }
            sliderV = c
        }, GridBagConstraints().also {
            it.insets = Insets(0, 0, 5, 0)
            it.fill = GridBagConstraints.BOTH
            it.gridx = 2
            it.gridy = 2
            it.gridwidth = 2
            it.gridheight = 1
        })

        val pattern = """[0-9a-fA-F]{6}""".toRegex()
        add(ParsingTextField(0, { it.trim().takeIf { s -> s matches pattern }?.toInt(16) }, { String.format("%06X", it and 0xffffff) }).also { c ->
            c.columns = 10
            c.value.register { _, it, _ ->
                if (value.modifying) return@register
                value.set(Rgb(Color(it)), c)
            }
            value.register { _, it, source ->
                if (source == c) return@register
                c.value.set(it.color.rgb and 0xFFFFFF)
            }
        }, GridBagConstraints().also {
            it.fill = GridBagConstraints.HORIZONTAL
            it.gridx = 0
            it.gridy = 3
            it.gridwidth = 1
            it.gridheight = 1
        })

        add(JToggleButton("Pick").also { c ->
            val timer = Timer(20) {
                try {
                    val location = MouseInfo.getPointerInfo().location
                    val createScreenCapture = Robot().createScreenCapture(Rectangle(location.x, location.y, 1, 1))
                    value.set(Rgb(Color(createScreenCapture.getRGB(0, 0))), c)
                } catch (e2: Exception) {
                    e2.printStackTrace()
                }
            }
            timer.isRepeats = true
            c.addActionListener {
                if (c.isSelected) {
                    timer.start()
                } else {
                    timer.stop()
                }
            }
            c.addComponentListener(object : ComponentAdapter() {
                override fun componentHidden(e: ComponentEvent) = timer.stop()
            })
        }, GridBagConstraints().also {
            it.fill = GridBagConstraints.HORIZONTAL
            it.gridx = 1
            it.gridy = 3
            it.gridwidth = 1
            it.gridheight = 1
        })

        value.register { _, _, _ ->
            repaintGradientEvent.fire()
        }
    }
}

sealed interface ColorValue {
    val color: Color
    val r: Int
    val g: Int
    val b: Int
    val h: Int
    val s: Int
    val v: Int
}

class Rgb(override val color: Color) : ColorValue {
    override val r = color.red
    override val g = color.green
    override val b = color.blue
    private val hsv = Color.RGBtoHSB(r, g, b, null)
    override val h = (hsv[0] * 360F).roundToInt()
    override val s = (hsv[1] * 100F).roundToInt()
    override val v = (hsv[2] * 100F).roundToInt()
}

class Hsv(override val h: Int, override val s: Int, override val v: Int) : ColorValue {
    init {
        require(h in 0..360)
        require(s in 0..100)
        require(v in 0..100)
    }

    override val color = Color(Color.HSBtoRGB(h.toFloat() / 360F, s.toFloat() / 100F, v.toFloat() / 100F))
    override val r = color.red
    override val g = color.green
    override val b = color.blue
}
