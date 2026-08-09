package miragefairy2024.client.util

import net.minecraft.client.particle.Particle

fun Particle.setRgb(rgb: Int) {
    this.setColor(
        (rgb shr 16 and 0xFF) / 255F,
        (rgb shr 8 and 0xFF) / 255F,
        (rgb shr 0 and 0xFF) / 255F,
    )
}
