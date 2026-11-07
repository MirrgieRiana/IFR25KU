package miragefairy2024.mod.recipeviewer.view

/** 種類ごとのシングルトンで共有する、スクロールバーのデザインなのだ～🌱 */
interface Scrollbar {
    /** 描画の太さに対して、コンテンツから差し引く負でない占有幅を返すのだ～🌱 */
    fun getReservedThickness(thickness: Int): Int
}

object FlatScrollbar : Scrollbar {
    override fun getReservedThickness(thickness: Int) = thickness
}
