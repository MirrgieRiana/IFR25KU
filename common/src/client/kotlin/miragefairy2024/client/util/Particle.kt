package miragefairy2024.client.util

import miragefairy2024.util.blueOfRgb
import miragefairy2024.util.greenOfRgb
import miragefairy2024.util.redOfRgb
import net.minecraft.client.particle.Particle

fun Particle.setRgb(rgb: Int) {
    this.setColor(
        rgb.redOfRgb.toFloat() / 255F,
        rgb.greenOfRgb.toFloat() / 255F,
        rgb.blueOfRgb.toFloat() / 255F,
    )
}
