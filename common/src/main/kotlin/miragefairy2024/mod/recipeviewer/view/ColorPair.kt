package miragefairy2024.mod.recipeviewer.view

class ColorPair(val lightModeArgb: Int, val darkModeArgb: Int) {
    companion object {
        val DEFAULT = ColorPair(0xFFFFFFFF.toInt(), 0xFFBBBBBB.toInt())
        val DARK_GRAY = ColorPair(0xFF404040.toInt(), 0xFFBBBBBB.toInt())
    }
}
