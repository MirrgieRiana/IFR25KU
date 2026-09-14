package miragefairy2024.client.util

import mirrg.kotlin.hydrogen.blueOfRgb
import mirrg.kotlin.hydrogen.greenOfRgb
import mirrg.kotlin.hydrogen.redOfRgb
import net.minecraft.client.particle.Particle

fun Particle.setRgb(rgb: Int) {
    this.setColor(
        rgb.redOfRgb.toFloat() / 255F,
        rgb.greenOfRgb.toFloat() / 255F,
        rgb.blueOfRgb.toFloat() / 255F,
    )
}
