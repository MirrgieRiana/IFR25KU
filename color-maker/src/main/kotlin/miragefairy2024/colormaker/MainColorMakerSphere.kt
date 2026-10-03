package miragefairy2024.colormaker

object MainColorMakerSphere {
    @JvmStatic
    fun main(args: Array<String>) {
        WindowColorMaker(
            { loadImage(it) },
            listOf(
                LayeredImageSetting(
                    4,
                    LayerSetting("sphere_layer0", ColorExpression("@background")),
                    LayerSetting("sphere_layer1", ColorExpression("@plasma")),
                    LayerSetting("sphere_layer2", ColorExpression("@core")),
                    LayerSetting("sphere_layer3", ColorExpression("@highlight")),
                ),
            ),
            listOf("core", "highlight", "background", "plasma"),
        ).isVisible = true
    }
}
