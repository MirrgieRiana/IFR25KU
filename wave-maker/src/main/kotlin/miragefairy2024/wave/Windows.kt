package miragefairy2024.wave

import kotlin.math.PI
import kotlin.math.cos

fun hanningWindow(t: Double, length: Double) = 0.5 - 0.5 * cos(2 * PI * t / length)
