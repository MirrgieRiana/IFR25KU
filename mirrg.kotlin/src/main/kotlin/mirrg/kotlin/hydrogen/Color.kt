// TODO mirrg
// このファイルは上流のmirrg.kotlinには無いIFR25KU独自のものだから、fetchMirrgKotlinタスクの取得対象にも入っていないのだ～🌱
package mirrg.kotlin.hydrogen

inline val Int.redOfRgb get() = (this shr 16) and 0xFF

inline val Int.greenOfRgb get() = (this shr 8) and 0xFF

inline val Int.blueOfRgb get() = this and 0xFF

inline fun rgbOf(red: Int, green: Int, blue: Int) = ((red and 0xFF) shl 16) or ((green and 0xFF) shl 8) or (blue and 0xFF)

inline val Int.alphaOfArgb get() = (this shr 24) and 0xFF

inline val Int.redOfArgb get() = (this shr 16) and 0xFF

inline val Int.greenOfArgb get() = (this shr 8) and 0xFF

inline val Int.blueOfArgb get() = this and 0xFF

inline fun argbOf(alpha: Int, red: Int, green: Int, blue: Int) = ((alpha and 0xFF) shl 24) or ((red and 0xFF) shl 16) or ((green and 0xFF) shl 8) or (blue and 0xFF)

inline fun argbOf(alpha: Int, rgb: Int) = ((alpha and 0xFF) shl 24) or (rgb and 0xFFFFFF)
