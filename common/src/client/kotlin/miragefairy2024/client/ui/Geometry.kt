package miragefairy2024.client.ui

/** コンポーネントの縦と横の長さの組なのだ～🌱 */
data class Size(val width: Int, val height: Int) {
    companion object {
        fun of(width: Int, height: Int) = Size(width, height)
    }
}

/** コンポーネントの四方の余白なのだ～🌱 */
data class Insets(val top: Int, val bottom: Int, val left: Int, val right: Int) {
    val horizontal get() = left + right

    val vertical get() = top + bottom

    companion object {
        fun of(inset: Int) = Insets(inset, inset, inset, inset)
        fun of(top: Int, bottom: Int, left: Int, right: Int) = Insets(top, bottom, left, right)
        fun vertical(inset: Int) = Insets(inset, inset, 0, 0)
    }
}
