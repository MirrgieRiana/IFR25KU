package miragefairy2024.colormaker

import mirrg.kotlin.helium.join
import mirrg.kotlin.hydrogen.formatAs
import java.awt.CardLayout
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.GridLayout
import java.awt.Insets
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.image.BufferedImage
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JSplitPane
import javax.swing.border.EmptyBorder
import javax.swing.border.TitledBorder


class LayeredImageSetting(val zoom: Int, val layerSettings: List<LayerSetting>)

fun LayeredImageSetting(zoom: Int, vararg layerSettings: LayerSetting) = LayeredImageSetting(zoom, layerSettings.toList())

class LayerSetting(val imagePath: String, val colorExpression: ColorExpression)

class WindowColorMaker(
    private val imageLoader: (String) -> BufferedImage,
    private val layeredImageSettings: List<LayeredImageSetting>,
    private val sliderNames: List<String>
) : JFrame() {
    private val panelColorSliders = mutableListOf<PanelColorSlider>()

    val backgroundColor = ObservableValue(Color(139, 139, 139))
    val colors = ObservableValue<List<Color>>(sliderNames.map { Color.white })
    val updateImageEvent = ObservableValue(Unit)

    init {
        contentPane.layout = GridLayout(0, 1, 0, 0)

        addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent) {
                backgroundColor.fire()
                colors.fire()
                updateImageEvent.fire()
            }
        })

        add(
            createSplitPaneHorizontal(

                // 左ペイン
                JPanel().also { imagePanel ->

                    imagePanel.layout = GridBagLayout().also {
                        it.columnWidths = intArrayOf(100)
                        it.rowHeights = intArrayOf(0, 0, 0)
                        it.columnWeights = doubleArrayOf(0.0)
                        it.rowWeights = doubleArrayOf(0.0, 0.0, 0.0)
                    }

                    // 画像列
                    val colorEvaluator = ColorEvaluator().also {
                        sliderNames.forEachIndexed { i, name ->
                            it.registerVariable("@$name") { panelColorSliders[i].value.get().color }
                        }
                    }
                    repeat(layeredImageSettings.size) { i ->
                        imagePanel.add(LayeredImage(layeredImageSettings[i].zoom).also { imageLabel ->
                            imageLabel.preferredSize = Dimension(16 * layeredImageSettings[i].zoom, 16 * layeredImageSettings[i].zoom)
                            imageLabel.colorEvaluator = colorEvaluator
                            backgroundColor.register { _, it, _ ->
                                imageLabel.backgroundColor = it
                            }
                            val imageLayerList = layeredImageSettings[i].layerSettings.map { it ->
                                Layer(imageLoader(it.imagePath), it.colorExpression)
                            }
                            updateImageEvent.register { _, _, _ ->
                                //if (!canUpdateImage) return@register
                                imageLabel.render(imageLayerList)
                            }
                        }, GridBagConstraints().also {
                            it.insets = Insets(0, 0, 5, 0)
                            it.gridx = 0
                            it.gridy = i
                        })
                    }

                    backgroundColor.register { _, it, _ ->
                        imagePanel.background = it
                    }

                },

                // 右ペイン
                JPanel().also { rightPane ->

                    rightPane.border = EmptyBorder(4, 4, 4, 4)
                    rightPane.layout = GridBagLayout().also {
                        it.columnWidths = intArrayOf(0)
                        it.rowHeights = intArrayOf(0, 0, 0, 0, 0, 0)
                        it.columnWeights = doubleArrayOf(1.0)
                        it.rowWeights = doubleArrayOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
                    }

                    // 右側スライダーコンポーネント
                    rightPane.add(
                        createPanelTitledBorder(
                            "Background",
                            PanelColorSlider().also { c ->
                                c.value.register { _, it, _ ->
                                    if (backgroundColor.modifying) return@register
                                    backgroundColor.set(it.color, c)
                                }
                                backgroundColor.register { _, it, source ->
                                    if (source == c) return@register
                                    c.value.set(Rgb(it))
                                }
                            }), GridBagConstraints().also {
                            it.fill = GridBagConstraints.HORIZONTAL
                            it.insets = Insets(0, 0, 5, 0)
                            it.gridx = 0
                            it.gridy = 0
                        }
                    )
                    sliderNames.forEachIndexed { i, name ->
                        rightPane.add(
                            createPanelTitledBorder(
                                name,
                                PanelColorSlider().also { c ->
                                    c.value.register { _, _, _ ->
                                        if (colors.modifying) return@register
                                        colors.set(panelColorSliders.map { it.value.get().color }, c)
                                    }
                                    colors.register { _, _, source ->
                                        if (source == c) return@register
                                        c.value.set(Rgb(colors.get()[i]))
                                    }
                                    panelColorSliders += c
                                }), GridBagConstraints().also {
                                it.fill = GridBagConstraints.HORIZONTAL
                                it.insets = Insets(0, 0, 5, 0)
                                it.gridx = 0
                                it.gridy = 1 + i
                            }
                        )
                    }

                    // 右側色構文
                    rightPane.add(ParsingTextField(colors.get(), { it.toColorsOrNull() }, { it.encode() }).also { c ->
                        c.columns = 10
                        c.value.register { _, it, _ ->
                            if (colors.modifying) return@register
                            colors.set(it, c)
                        }
                        colors.register { _, it, source ->
                            if (source == c) return@register
                            c.value.set(it)
                        }
                    }, GridBagConstraints().also {
                        it.fill = GridBagConstraints.HORIZONTAL
                        it.gridx = 0
                        it.gridy = sliderNames.size + 1
                    })

                },

                )
        )

        backgroundColor.register { _, _, _ ->
            updateImageEvent.fire()
        }
        colors.register { _, _, _ ->
            updateImageEvent.fire()
        }

        isLocationByPlatform = true
        defaultCloseOperation = DISPOSE_ON_CLOSE
        pack()
    }
}


private fun String.toColorsOrNull(): List<Color>? = this.split(",").map { it.trim().toColorOrNull() ?: return null }

private fun String.toColorOrNull() = try {
    Color.decode(this)
} catch (_: NumberFormatException) {
    null
}

private fun List<Color>.encode() = this.map { it.rgb and 0xffffff formatAs "0x%06X" }.join(", ")


private fun createPanelTitledBorder(title: String, component: Component) = createPanel(component) {
    layout = CardLayout()
    border = TitledBorder(title)
}

private fun createPanel(vararg components: Component, initializer: JPanel.() -> Unit = {}) = JPanel().also { panel ->
    components.forEach { component ->
        panel.add(component)
    }
    initializer(panel)
}

private fun createSplitPaneHorizontal(vararg components: Component) = createSplitPaneHorizontal(components.toList())

private fun createSplitPaneHorizontal(components: List<Component>): Component = if (components.size == 1) {
    components.first()
} else {
    JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, components.first(), createSplitPaneHorizontal(components.drop(1)))
}
