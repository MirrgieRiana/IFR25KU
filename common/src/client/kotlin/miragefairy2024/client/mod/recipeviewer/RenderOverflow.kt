package miragefairy2024.client.mod.recipeviewer

/**
 * 配置矩形の各辺から外向きに測った非負の描画のはみ出し量なのだ～🌱
 * `0` を指定した辺からは描画がはみ出さないのだ～🌱
 * 各辺に [kotlin.Int.MAX_VALUE] を指定すると、その方向のはみ出し量に上限がないことを表すのだ～🌱
 */
data class RenderOverflow(val xMin: Int, val xMax: Int, val yMin: Int, val yMax: Int) {
    init {
        require(xMin >= 0 && xMax >= 0 && yMin >= 0 && yMax >= 0) { "Render outsets must be non-negative" }
    }

    companion object {
        val NONE = RenderOverflow(0, 0, 0, 0)
        val UNBOUNDED = RenderOverflow(Int.MAX_VALUE, Int.MAX_VALUE, Int.MAX_VALUE, Int.MAX_VALUE)
    }
}
