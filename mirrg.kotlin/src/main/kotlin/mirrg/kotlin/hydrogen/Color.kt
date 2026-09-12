// このファイルは上流のmirrg.kotlinには無いIFR25KU独自のものだから、fetchMirrgKotlinタスクの取得対象にも入っていないのだ～🌱
package mirrg.kotlin.hydrogen

inline val Int.redOfRgb get() = (this shr 16) and 0xFF

inline val Int.greenOfRgb get() = (this shr 8) and 0xFF

inline val Int.blueOfRgb get() = this and 0xFF

inline val Int.alphaOfArgb get() = (this shr 24) and 0xFF

inline val Int.redOfArgb get() = (this shr 16) and 0xFF

inline val Int.greenOfArgb get() = (this shr 8) and 0xFF

inline val Int.blueOfArgb get() = this and 0xFF
