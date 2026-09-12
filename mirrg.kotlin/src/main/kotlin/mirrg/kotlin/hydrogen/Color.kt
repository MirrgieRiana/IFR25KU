package mirrg.kotlin.hydrogen

/** RGB色の赤成分なのだ～🌱 */
inline val Int.redOfRgb get() = (this shr 16) and 0xFF

/** RGB色の緑成分なのだ～🌱 */
inline val Int.greenOfRgb get() = (this shr 8) and 0xFF

/** RGB色の青成分なのだ～🌱 */
inline val Int.blueOfRgb get() = this and 0xFF

/** ARGB色のアルファ成分なのだ～🌱 */
inline val Int.alphaOfArgb get() = (this shr 24) and 0xFF

/** ARGB色の赤成分なのだ～🌱 */
inline val Int.redOfArgb get() = (this shr 16) and 0xFF

/** ARGB色の緑成分なのだ～🌱 */
inline val Int.greenOfArgb get() = (this shr 8) and 0xFF

/** ARGB色の青成分なのだ～🌱 */
inline val Int.blueOfArgb get() = this and 0xFF
