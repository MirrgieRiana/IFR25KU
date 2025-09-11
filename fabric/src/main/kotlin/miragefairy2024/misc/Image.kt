package miragefairy2024.misc

import mirrg.kotlin.helium.join
import mirrg.kotlin.helium.max
import java.awt.Color
import java.io.File
import javax.imageio.ImageIO

object ImageMain1 {
    @JvmStatic
    fun main(args: Array<String>) {
        val dir = File("./src/main/resources/assets/miragefairy2024/sounds")
        val image1 = ImageIO.read(dir.resolve("無題.png"))
        val image2 = ImageIO.read(dir.resolve("無題2.png"))
        val conversions: List<Pair<String, (x: Int, y: Int, r: Int, g: Int, b: Int) -> Any?>> = listOf(
            "r" to { x, y, r, g, b -> r },
            "g" to { x, y, r, g, b -> g },
            "b" to { x, y, r, g, b -> b },
            "h" to { x, y, r, g, b -> Color.RGBtoHSB(r, g, b, null)[0] },
            "s" to { x, y, r, g, b -> Color.RGBtoHSB(r, g, b, null)[1] },
            "v" to { x, y, r, g, b -> Color.RGBtoHSB(r, g, b, null)[2] },
            "La" to { x, y, r, g, b -> (r + g + b) / (255 * 3).toDouble() },
            "Lm" to { x, y, r, g, b -> r max g max b },
            "L2a" to { x, y, r, g, b -> (r * 76.0 + g * 149.0 + b * 29.0) / (255 * (76 + 149 + 29)).toDouble() },
        )
        println(
            listOf(
                "x",
                "y",
                *conversions.map { (name, _) -> "${name}1" }.toTypedArray(),
                *conversions.map { (name, _) -> "${name}2" }.toTypedArray(),
            ).join(",")
        )
        (0 until 16).forEach { x ->
            (0 until 16).forEach { y ->
                val rgb1 = image1.getRGB(x, y)
                val rgb2 = image2.getRGB(x, y)
                val r1 = rgb1 shr 16 and 0xFF
                val g1 = rgb1 shr 8 and 0xFF
                val b1 = rgb1 and 0xFF
                val r2 = rgb2 shr 16 and 0xFF
                val g2 = rgb2 shr 8 and 0xFF
                val b2 = rgb2 and 0xFF
                println(
                    listOf(
                        x.toString(),
                        y.toString(),
                        *conversions.map { (_, f) -> f(x, y, r1, g1, b1).toString() }.toTypedArray(),
                        *conversions.map { (_, f) -> f(x, y, r2, g2, b2).toString() }.toTypedArray(),
                    ).join(",")
                )
            }
        }
    }
}
