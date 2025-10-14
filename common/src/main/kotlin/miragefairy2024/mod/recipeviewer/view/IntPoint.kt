package miragefairy2024.mod.recipeviewer.view

data class IntPoint(val x: Int, val y: Int) {
    companion object {
        val ZERO = IntPoint(0, 0)
    }
}

fun IntPoint.offset(dx: Int, dy: Int) = IntPoint(x + dx, y + dy)
operator fun IntPoint.unaryMinus() = IntPoint(-x, -y)
operator fun IntPoint.plus(other: IntPoint) = IntPoint(x + other.x, y + other.y)
operator fun IntPoint.minus(other: IntPoint) = IntPoint(x - other.x, y - other.y)
